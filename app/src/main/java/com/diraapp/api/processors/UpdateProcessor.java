package com.diraapp.api.processors;

import android.content.Context;
import android.util.Log;

import com.diraapp.api.SocketClient;
import com.diraapp.api.processors.listeners.ProcessorListener;
import com.diraapp.api.processors.listeners.SocketListener;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.GetUpdatesRequest;
import com.diraapp.api.requests.MessageReadRequest;
import com.diraapp.api.requests.PingReactRequest;
import com.diraapp.api.requests.Request;
import com.diraapp.api.requests.SubscribeRequest;
import com.diraapp.api.updates.DhInitUpdate;
import com.diraapp.api.updates.KeyReceivedUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.NewRoomUpdate;
import com.diraapp.api.updates.PingUpdate;
import com.diraapp.api.updates.RenewingCancelUpdate;
import com.diraapp.api.updates.RenewingConfirmUpdate;
import com.diraapp.api.updates.ServerSyncUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateDeserializer;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.api.views.BaseMember;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.daos.MemberDao;
import com.diraapp.db.daos.MessageDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.OldUpdateException;
import com.diraapp.exceptions.SingletonException;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.notifications.Notifier;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.SaveAttachmentTask;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.DiraApplication;
import com.diraapp.utils.EncryptionUtil;
import com.google.gson.Gson;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * UpdateProcessor is a Dira core class
 * <p>
 * It receives and sends all changes to be synced with other users
 *
 * @author Mikhail
 */
public class UpdateProcessor {

    public static final String OFFICIAL_ADDRESS = "ws://diraapp.com:8888";
    public static final String API_VERSION = "0.0.3";

    private static UpdateProcessor instance;
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
     * Server startup time allows to sync updates' ids
     */
    private final HashMap<String, Long> timeServerStartups = new HashMap<>();
    private final RoomDao roomDao;
    private final MemberDao memberDao;
    private final RoomUpdatesProcessor roomUpdatesProcessor;
    private final DiraKeyProtocolProcessor diraKeyProtocol;

    private final ClientMessageProcessor clientMessageProcessor;
    int updatedRoomsCount = 0;
    private SocketClient socketClient;

    public UpdateProcessor(Context context) throws SingletonException {
        if (instance != null) throw new SingletonException();
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

        diraKeyProtocol = new DiraKeyProtocolProcessor(roomDao, new CacheUtils(context));
        MessageDao messageDao = DiraMessageDatabase.getDatabase(context).getMessageDao();
        roomUpdatesProcessor = new RoomUpdatesProcessor(roomDao, memberDao, messageDao, context);
        clientMessageProcessor = new ClientMessageProcessor(context);

    }

    public static UpdateProcessor getInstance() {
        return instance;
    }

    public static UpdateProcessor getInstance(Context context) {
        if (instance == null) {
            try {
                instance = new UpdateProcessor(context);
            } catch (SingletonException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Room getRoom(String roomSecret) {
        return roomDao.getRoomBySecretName(roomSecret);
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
            } else if (update.getUpdateType() == UpdateType.NEW_ROOM_UPDATE) {
                roomUpdatesProcessor.onNewRoom((NewRoomUpdate) update, address);
            } else if (update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE) {
                NewMessageUpdate newMessageUpdate = ((NewMessageUpdate) update);

                boolean isDecrypted = false;
                Room room = roomDao.getRoomBySecretName(newMessageUpdate.getMessage().getRoomSecret());
                if (room.getTimeEncryptionKeyUpdated() == newMessageUpdate.getMessage().getLastTimeEncryptionKeyUpdated()) {
                    if (!room.getEncryptionKey().equals("")) {
                        String rawText = newMessageUpdate.getMessage().getText();
                        newMessageUpdate.getMessage().setText(EncryptionUtil.decrypt(rawText,
                                room.getEncryptionKey()));
                        isDecrypted = true;
                    }
                }

                ((NewMessageUpdate) update).getMessage().setRead(newMessageUpdate.getMessage().
                        getAuthorId().equals(new CacheUtils(context).getString(CacheUtils.ID)));

                if (!isDecrypted) {
                    // here you can write your shit code
                }

                if (DiraApplication.isBackgrounded()) {
                    Notifier.notifyMessage(newMessageUpdate.getMessage(), context);
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
            } else if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
                roomUpdatesProcessor.updateRoom(update);
            } else if (update.getUpdateType() == UpdateType.DIFFIE_HELLMAN_INIT_UPDATE) {
                diraKeyProtocol.onDiffieHellmanInit((DhInitUpdate) update);
                System.out.println("Dhinit update - " + message);

                // Exactly this shit brakes key generation
                // Don't know why

                //roomUpdatesProcessor.updateRoom(update, true);
            } else if (update.getUpdateType() == UpdateType.KEY_RECEIVED_UPDATE) {
                diraKeyProtocol.onIntermediateKey((KeyReceivedUpdate) update);
                System.out.println("KeyReceived update - " + message);
                // roomUpdatesProcessor.updateRoom(update);
            } else if (update.getUpdateType() == UpdateType.RENEWING_CANCEL) {
                diraKeyProtocol.onKeyCancel((RenewingCancelUpdate) update);
                System.out.println("Cancel update - " + message);
                roomUpdatesProcessor.updateRoom(update);
            } else if (update.getUpdateType() == UpdateType.RENEWING_CONFIRMED) {
                diraKeyProtocol.onKeyConfirmed((RenewingConfirmUpdate) update);
                System.out.println("Confirmed update - " + message);
                roomUpdatesProcessor.updateRoom(update);
            } else if (update.getUpdateType() == UpdateType.PING_UPDATE) {
                PingUpdate pingUpdate = (PingUpdate) update;
                String roomSecret = pingUpdate.getRoomSecret();

                CacheUtils cacheUtils = new CacheUtils(context);
                String id = cacheUtils.getString(CacheUtils.ID);
                String nickname = cacheUtils.getString(CacheUtils.NICKNAME);

                BaseMember baseMember = new BaseMember(id, nickname);
                PingReactRequest request = new PingReactRequest(roomSecret, baseMember);
                sendRequest(request, address);
            } else if (update.getUpdateType() == UpdateType.READ_UPDATE) {
                roomUpdatesProcessor.updateRoom(update);
            }

            notifyUpdateListeners(update);
        } catch (OldUpdateException oldUpdateException) {
            oldUpdateException.printStackTrace();
        } catch (UnablePerformRequestException e) {
            e.printStackTrace();
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

    public void notifyUpdateListeners(Update update) {
        for (UpdateListener updateListener : updateListeners) {
            try {
                updateListener.onUpdate(update);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public RoomUpdatesProcessor getRoomUpdatesProcessor() {
        return roomUpdatesProcessor;
    }

    public ClientMessageProcessor getClientMessageProcessor() {
        return clientMessageProcessor;
    }
}
