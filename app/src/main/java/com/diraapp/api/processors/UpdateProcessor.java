package com.diraapp.api.processors;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.diraapp.api.SocketClient;
import com.diraapp.api.processors.listeners.ProcessorListener;
import com.diraapp.api.processors.listeners.SocketListener;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.GetUpdatesRequest;
import com.diraapp.api.requests.Request;
import com.diraapp.api.requests.SubscribeRequest;
import com.diraapp.api.updates.MemberUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.NewRoomUpdate;
import com.diraapp.api.updates.RoomUpdate;
import com.diraapp.api.updates.ServerSyncUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateDeserializer;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.daos.MemberDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.CustomClientData;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.NewUserRoomJoining;
import com.diraapp.db.entities.messages.RoomIconChange;
import com.diraapp.db.entities.messages.RoomNameChange;
import com.diraapp.exceptions.OldUpdateException;
import com.diraapp.exceptions.SingletonException;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.notifications.Notifier;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.DiraApplication;
import com.google.gson.Gson;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.net.URISyntaxException;
import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * UpdateProcessor is a core Dira class
 * <p>
 * It receives and sends all changes to be synced with other users
 *
 * @author Mikhail
 */
public class UpdateProcessor {

    public static final String OFFICIAL_ADDRESS = "ws://diraapp.com:8888";
    public static final String API_VERSION = "0.0.2";

    private static UpdateProcessor updateProcessor;
    private final HashMap<String, SocketClient> socketClients = new HashMap<>();
    private final HashMap<String, String> fileServers = new HashMap<>();
    private final HashMap<Long, UpdateListener> updateReplies = new HashMap<>();
    private final List<UpdateListener> updateListeners = new ArrayList<>();
    private final List<ProcessorListener> processorListeners = new ArrayList<>();
    private final Context context;
    /**
     * Requests ids counter
     */
    private final HashMap<String, Long> lastRequestIds = new HashMap<>();
    /**
     * Server startup time allows to sync updates ids
     */
    private final HashMap<String, Long> timeServerStartups = new HashMap<>();
    private final RoomDao roomDao;
    private final MemberDao memberDao;
    private final RoomUpdatesProcessor roomUpdatesProcessor;
    int updatedRoomsCount = 0;
    private SocketClient socketClient;

    public UpdateProcessor(Context context) throws SingletonException {
        if (updateProcessor != null) throw new SingletonException();
        this.context = context;


        for (String serverAddress : AppStorage.getServerList(context)) {
            try {
                registerSocket(serverAddress);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }


        roomDao = DiraRoomDatabase.getDatabase(context).getRoomDao();
        memberDao = DiraRoomDatabase.getDatabase(context).getMemberDao();
        roomUpdatesProcessor = new RoomUpdatesProcessor(roomDao, memberDao, context);

    }

    public static UpdateProcessor getInstance() {
        return updateProcessor;
    }

    public static UpdateProcessor getInstance(Context context) {
        if (updateProcessor == null) {
            try {
                updateProcessor = new UpdateProcessor(context);
            } catch (SingletonException e) {
                e.printStackTrace();
            }
        }
        return updateProcessor;
    }

    public String getFileServer(String address) {
        System.out.println("Getting file server  for " + address);
        return fileServers.get(address);
    }

    private void registerSocket(String address) throws URISyntaxException {
        socketClient = new SocketClient(address);
        setupSocketClient(socketClient);
        socketClient.connect();
        socketClients.put(address, socketClient);
    }

    /**
     * Main handler for all updates
     *
     * @param message
     * @param address
     */
    public void notifyMessage(String message, String address) {
        Update update = UpdateDeserializer.deserialize(message);
        if (updateReplies.containsKey(update.getOriginRequestId())) {
            try {
                updateReplies.get(update.getOriginRequestId()).onUpdate(update);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {


            if (update.getUpdateType() == UpdateType.SERVER_SYNC) {
                ServerSyncUpdate serverSyncUpdate = (ServerSyncUpdate) update;
                System.out.println("New file server " + serverSyncUpdate.getFileServerUrl() + " for " + address);
                fileServers.put(address, serverSyncUpdate.getFileServerUrl());
                timeServerStartups.put(address, serverSyncUpdate.getTimeServerStart());
            }
            long timeServerStartup = 0;

            if (timeServerStartups.containsKey(address)) {
                timeServerStartup = timeServerStartups.get(address);
            }
            Room roomUpdate = roomDao.getRoomBySecretName(update.getRoomSecret());
            if (roomUpdate != null) {
                if (roomUpdate.getTimeServerStartup() != timeServerStartup) {
                    roomUpdate.setLastUpdateId(0);
                    roomUpdate.setTimeServerStartup(timeServerStartup);
                    roomDao.update(roomUpdate);
                }
                if (roomUpdate.getLastUpdateId() < update.getUpdateId()) {

                    if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
                        updateMember(((MemberUpdate) update));
                        roomUpdate.setLastUpdateId(update.getUpdateId());
                    }

                }
                roomDao.update(roomUpdate);

            }

            if (update.getUpdateType() == UpdateType.NEW_ROOM_UPDATE) {
                roomUpdatesProcessor.onNewRoom((NewRoomUpdate) update, address);
            } else if (update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE) {
                if (DiraApplication.isBackgrounded()) {
                    Notifier.notifyMessage(((NewMessageUpdate) update).getMessage(), context);
                }
                roomUpdatesProcessor.updateRoom(update);

                /*
                 * Save attachments
                 */
                for (Attachment attachment : ((NewMessageUpdate) update).getMessage().getAttachments()) {
                    SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(context, true, attachment,
                            ((NewMessageUpdate) update).getMessage().getRoomSecret());

                    AttachmentsStorage.saveAttachmentAsync(saveAttachmentTask, address);
                }
            } else if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
                roomUpdatesProcessor.updateRoom(update);
            }

            notifyUpdateListeners(update);
        } catch (OldUpdateException oldUpdateException) {
            oldUpdateException.printStackTrace();
        }
    }

    /**
     * Apply changes of room member to local database
     *
     * @param memberUpdate
     */
    public void updateMember(MemberUpdate memberUpdate) {
        if (memberUpdate.getId().equals(new CacheUtils(context).getString(CacheUtils.ID)))
            return;

        Member member = memberDao.getMemberByIdAndRoomSecret(memberUpdate.getId(), memberUpdate.getRoomSecret());

        boolean hasMemberInDatabase = true;

        if (member == null) {
            hasMemberInDatabase = false;
            member = new Member(memberUpdate.getId(), memberUpdate.getNickname(),
                    null, memberUpdate.getRoomSecret(), memberUpdate.getUpdateTime());

            // I think there we can observe for new user join room
            notifyMemberAdded(memberUpdate);
        }

        member.setLastTimeUpdated(memberUpdate.getUpdateTime());
        member.setNickname(memberUpdate.getNickname());

        if (memberUpdate.getBase64pic() != null) {
            Bitmap bitmap = AppStorage.getBitmapFromBase64(memberUpdate.getBase64pic());
            String path = AppStorage.saveToInternalStorage(bitmap,
                    member.getId() + "_" + memberUpdate.getRoomSecret(), memberUpdate.getRoomSecret(), context);
            member.setImagePath(path);
        }

        if (hasMemberInDatabase) {
            memberDao.update(member);
        } else {
            memberDao.insertAll(member);
        }
    }

    public void deleteRoom(Room room) {
        for (Member member : memberDao.getMembersByRoomSecret(room.getSecretName())) {
            memberDao.delete(member);
        }
        roomDao.delete(room);

    }

    /**
     * Sends new request to active connection with callback and specific server address
     *
     * @param request
     * @param callback
     * @param address
     * @return
     * @throws WebsocketNotConnectedException
     * @throws UnablePerformRequestException
     */
    public Request sendRequest(Request request, UpdateListener callback, String address) throws WebsocketNotConnectedException, UnablePerformRequestException {
        try {
            long lastRequestId = 0;

            if (lastRequestIds.containsKey(address)) {
                lastRequestId = lastRequestIds.get(address);
            }

            request.setRequestId(lastRequestId);
            SocketClient socketClient = socketClients.get(address);
            if (socketClient != null) {
                Gson gson = new Gson();
                String sent = gson.toJson(request);
                System.out.println(sent);
                socketClient.send(sent);
                if (callback != null) {
                    long finalLastRequestId = lastRequestId;
                    updateReplies.put(lastRequestId, new UpdateListener() {
                        @Override
                        public void onUpdate(Update update) {
                            try {
                                callback.onUpdate(update);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            updateReplies.remove(finalLastRequestId);
                        }
                    });
                }
            }

            lastRequestId++;
            lastRequestIds.put(address, lastRequestId);
            return request;
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnablePerformRequestException();
        }

    }

    public Request sendRequest(Request request, String serverAddress) throws UnablePerformRequestException {
        return sendRequest(request, null, serverAddress);
    }

    public void notifySocketsCountChanged() {
        float percent = getActiveSocketsPercent();
        for (ProcessorListener processorListener : processorListeners) {
            processorListener.onSocketsCountChange(percent);
        }
    }

    /**
     * Force to reconnect all sockets
     */
    public void reconnectSockets() {
        Log.d("UpdateProcessor", "Reconnecting sockets..");
        try {
            for (String address : new HashSet<>(socketClients.keySet())) {
                SocketClient socketClient = socketClients.get(address);
                if (socketClient != null) {
                    if (socketClient.isClosed()) {
                        try {
                            socketClient = new SocketClient(address);
                            setupSocketClient(socketClient);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        socketClient.connect();

                    }
                } else {
                    try {
                        socketClient = new SocketClient(address);
                        setupSocketClient(socketClient);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    socketClient.connect();
                }
                socketClients.remove(address);
                socketClients.put(address, socketClient);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Subscribes client to all rooms and
     * gets latest updates for each room
     * <p>
     * After subscription server will begin sending updates
     */
    public void sendSubscribeRequest() {

        updatedRoomsCount = 0;

        List<Room> rooms = roomDao.getAllRoomsByUpdatedTime();


        for (Room room : new ArrayList<>(rooms)) {
            Message message = DiraMessageDatabase.getDatabase(context).getMessageDao().getMessageById(room.getLastMessageId());
            room.setMessage(message);
            try {
                UpdateProcessor.getInstance().sendRequest(new GetUpdatesRequest(room.getSecretName(), room.getLastUpdateId()),
                        new UpdateListener() {
                            @Override
                            public void onUpdate(Update update) {
                                updatedRoomsCount++;

                                if (updatedRoomsCount == rooms.size()) {
                                    SubscribeRequest subscribeRequest = new SubscribeRequest();

                                    List<String> roomSecrets = new ArrayList<>();

                                    for (Room room : roomDao.getAllRoomsByUpdatedTime()) {
                                        roomSecrets.add(room.getSecretName());
                                    }

                                    subscribeRequest.setRoomSecrets(roomSecrets);
                                    try {
                                        sendRequest(subscribeRequest, room.getServerAddress());
                                    } catch (UnablePerformRequestException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, room.getServerAddress());
            } catch (Exception ignored) {
                ignored.printStackTrace();

            }
        }


    }

    private void setupSocketClient(SocketClient socketClient) {
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

    /**
     * Callbacks
     */

    public void addCallbackListener(UpdateListener updateListener, long requestId) {
        if (updateReplies.containsKey(requestId)) return;
        updateReplies.remove(requestId);
    }

    public void removeCallbackListener(long requestId) {
        updateReplies.remove(requestId);
    }

    public void addProcessorListener(ProcessorListener updateListener) {
        if (processorListeners.contains(updateListener)) return;
        processorListeners.add(updateListener);
    }

    public void removeProcessorListener(ProcessorListener updateListener) {
        processorListeners.remove(updateListener);
    }

    public void addUpdateListener(UpdateListener updateListener) {
        if (updateListeners.contains(updateListener)) return;
        updateListeners.add(updateListener);
    }

    public void removeUpdateListener(UpdateListener updateListener) {
        updateListeners.remove(updateListener);
    }

    public float getActiveSocketsPercent() {
        int countActive = 0;

        for (SocketClient socketClient : socketClients.values()) {
            if (socketClient.isOpen()) countActive++;
        }

        return countActive / (float) socketClients.size();
    }

    public long getTimeServerStartup(String address) {
        if (!timeServerStartups.containsKey(address)) return 0;
        return timeServerStartups.get(address);
    }

    public HashMap<String, SocketClient> getSocketClients() {
        return new HashMap<>(socketClients);
    }

    private void notifyUpdateListeners(Update update) {
        for (UpdateListener updateListener : updateListeners) {
            try {
                updateListener.onUpdate(update);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyMemberAdded(MemberUpdate memberUpdate) {
        NewUserRoomJoining joining = new NewUserRoomJoining(memberUpdate.getNickname());
        notifyRoomChange(memberUpdate.getRoomSecret(), joining);
    }

    public void notifyRoomNameChange(RoomUpdate roomUpdate, String oldName) {
        RoomNameChange roomNameChange = new RoomNameChange(roomUpdate.getName(), oldName);
        notifyRoomChange(roomUpdate.getRoomSecret(), roomNameChange);
    }

    public void notifyRoomIconChange(Room room) {
        RoomIconChange roomIconChange = new RoomIconChange(room.getImagePath());
        notifyRoomChange(room.getSecretName(), roomIconChange);
    }

    private void notifyRoomChange(String secretName, CustomClientData clientData) {
        Message message = new Message(secretName, clientData);
        NewMessageUpdate messageUpdate = new NewMessageUpdate(message);

        // notifyUpdateListeners(messageUpdate);
    }


}
