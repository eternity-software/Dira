package ru.dira.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;

import androidx.room.RoomDatabase;

import com.google.gson.Gson;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.net.ContentHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.dira.activities.RoomSelectorActivity;
import ru.dira.adapters.RoomSelectorAdapter;
import ru.dira.api.InviteRoom;
import ru.dira.api.RoomMember;
import ru.dira.api.SocketClient;
import ru.dira.api.requests.GetUpdatesRequest;
import ru.dira.api.requests.Request;
import ru.dira.api.requests.RoomUpdateRequest;
import ru.dira.api.requests.SubscribeRequest;
import ru.dira.api.updates.MemberUpdate;
import ru.dira.api.updates.NewMessageUpdate;
import ru.dira.api.updates.NewRoomUpdate;
import ru.dira.api.updates.RoomUpdate;
import ru.dira.api.updates.ServerSyncUpdate;
import ru.dira.api.updates.Update;
import ru.dira.api.updates.UpdateDeserializer;
import ru.dira.api.updates.UpdateType;
import ru.dira.attachments.ImageStorage;
import ru.dira.db.DiraMessageDatabase;
import ru.dira.db.DiraRoomDatabase;
import ru.dira.db.daos.MemberDao;
import ru.dira.db.daos.RoomDao;
import ru.dira.db.entities.Member;
import ru.dira.db.entities.Message;
import ru.dira.db.entities.Room;
import ru.dira.exceptions.OldUpdateException;
import ru.dira.exceptions.SingletonException;
import ru.dira.exceptions.UnablePerformRequestException;
import ru.dira.notifications.Notifier;
import ru.dira.utils.CacheUtils;
import ru.dira.utils.DiraApplication;

public class UpdateProcessor {

    public static final String OFFICIAL_ADDRESS = "ws://164.132.138.80:8888";

    private static UpdateProcessor updateProcessor;

    private HashMap<String, SocketClient> socketClients = new HashMap<>();
    private HashMap<Long, UpdateListener> updateCallbacks = new HashMap<>();
    private List<UpdateListener> updateListeners = new ArrayList<>();
    private List<UpdateProcessorListener> processorListeners = new ArrayList<>();
    private long lastRequestId = 0;
    private long timeServerStartup = 0;
    private RoomDao roomDao;
    private MemberDao memberDao;
    private  SocketClient socketClient;
    private Context context;



    public UpdateProcessor(Context context) throws SingletonException {
        if(updateProcessor != null) throw new SingletonException();
        this.context = context;
        try {
            socketClient = new SocketClient(new URI(OFFICIAL_ADDRESS));
            setupSocketClient(socketClient);
            socketClient.connect();
            socketClients.put(OFFICIAL_ADDRESS, socketClient);
            roomDao = DiraRoomDatabase.getDatabase(context).getRoomDao();
            memberDao = DiraRoomDatabase.getDatabase(context).getMemberDao();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static UpdateProcessor getInstance()
    {
        return updateProcessor;
    }

    public static UpdateProcessor getInstance(Context context)
    {
        if(updateProcessor == null) {
            try {
                updateProcessor = new UpdateProcessor(context);
            } catch (SingletonException e) {
                e.printStackTrace();
            }
        }
        return updateProcessor;
    }

    public float getActiveSocketsPercent()
    {
        int countActive = 0;

        for(SocketClient socketClient : socketClients.values())
        {
            if(socketClient.isOpen()) countActive++;
        }

        return countActive / (float) socketClients.size();
    }

    public void addCustomServer(String address)
    {
        socketClients.put(address, null);
    }

    public void removeCustomServer(String address)
    {
        try {
            socketClients.get(address).close();
            socketClients.remove(address);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public HashMap<String, SocketClient> getSocketClients() {
        return new HashMap<>(socketClients);
    }

    public void notifyMessage(String message, String address)
    {

        Update update = UpdateDeserializer.deserialize(message);
        if(updateCallbacks.containsKey(update.getOriginRequestId()))
        {
            try {
                updateCallbacks.get(update.getOriginRequestId()).onUpdate(update);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        try {

           if(update.getUpdateType() == UpdateType.SERVER_SYNC)
           {
               ServerSyncUpdate serverSyncUpdate = (ServerSyncUpdate) update;
               timeServerStartup = serverSyncUpdate.getTimeServerStart();
           }


            Room roomUpdate = roomDao.getRoomBySecretName(update.getRoomSecret());
           if(roomUpdate != null)
           {
               if(roomUpdate.getTimeServerStartup() != timeServerStartup)
               {
                   roomUpdate.setLastUpdateId(0);
                   roomUpdate.setTimeServerStartup(timeServerStartup);
                   roomDao.update(roomUpdate);
               }
               if(roomUpdate.getLastUpdateId() < update.getUpdateId())
               {

                   if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
                       updateMember(((MemberUpdate) update));
                       roomUpdate.setLastUpdateId(update.getUpdateId());
                   }

               }
               roomDao.update(roomUpdate);

           }

            if (update.getUpdateType() == UpdateType.NEW_ROOM_UPDATE) {

                NewRoomUpdate newRoomUpdate = (NewRoomUpdate) update;



                InviteRoom inviteRoom = newRoomUpdate.getInviteRoom();

                Room oldRoom = roomDao.getRoomBySecretName(newRoomUpdate.getRoomSecret());
                if(oldRoom == null) {

                    Room room = new Room(inviteRoom.getName(), System.currentTimeMillis(), inviteRoom.getSecretName());

                    if (inviteRoom.getBase64pic() != null) {
                        Bitmap bitmap = ImageStorage.getBitmapFromBase64(inviteRoom.getBase64pic());
                        room.setImagePath(ImageStorage.saveToInternalStorage(bitmap, room.getSecretName(), context));
                    }

                    room.setLastUpdateId(0);

                    for (RoomMember roomMember : inviteRoom.getMemberList()) {

                        Member member = memberDao.getMemberByIdAndRoomSecret(roomMember.getId(), roomMember.getRoomSecret());

                        boolean hasMemberInDatabase = true;
                        if (member == null) {
                            hasMemberInDatabase = false;
                            member = new Member(roomMember.getId(), roomMember.getNickname(),
                                    null, roomMember.getRoomSecret(), roomMember.getLastTimeUpdated());
                        }

                        member.setLastTimeUpdated(roomMember.getLastTimeUpdated());
                        member.setNickname(roomMember.getNickname());

                        if (roomMember.getImageBase64() != null) {
                            Bitmap bitmap = ImageStorage.getBitmapFromBase64(roomMember.getImageBase64());
                            String path = ImageStorage.saveToInternalStorage(bitmap, member.getId() + "_" + roomMember.getRoomSecret(), context);
                            member.setImagePath(path);
                        }

                        if (hasMemberInDatabase) {
                            memberDao.insertAll(member);
                        } else {
                            memberDao.update(member);
                        }

                    }

                    roomDao.insertAll(room);
                }

            }
            else if (update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE) {
                if(DiraApplication.isBackgrounded())
                {
                    Notifier.notifyMessage(((NewMessageUpdate) update).getMessage(), context);
                }
                updateRoom(((NewMessageUpdate) update));
            }
            else if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
                updateRoom(((RoomUpdate) update));
            }


            for (UpdateListener updateListener : updateListeners) {
                try {
                    updateListener.onUpdate(update);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
         catch (OldUpdateException oldUpdateException)
        {
            oldUpdateException.printStackTrace();
            return;
        }
    }


    public void updateMember(MemberUpdate memberUpdate)
    {
        if(memberUpdate.getId().equals(CacheUtils.getInstance().getString(CacheUtils.ID, context))) return;


        Member member = memberDao.getMemberByIdAndRoomSecret(memberUpdate.getId(), memberUpdate.getRoomSecret());



        boolean hasMemberInDatabase = true;

        if(member == null) {
            hasMemberInDatabase = false;
            member = new Member(memberUpdate.getId(), memberUpdate.getNickname(),
                    null, memberUpdate.getRoomSecret(), memberUpdate.getUpdateTime());
        }

            member.setLastTimeUpdated(memberUpdate.getUpdateTime());
            member.setNickname(memberUpdate.getNickname());

            if(memberUpdate.getBase64pic() != null)
            {
                Bitmap bitmap = ImageStorage.getBitmapFromBase64(memberUpdate.getBase64pic());
                String path = ImageStorage.saveToInternalStorage(bitmap, member.getId() + "_" + memberUpdate.getRoomSecret(), context);
                member.setImagePath(path);
            }

            if(hasMemberInDatabase)
            {
                System.out.println("updating member");
                memberDao.update(member);
            }
            else
            {
                System.out.println("inserting member");
                memberDao.insertAll(member);

            }

    }

    private void updateRoom(RoomUpdate roomUpdate) throws OldUpdateException {

        Room room = roomDao.getRoomBySecretName(roomUpdate.getRoomSecret());
        if(room != null)
        {
            System.out.println(room.getLastUpdateId() + ", mew " + roomUpdate.getUpdateId());
            if(room.getLastUpdateId() < roomUpdate.getUpdateId())
            {
                room.setLastUpdateId(roomUpdate.getUpdateId());
                room.setName(roomUpdate.getName());
                if(roomUpdate.getBase64Pic() != null)
                {
                    Bitmap bitmap = ImageStorage.getBitmapFromBase64(roomUpdate.getBase64Pic());
                    String path = ImageStorage.saveToInternalStorage(bitmap, room.getSecretName(), context);
                    room.setImagePath(path);
                }

                roomDao.update(room);

            }
            else
            {
                System.out.println(room.getLastUpdateId() + " " + roomUpdate.getUpdateId());
                throw new OldUpdateException();
            }

        }
    }

    private void updateRoom(NewMessageUpdate newMessageUpdate) throws OldUpdateException {
        Message newMessage = newMessageUpdate.getMessage();
        Room room = roomDao.getRoomBySecretName(newMessage.getRoomSecret());
        if(room != null)
        {


            compareStartupTimes(room);
            if(room.getLastUpdateId() < newMessageUpdate.getUpdateId())
            {
                room.setLastUpdateId(newMessageUpdate.getUpdateId());
                room.setLastMessageId(newMessage.getId());
                room.setLastUpdatedTime(newMessage.getTime());
                room.setUpdatedRead(false);
                DiraMessageDatabase.getDatabase(context).getMessageDao().insertAll(newMessage);
                roomDao.update(room);
            }
            else
            {
                throw new OldUpdateException();
            }

        }
    }

    private void compareStartupTimes(Room room)
    {
        if(room.getTimeServerStartup() != timeServerStartup)
        {
            room.setLastUpdateId(0);
            room.setTimeServerStartup(timeServerStartup);
            roomDao.update(room);
        }
    }

    public void addCallbackListener(UpdateListener updateListener, long requestId)
    {
        if(updateCallbacks.containsKey(requestId)) return;
        updateCallbacks.remove(requestId);
    }

    public void removeCallbackListener(long requestId)
    {
        updateCallbacks.remove(requestId);
    }

    public void addProcessorListener(UpdateProcessorListener updateListener)
    {
        if(processorListeners.contains(updateListener)) return;
        processorListeners.add(updateListener);
    }

    public void removeProcessorListener(UpdateProcessorListener updateListener)
    {
        processorListeners.remove(updateListener);
    }

    public void addUpdateListener(UpdateListener updateListener)
    {
        if(updateListeners.contains(updateListener)) return;
        updateListeners.add(updateListener);
    }

    public void removeUpdateListener(UpdateListener updateListener)
    {
        updateListeners.remove(updateListener);
    }

    public Request sendRequest(Request request) throws UnablePerformRequestException {
        return sendRequest(request, null, OFFICIAL_ADDRESS);
    }

    public Request sendRequest(Request request, UpdateListener callback) throws UnablePerformRequestException {
        return sendRequest(request, callback, OFFICIAL_ADDRESS);
    }

    public Request sendRequest(Request request, String address) throws UnablePerformRequestException {
        return sendRequest(request, null, address);
    }

    public Request sendRequest(Request request, UpdateListener callback, String address) throws WebsocketNotConnectedException, UnablePerformRequestException {
        try {
            request.setRequestId(lastRequestId);
            SocketClient socketClient = socketClients.get(address);
            if (socketClient != null) {
                Gson gson = new Gson();
                String sent = gson.toJson(request);
                System.out.println(sent);
                socketClient.send(sent);
                if (callback != null) {
                    updateCallbacks.put(lastRequestId, new UpdateListener() {
                        @Override
                        public void onUpdate(Update update) {
                            try {
                                callback.onUpdate(update);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            updateCallbacks.remove(lastRequestId);
                        }
                    });
                }
            }
            lastRequestId++;
            return request;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new UnablePerformRequestException();
        }

    }

    public void notifySocketsCountChanged()
    {
        float percent = getActiveSocketsPercent();
        for(UpdateProcessorListener updateProcessorListener : processorListeners)
        {
            updateProcessorListener.onSocketsCountChange(percent);
        }
    }

    public void reconnectSockets()
    {
        for(String address : socketClients.keySet())
        {
            SocketClient socketClient = socketClients.get(address);
            if(socketClient != null) {
                if (socketClient.isClosed()) {
                    try {
                        socketClient = new SocketClient(new URI(address));
                        setupSocketClient(socketClient);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    socketClient.connect();

                }
            }
            else
            {
                try {
                    socketClient = new SocketClient(new URI(address));
                    setupSocketClient(socketClient);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                socketClient.connect();
            }
            socketClients.remove(address);
            socketClients.put(address, socketClient);

        }
    }
    int updatedRoomsCount = 0;
    public void sendSubscribeRequest()
    {

        updatedRoomsCount = 0;

        List<Room> rooms = roomDao.getAllRoomsByUpdatedTime();



        for(Room room : new ArrayList<>(rooms))
        {
            Message message = DiraMessageDatabase.getDatabase(context).getMessageDao().getMessageById(room.getLastMessageId());
            room.setMessage(message);
            try {
                UpdateProcessor.getInstance().sendRequest(new GetUpdatesRequest(room.getSecretName(), room.getLastUpdateId()),
                        new UpdateListener() {
                            @Override
                            public void onUpdate(Update update) {
                                updatedRoomsCount++;

                                if(updatedRoomsCount == rooms.size())
                                {
                                    SubscribeRequest subscribeRequest = new SubscribeRequest();

                                    List<String> roomSecrets = new ArrayList<>();

                                    for(Room room : roomDao.getAllRoomsByUpdatedTime())
                                    {
                                        roomSecrets.add(room.getSecretName());
                                    }

                                    subscribeRequest.setRoomSecrets(roomSecrets);
                                    try {
                                        sendRequest(subscribeRequest);
                                    } catch (UnablePerformRequestException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
            }
            catch (Exception ignored)
            {
                ignored.printStackTrace();

            }
        }


    }

    private void setupSocketClient(SocketClient socketClient)
    {
        socketClient.setSocketListener(new SocketListener() {
            @Override
            public void onSocketOpened() {
                notifySocketsCountChanged();
                sendSubscribeRequest();
            }

            @Override
            public void onSocketClosed() {
                notifySocketsCountChanged();
            }
        });

    }

}
