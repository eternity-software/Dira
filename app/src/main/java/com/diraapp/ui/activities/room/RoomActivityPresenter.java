package com.diraapp.ui.activities.room;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.abedelazizshe.lightcompressorlibrary.config.AppSpecificStorageConfiguration;
import com.abedelazizshe.lightcompressorlibrary.config.Configuration;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.SendMessageRequest;
import com.diraapp.api.requests.SendUserStatusRequest;
import com.diraapp.api.updates.AttachmentListenedUpdate;
import com.diraapp.api.updates.MessageReadUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.PinnedMessageAddedUpdate;
import com.diraapp.api.updates.PinnedMessageRemovedUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.api.userstatus.UserStatus;
import com.diraapp.api.userstatus.UserStatusHandler;
import com.diraapp.api.userstatus.UserStatusListener;
import com.diraapp.api.views.UserStatusType;
import com.diraapp.db.daos.MessageDao;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.MessageReading;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.entities.rooms.RoomType;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
import com.diraapp.storage.images.FilesUploader;
import com.diraapp.storage.images.ImageCompressor;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.adapters.messages.legacy.MessageReplyListener;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.components.viewswiper.ViewSwiperListener;
import com.diraapp.utils.EncryptionUtil;
import com.diraapp.utils.Logger;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RoomActivityPresenter implements RoomActivityContract.Presenter, UpdateListener,
        ViewSwiperListener, MessageReplyListener {


    private static final int MAX_ADAPTER_MESSAGES_COUNT = 200;
    private final String roomSecret;
    private final String selfId;
    private ArrayList<Message> pinnedMessages = new ArrayList<>();
    private HashSet<String> pinnedIds = new HashSet<>();
    private List<Message> messageList = new ArrayList<>();
    private RoomActivityContract.View view;
    private UserStatus currentUserStatus;
    private Room room;
    private boolean isNewestMessagesLoaded = false;

    private Message replyingMessage = null;

    private HashMap<String, Member> members = new HashMap<>();

    private Message clickedRepliedMessage = null;

    public RoomActivityPresenter(String roomSecret, String selfId) {
        this.roomSecret = roomSecret;
        this.selfId = selfId;
    }

    private static void clearJunkAfterCompression(Context context) {
        // Delete junk after this buggy library finishes its work
        // New version with fixes not released and it's not supporting sdk 21
        // TODO: Make our own compression (maybe fork of compression library with fixes)
        for (File file : new File(context.getApplicationInfo().dataDir).listFiles()) {
            if (file.isFile()) file.delete();
        }
    }

    @Override
    public void onUpdate(Update update) {
        Logger.logDebug("RoomActivityPresenter", "New update - " + update.getUpdateType());

        if (update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE) {
            NewMessageUpdate newMessageUpdate = (NewMessageUpdate) update;
            if (!newMessageUpdate.getMessage().getRoomSecret().equals(roomSecret)) return;

            Message message = newMessageUpdate.getMessage();

            boolean needUpdateList = true;
            if (room.getLastMessageId() != null) {
                if (messageList.size() > 0) {
                    needUpdateList = messageList.get(0).getId().
                            equals(room.getLastMessageId());
                }
            }

            if (needUpdateList) {
                messageList.add(0, message);
            }

            room.setLastMessageId(message.getId());
            room.setLastUpdatedTime(message.getTime());

            view.notifyRecyclerMessage(newMessageUpdate.getMessage(), needUpdateList);

            if (message.hasAuthor()) {
                if (message.getAuthorId().equals(selfId)) {
                    room.getUnreadMessagesIds().clear();
                } else {
                    room.getUnreadMessagesIds().add(message.getId());
                }

                view.updateScrollArrow();
                view.updateScrollArrowIndicator();

            }
        } else if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;
            initRoomInfo();
        } else if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;
            view.runBackground(this::initMembers);

            if (room.getRoomType() == RoomType.PRIVATE) initRoomInfo();
        } else if (update.getUpdateType() == UpdateType.READ_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;
            Logger.logDebug("ReadingDebug", "2");
            MessageReadUpdate readUpdate = (MessageReadUpdate) update;

            Message thisMessage = null;
            int index = 0;

            for (int i = 0; i < messageList.size(); i++) {
                Message message = messageList.get(i);
                if (message.getId().equals(readUpdate.getMessageId())) {
                    thisMessage = message;
                    index = i;
                    break;
                }
            }
            if (thisMessage == null) return;

            onMessageRead(thisMessage, index, readUpdate.getUserId(),
                    readUpdate.getReadTime());
        } else if (update.getUpdateType() == UpdateType.ATTACHMENT_LISTENED_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;
            AttachmentListenedUpdate listenedUpdate = (AttachmentListenedUpdate) update;

            onAttachmentListenedUpdate(listenedUpdate);
        } else if (update.getUpdateType() == UpdateType.PINNED_MESSAGE_ADDED_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;
            addPinned((PinnedMessageAddedUpdate) update);
        } else if (update.getUpdateType() == UpdateType.PINNED_MESSAGE_REMOVED_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;
            removePinned((PinnedMessageRemovedUpdate) update);
        }
    }

    @Override
    public void attachView(RoomActivityContract.View view) {
        this.view = view;
        UpdateProcessor.getInstance().addUpdateListener(this);
    }

    @Override
    public void detachView() {
        UpdateProcessor.getInstance().removeUpdateListener(this);
        //this.view = null;
    }

    @Override
    public void initRoomInfo() {
        view.runBackground(() -> {
            room = view.getRoomDatabase().getRoomDao().getRoomBySecretName(roomSecret);
            room.setUpdatedRead(true);
            view.getRoomDatabase().getRoomDao().update(room);

            Bitmap roomPicture = null;
            if (room.getImagePath() != null) {
                roomPicture = AppStorage.getBitmapFromPath(room.getImagePath());
            }

            view.fillRoomInfo(roomPicture, room);

        });
    }

    @Override
    public void initMembers() {
        List<Member> memberList = view.getRoomDatabase().getMemberDao().getMembersByRoomSecret(roomSecret);

        members = new HashMap<>();

        for (Member member : memberList) {
            members.put(member.getId(), member);
        }

        ArrayList<UserStatus> userStatusList = UserStatusHandler.getInstance().
                getUserStatuses(roomSecret);

        ((UserStatusListener) view).updateRoomStatus(roomSecret, userStatusList);

    }

    @Override
    public void loadMessagesBefore(Message message, int index) {
        view.runBackground(() -> {
            try {
                MessageDao messageDao = view.getMessagesDatabase().getMessageDao();
                List<Message> oldMessages = messageDao.getBeforeMessagesInRoom(roomSecret, message.getTime());
                if (oldMessages.size() == 0) return;
                int notifyingIndex = messageList.size() - 1;

                loadReplies(oldMessages, messageDao);

                boolean isMessagesRemoved = messageList.size() > MAX_ADAPTER_MESSAGES_COUNT;

                view.runOnUiThread(() -> {
                    messageList.addAll(oldMessages);
                    view.notifyMessageInsertedWithoutScroll(
                            index + 1, index + oldMessages.size());

                    Message upperMessage = null;
                    Message bottomMessage = messageList.get(notifyingIndex);
                    if (messageList.size() > notifyingIndex + 1) {
                        upperMessage = messageList.get(notifyingIndex + 1);
                    }
                    view.notifyViewHolderUpdateTimeAndPicture(notifyingIndex, bottomMessage, upperMessage);
                    //view.notifyAdapterItemChanged(notifyingIndex);

                    if (isMessagesRemoved) {
                        messageList.subList(0, MessageDao.LOADING_COUNT).clear();
                        view.notifyAdapterItemsDeleted(0, MessageDao.LOADING_COUNT);
                        isNewestMessagesLoaded = false;
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public void loadNewerMessage(Message message, int index) {
        view.runBackground(() -> {
            if (messageList.size() == 0) return;
            if (isNewestMessagesLoaded) return;

            try {
                MessageDao messageDao = view.getMessagesDatabase().getMessageDao();

                Message newestLoaded = messageList.get(0);

                // Collect all massages at last millisecond loaded


                List<Message> newMessages = messageDao.getNewerMessages(roomSecret,
                        newestLoaded.getTime());

                if (newMessages.size() == 0) {
                    isNewestMessagesLoaded = true;
                    return;
                }

                loadReplies(newMessages, messageDao);
                isNewestMessagesLoaded = messageList.get(0).getId().equals(room.getLastMessageId());

                boolean isMessagesRemoved = messageList.size() > MAX_ADAPTER_MESSAGES_COUNT;
                int size = messageList.size();
                view.runOnUiThread(() -> {
                            for (Message m : newMessages) {
                                messageList.add(0, m);
                            }
                            view.notifyMessagesInserted(0, newMessages.size(), newMessages.size());

                            if (isMessagesRemoved) {
                                messageList.subList(size - MessageDao.LOADING_COUNT + 1, size).clear();
                                view.notifyAdapterItemsDeleted(size - MessageDao.LOADING_COUNT,
                                        MessageDao.LOADING_COUNT);
                            }
                        }
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void sendStatus(UserStatusType userStatusType) {
        if (currentUserStatus != null) {
            if (userStatusType == currentUserStatus.getUserStatus()) {
                if (System.currentTimeMillis() - currentUserStatus.getTime() < UserStatus.REQUEST_DELAY)
                    return;
            }
        }

        currentUserStatus = new UserStatus(userStatusType, selfId, roomSecret);
        SendUserStatusRequest request = new SendUserStatusRequest(currentUserStatus);
        currentUserStatus.setTime(System.currentTimeMillis());

        try {
            UpdateProcessor.getInstance().sendRequest(request, room.getServerAddress());
        } catch (UnablePerformRequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadMessagesAtRoomStart() {
        view.runBackground(() -> {
            MessageDao messageDao = view.getMessagesDatabase().getMessageDao();

            boolean loadBottom = true;
            int scrollTo;
            String messageId;

            int size = room.getUnreadMessagesIds().size();
            if (size == 0) {
                if (!room.getFirstVisibleScrolledItemId().equals("")) {
                    messageId = room.getFirstVisibleScrolledItemId();
                    Message message = messageDao.getMessageById(messageId);

                    if (message != null) {
                        loadBottom = false;
                        loadMessagesNearByTime(message.getTime());
                    }
                }

            } else {
                messageId = room.getUnreadMessagesIds().get(0);
                Message message = messageDao.getMessageById(messageId);

                if (message != null) {
                    loadBottom = false;
                    messageList = messageDao.getBeforePartOnRoomLoading(roomSecret, message.getTime());

                    List<Message> newerPart = messageDao.getNewerPartOnRoomLoading(roomSecret,
                            message.getTime());

                    Collections.reverse(newerPart);
                    messageList.addAll(0, newerPart);

                    if (size < MessageDao.LOADING_COUNT_HALF) {
                        scrollTo = size - 1;
                    } else {
                        scrollTo = MessageDao.LOADING_COUNT_HALF - 1;
                    }

                    loadReplies(messageList, messageDao);
                    view.setMessages(messageList);

                    int finalScrollTo = scrollTo;
                    view.runOnUiThread(() -> {
                        view.notifyOnRoomOpenMessagesLoaded(finalScrollTo);
                    });
                }

            }

            if (loadBottom) {
                loadRoomBottomMessages();
            }

            initMembers();

            loadPinnedMessages();
        });

    }

    private void loadPinnedMessages() {
        pinnedIds = new HashSet<>(room.getPinnedMessagesIds().size());
        pinnedMessages = new ArrayList<>(room.getPinnedMessagesIds().size());

        MessageDao messageDao = view.getMessagesDatabase().getMessageDao();

        for (String id : room.getPinnedMessagesIds()) {
            Message message = messageDao.getMessageAndAttachmentsById(id);

            if (message == null) continue;

            pinnedMessages.add(message);
            pinnedIds.add(id);
        }

        view.runOnUiThread(() -> {
            view.showLastPinned(false);
        });
    }

    @Override
    public void loadRoomBottomMessages() {

        MessageDao messageDao = view.getMessagesDatabase().getMessageDao();
        messageList = messageDao.getLatestMessagesInRoom(roomSecret);
        loadReplies(messageList, messageDao);

        view.runOnUiThread(() -> {
            view.setMessages(messageList);
            view.notifyOnRoomOpenMessagesLoaded(0);
        });

        isNewestMessagesLoaded = true;
    }

    @Override
    public void loadMessagesNearByTime(long time) {
        loadMessagesNearByTime(time, false);
    }

    @Override
    public void loadMessagesNearByTime(long time, boolean needBlink) {
        view.runBackground(() -> {
            MessageDao messageDao = view.getMessagesDatabase().getMessageDao();
            List<Message> olderMessages = messageDao.getBeforePartOnRoomLoading(roomSecret, time);
            List<Message> messages = messageDao.getNewerPartOnRoomLoading(roomSecret, time);

            Collections.reverse(messages);
            messages.addAll(olderMessages);

            int scrollTo = 0;
            for (int i = 0; i < messages.size(); i++) {
                Message m = messages.get(i);
                if (m.getTime() == time) {
                    scrollTo = i;
                    break;
                }
            }

            messageList = messages;
            loadReplies(messages, messageDao);
            int finalScrollTo = scrollTo;
            view.runOnUiThread(() -> {
                view.setMessages(messageList);
                view.notifyOnRoomOpenMessagesLoaded(finalScrollTo);

                if (needBlink) view.blinkViewHolder(finalScrollTo);
            });

            isNewestMessagesLoaded = messages.get(0).getId().equals(room.getLastMessageId());
        });
    }

    @Override
    public void scrollToMessage(String messageId, long messageTime) {
        scrollToMessage(messageId, messageTime, true);
    }

    @Override
    public void scrollToMessage(String messageId, long messageTime, boolean blink) {
        if (messageId == null) return;
        int messageIndex = getMessagePos(messageId);

        if (messageIndex != -1) {
            if (blink) {
                if (view.isMessageVisible(messageIndex)) {
                    view.blinkViewHolder(messageIndex);
                } else {
                    view.addMessageToBlinkId(messageId);
                }
            }

            view.scrollToAndStop(messageIndex);
            return;
        }

        if (blink) view.addMessageToBlinkId(messageId);
        loadMessagesNearByTime(messageTime, true);
    }

    @Override
    public void blinkMessage(Message message) {
//        if (message == null) return;
//        int messageIndex = getMessagePos(message);
//
//        if (messageIndex != -1) {
//            view.blinkViewHolder(messageIndex);
//        }
    }

    private int getMessagePos(String messageId) {
        int messageIndex = -1;

        for (int i = 0; i < messageList.size(); i++) {
            Message m = messageList.get(i);
            if (m.getId().equals(messageId)) {
                messageIndex = i;
                break;
            }
        }

        return messageIndex;
    }

    private void loadReplies(List<Message> messages, MessageDao messageDao) {
        HashMap<String, Message> messageHashMap = new HashMap<>(messages.size());

        for (Message m : messageList) {
            messageHashMap.put(m.getId(), m);
        }

        for (Message message : messages) {
            if (message.getRepliedMessageId() == null) continue;
            Message repliedMessage;

            repliedMessage = messageHashMap.get(message.getRepliedMessageId());

            if (repliedMessage == null) {
                repliedMessage = messageDao.getMessageAndAttachmentsById(
                        message.getRepliedMessageId());
            }

            if (repliedMessage != null) message.setRepliedMessage(repliedMessage);
        }
    }

    public boolean sendTextMessage(String text) {
        while (text.contains("   ")) {
            text = text.replace("   ", " ");
        }

        while (text.contains("\n\n\n")) {
            text = text.replace("\n\n\n", "\n");
        }

        if (text.replace(" ", "").replace("\n", "").length() != 0) {
            Message message = Message.generateMessage(view.getCacheUtils(), roomSecret);
            message.setLastTimeEncryptionKeyUpdated(room.getTimeEncryptionKeyUpdated());

            if (text.length() >= 2) {
                if (text.charAt(0) == ' ') {
                    text = text.substring(1);
                }

                if (text.charAt(text.length() - 1) == ' ') {
                    text = text.substring(0, text.length() - 1);
                }
            }

            final String replyId = getAndClearReplyId();
            message.setRepliedMessageId(replyId);

            if (room.getEncryptionKey().equals("")) {
                message.setText(text);
            } else {
                message.setText(EncryptionUtil.encrypt(text, room.getEncryptionKey()));
            }

            try {
                sendMessage(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    @Override
    public void uploadAttachment(AttachmentType attachmentType,
                                 AttachmentReadyListener attachmentReadyListener, String fileUri,
                                 DiraActivity context) {

        DiraActivity.runGlobalBackground(() -> {
            Logger.logDebug(this.getClass().getSimpleName(),
                    "Uploading started.. ");

            boolean isVideo = attachmentType == AttachmentType.VIDEO
                    || attachmentType == AttachmentType.BUBBLE;
            if (isVideo) isVideo = isVideo && FileClassifier.isVideoFile(fileUri);

            if (isVideo) {
                List<Uri> urisToCompress = new ArrayList<>();
                urisToCompress.add(Uri.fromFile(new File(fileUri)));
                Logger.logDebug(this.getClass().getSimpleName(),
                        "Compression started.. ");

                Double videoHeight = null;
                Double videoWidth = null;

                VideoQuality videoQuality = VideoQuality.MEDIUM;

                if (attachmentType == AttachmentType.BUBBLE) {
                    videoHeight = 340D;
                    videoWidth = 340D;

                }


                compressVideo(urisToCompress, fileUri, videoQuality,
                        videoHeight, videoWidth,
                        new AttachmentHandler(null, attachmentReadyListener,
                                attachmentType),
                        room.getServerAddress(), room.getEncryptionKey(), context);

            } else {
                boolean compressImage = attachmentType == AttachmentType.IMAGE;
                boolean deleteIfFile = attachmentType == AttachmentType.FILE;
                uploadFile(fileUri,
                        new AttachmentHandler(fileUri, attachmentReadyListener, attachmentType),
                        deleteIfFile,
                        room.getServerAddress(),
                        room.getEncryptionKey(),
                        compressImage, context);
            }
        });

    }

    private void uploadFile(String sourceFileUri, RoomActivityPresenter.AttachmentHandler callback,
                            boolean deleteAfterUpload, String serverAddress, String encryptionKey,
                            boolean compressImage, DiraActivity context) {
        try {
            if (FileClassifier.isImageFile(sourceFileUri) && compressImage) {
                ImageCompressor.compress(context, new File(sourceFileUri), new com.diraapp.storage.images.Callback() {
                    @Override
                    public void onComplete(boolean status, @Nullable File file) {
                        try {
                            FilesUploader.uploadFile(file.getPath(), callback, context, deleteAfterUpload, serverAddress, encryptionKey);
                        } catch (IOException e) {

                        }
                    }
                });
            } else {
                FilesUploader.uploadFile(sourceFileUri, callback, context, deleteAfterUpload, serverAddress, encryptionKey);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compressVideo(List<Uri> urisToCompress, String fileUri, VideoQuality videoQuality, Double videoHeight,
                              Double videoWidth, RoomActivityPresenter.AttachmentHandler callback, String serverAddress, String encryptionKey, Context context) {
        VideoCompressor.start(context, urisToCompress,
                false,
                null,
                new AppSpecificStorageConfiguration(new File(fileUri).getName() + "temp_compressed", null),
                new Configuration(videoQuality,
                        false,
                        6,
                        false,
                        false,
                        videoHeight,
                        videoWidth), new CompressionListener() {
                    @Override
                    public void onStart(int i) {

                    }

                    @Override
                    public void onSuccess(int i, long l, @Nullable String path) {

                        clearJunkAfterCompression(context);


                        if (path != null) {
                            try {


                                FilesUploader.uploadFile(path,
                                        callback.setFileUri(path),
                                        context, true,
                                        serverAddress, encryptionKey);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                        Logger.logDebug(this.getClass().getSimpleName(),
                                "Compression failed: " + s);
                        clearJunkAfterCompression(context);

                    }

                    @Override
                    public void onProgress(int i, float v) {
                        Logger.logDebug(this.getClass().getSimpleName(),
                                "Compression progress: " + i + " " + v);
                    }

                    @Override
                    public void onCancelled(int i) {
                        Logger.logDebug(this.getClass().getSimpleName(),
                                "Compression cancelled: " + i);
                        clearJunkAfterCompression(context);

                    }
                });
    }

    @Override
    public void sendMessage(ArrayList<Attachment> attachments, String messageText, String replyId) {
        if (replyId.equals("")) {
            replyId = null;
        }

        Message message = Message.generateMessage(view.getCacheUtils(), roomSecret);
        message.setRepliedMessageId(replyId);

        message.setLastTimeEncryptionKeyUpdated(room.getTimeEncryptionKeyUpdated());

        if (room.getEncryptionKey().equals("")) {
            message.setText(messageText);
        } else {
            message.setText(EncryptionUtil.encrypt(messageText, room.getEncryptionKey()));
        }

        message.setAttachments(attachments);

        try {
            sendMessage(message);
        } catch (UnablePerformRequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateDynamicRoomFields() {
        RoomDao roomDao = view.getRoomDatabase().getRoomDao();

        view.runBackground(() -> {
            Room dbRoom = roomDao.getRoomBySecretName(roomSecret);

            dbRoom.setFirstVisibleScrolledItemId(room.getFirstVisibleScrolledItemId());
            dbRoom.setUnsentText(room.getUnsentText());
            roomDao.update(dbRoom);
        });
    }

    private void onMessageRead(Message thisMessage, int index, String userId, long readTime) {
        onMessageRead(thisMessage, index, userId, readTime, false);
    }

    private void onMessageRead(Message thisMessage, int index, String userId, long readTime,
                               boolean isListened) {

        Logger.logDebug("ReadingDebug", "3");

        if (userId.equals(selfId)) {

            int pos = room.getUnreadMessagesIds().indexOf(thisMessage.getId());

            ArrayList<String> toDelete = new ArrayList<>();
            if (pos != -1) {
                for (int i = 0; i <= pos; i++) {
                    toDelete.add(room.getUnreadMessagesIds().get(i));
                }
                room.removeFromUnreadMessages(toDelete);
            }

            view.updateScrollArrow();
            view.updateScrollArrowIndicator();
            return;
        }
        Logger.logDebug("ReadingDebug", "4");
        MessageReading thisReading = new MessageReading(userId,
                readTime);
        thisReading.setHasListened(isListened);

        thisMessage.addReadingIfAvailable(thisReading);
        Logger.logDebug("ReadingDebug", "5");

        view.notifyRecyclerMessageRead(thisMessage, index);
    }

    private void onAttachmentListenedUpdate(AttachmentListenedUpdate listenedUpdate) {

        Message thisMessage = null;
        int index = 0;

        for (int i = 0; i < messageList.size(); i++) {
            Message message = messageList.get(i);
            if (message.getId().equals(listenedUpdate.getMessageId())) {
                thisMessage = message;
                index = i;
                break;
            }
        }
        if (thisMessage == null) return;

        if (!thisMessage.hasAuthor()) return;
        if (thisMessage.getAuthorId().equals(listenedUpdate.getUserId())) return;
        if (thisMessage.getAttachments().size() == 0) return;

        Attachment attachment = thisMessage.getSingleAttachment();
        boolean isSelf = listenedUpdate.getUserId().equals(selfId);

        if (isSelf) {
            attachment.setListened(true);
        } else {
            if (thisMessage.getAuthorId().equals(selfId)) {
                attachment.setListened(true);
            }
        }

        // notify ViewHolder
        Message finalThisMessage = thisMessage;
        view.notifyViewHolder(index, (BaseMessageViewHolder holder) -> {
            holder.updateListeningIndicator(finalThisMessage.getSingleAttachment());
        });

        if (isSelf) return;

        MessageReading messageReading = null;
        for (MessageReading mr : thisMessage.getMessageReadingList()) {
            if (mr.getUserId().equals(listenedUpdate.getUserId())) {
                messageReading = mr;
                break;
            }
        }

        if (messageReading != null) {
            messageReading.setHasListened(true);
        } else {
            onMessageRead(thisMessage, index, listenedUpdate.getUserId(),
                    System.currentTimeMillis(), true);
        }

    }

    @Override
    public void deleteMessage(Message message, Context context) {
        view.runBackground(() -> {
            if (message == null) return;

            int pos = messageList.indexOf(message);
            if (pos == -1) {
                Logger.logDebug(RoomActivityPresenter.class.toString(),
                        "Message to remove not found if list");
                return;
            }
            messageList.remove(pos);

            view.runOnUiThread(() -> {
                view.notifyAdapterItemRemoved(pos);

                if (replyingMessage != null) {
                    if (replyingMessage.getId().equals(message.getId())) {
                        view.setReplyMessage(null);
                    }
                }

                if (pos == 0) return;

                Message upperMessage = null;
                Message bottomMessage = messageList.get(pos - 1);
                if (messageList.size() > pos) {
                    upperMessage = messageList.get(pos);
                }

                view.notifyViewHolderUpdateTimeAndPicture(pos - 1, bottomMessage, upperMessage);

            });

            for (Attachment attachment : message.getAttachments()) {
                AppStorage.deleteAttachment(context, attachment, message.getRoomSecret());
            }

            String messageId = message.getId();

            RoomDao roomDao = view.getRoomDatabase().getRoomDao();
            Room dbRoom = roomDao.getRoomBySecretName(roomSecret);

            boolean updateRoom = false;
            if (room.getLastMessageId().equals(messageId)) {
                updateRoom = true;
                String newLastMessageId = null;
                room.setMessage(null);
                if (messageList.size() > 0) {
                    room.setMessage(messageList.get(0));
                    newLastMessageId = room.getMessage().getId();
                }
                room.setLastMessageId(newLastMessageId);
                dbRoom.setLastMessageId(newLastMessageId);
            }

            if (room.getFirstVisibleScrolledItemId().equals(messageId)) {
                updateRoom = true;
                String newLastScrolledMessageId = null;

                if (messageList.size() > 0) {

                    final int newPos;
                    if (pos == 0) newPos = pos;
                    else newPos = pos - 1;

                    room.setMessage(messageList.get(newPos));
                    newLastScrolledMessageId = room.getMessage().getId();
                }
                room.setFirstVisibleScrolledItemId(newLastScrolledMessageId);
                dbRoom.setFirstVisibleScrolledItemId(newLastScrolledMessageId);
            }

            if (room.getUnreadMessagesIds().contains(messageId)) {
                updateRoom = true;

                room.getUnreadMessagesIds().remove(messageId);
                dbRoom.getUnreadMessagesIds().remove(messageId);
            }

            if (pinnedIds.contains(messageId)) {
                updateRoom = true;

                removePinned(new PinnedMessageRemovedUpdate(
                        roomSecret, messageId, message.getAuthorId()));
                dbRoom.getPinnedMessagesIds().remove(messageId);
            }

            if (updateRoom) {
                roomDao.update(dbRoom);
            }

            // Removing message from db
            view.getMessagesDatabase().getMessageDao().delete(message);
        });
    }

    @Override
    public ArrayList<Message> getPinnedMessages() {
        return pinnedMessages;
    }

    @Override
    public boolean isPinned(String messageId) {
        return pinnedIds.contains(messageId);
    }

    @Override
    public void addPinned(PinnedMessageAddedUpdate update) {
        if (pinnedIds.contains(update.getMessageId())) return;


        view.runBackground(() -> {
            Message message = view.getMessagesDatabase().getMessageDao().
                    getMessageAndAttachmentsById(update.getMessageId());

            if (message == null) return;
            pinnedIds.add(update.getMessageId());

            pinnedMessages.add(message);

            room.getPinnedMessagesIds().add(update.getMessageId());

            view.runOnUiThread(() -> {
                view.showLastPinned(true);
            });
        });
    }

    @Override
    public void removePinned(PinnedMessageRemovedUpdate update) {
        if (!pinnedIds.contains(update.getMessageId())) return;

        pinnedIds.remove(update.getMessageId());

        Message message = null;
        for (Message m : pinnedMessages) {
            if (m.getId().equals(update.getMessageId())) {
                message = m;
                break;
            }
        }

        if (message == null) return;
        pinnedMessages.remove(message);

        room.getPinnedMessagesIds().remove(update.getMessageId());

        view.showLastPinned(true);
    }

    @Override
    public void sendFileAttachmentMessage(ArrayList<Uri> uris, String messageText, DiraActivity context) {
        for (Uri uri : uris) {
            File file = AppStorage.copyFile(context, uri);
            if (file == null) {
                Logger.logDebug(this.getClass().getSimpleName(), "On result: file = null");
                return;
            }
            String path = file.getAbsolutePath();

            sendStatus(UserStatusType.SENDING_FILE);
            Logger.logDebug(this.getClass().getSimpleName(), "On result: File Path: " + path);

            ArrayList<Attachment> attachments = new ArrayList<>();
            final String replyId = getAndClearReplyId();
            final String currentText = messageText;
            RoomActivityPresenter.AttachmentReadyListener attachmentReadyListener = attachment -> {
                attachments.add(attachment);
                sendMessage(attachments, currentText, replyId);
            };

            uploadAttachment(AttachmentType.FILE, attachmentReadyListener, path, context);

            messageText = "";
        }
    }

    @Override
    public void onSwiped(int position) {
        replyingMessage = messageList.get(position);

        view.setReplyMessage(replyingMessage);
    }

    @Override
    public boolean denySwipe(RecyclerView.ViewHolder viewHolder) {
        if (!(viewHolder instanceof BaseMessageViewHolder)) return false;

        BaseMessageViewHolder baseMessageViewHolder = (BaseMessageViewHolder) viewHolder;
        return !baseMessageViewHolder.canBeSwiped();
    }

    @Override
    public void sendMessage(Message message) throws UnablePerformRequestException {

        SendMessageRequest sendMessageRequest = new SendMessageRequest(message, room.getUpdateExpireSec());

        UpdateProcessor.getInstance().sendRequest(sendMessageRequest, room.getServerAddress());

    }

    @Override
    public void setReplyingMessage(Message message) {
        replyingMessage = message;
    }

    @Override
    public void onScrollArrowPressed(int newestVisible) {
        view.runBackground(() -> {

            if (messageList.size() == 0) return;

            Message message = messageList.get(newestVisible);
            if (clickedRepliedMessage != null) {
                if (message.getTime() >= clickedRepliedMessage.getTime()) {
                    clickedRepliedMessage = null;
                    Logger.logDebug("fsdfsdf", "sdfsdfsdfs / " + message.getText());
                }
            }

            Message messageToScroll = null;
            if (clickedRepliedMessage != null) {
                messageToScroll = clickedRepliedMessage;
                clickedRepliedMessage = null;
            } else {
                final String id;
                if (room.getUnreadMessagesIds().size() > 0) {
                    id = room.getUnreadMessagesIds().get(0);
                } else {
                    id = room.getLastMessageId();
                }

                for (int i = 0; i < messageList.size(); i++) {
                    Message m = messageList.get(i);
                    if (m.getId().equals(id)) {
                        messageToScroll = m;
                        break;
                    }
                }

                if (messageToScroll == null) {
                    messageToScroll = view.getMessagesDatabase().getMessageDao().getMessageById(id);
                }
            }

            Message finalMessageToScroll = messageToScroll;
            view.runOnUiThread(() -> {
                scrollToMessage(finalMessageToScroll.getId(), finalMessageToScroll.getTime(), false);
            });
        });
    }

    @Override
    public String getAndClearReplyId() {
        if (replyingMessage == null) {
            return "";
        }
        String replyId = replyingMessage.getId();
        view.setReplyMessage(null);
        return replyId;
    }

    @Override
    public void onReplyClicked(Message message, Message holderMessage) {
        scrollToMessage(message.getId(), message.getTime());
        clickedRepliedMessage = holderMessage;
    }

    @Override
    public Room getRoom() {
        return room;
    }

    @Override
    public Message getMessageByPosition(int pos) {
        if (pos >= messageList.size()) return null;
        if (pos == -1) return null;
        return messageList.get(pos);
    }

    @Override
    public int getItemsCount() {
        return messageList.size();
    }

    @Override
    public boolean isNewestMessagesLoaded() {
        return isNewestMessagesLoaded;
    }

    @Override
    public HashMap<String, Member> getMembers() {
        return members;
    }

    public interface AttachmentReadyListener {
        void onReady(Attachment attachment);
    }

    public class AttachmentHandler implements Callback {
        private final AttachmentType attachmentType;
        private final AttachmentReadyListener attachmentReadyListener;
        private int height;
        private int width;
        private String fileUri;
        private long fileSize;

        public AttachmentHandler(String fileUri, AttachmentReadyListener attachmentReadyListener, AttachmentType attachmentType) {
            this.fileUri = fileUri;
            this.attachmentType = attachmentType;
            this.attachmentReadyListener = attachmentReadyListener;
        }

        public AttachmentType getAttachmentType() {
            return attachmentType;
        }

        public int getHeight() {
            return height;
        }

        public void setWidthAndHeight(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public AttachmentHandler setFileUri(String fileUri) {
            this.fileUri = fileUri;
            fileSize = new File(fileUri).length();
            return this;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            Logger.logDebug(this.getClass().getSimpleName(),
                    "Uploading started.. ");

            try {
                String fileTempName = new JSONObject(response.body().string()).getString("message");
                Attachment attachment = new Attachment();


                attachment.setAttachmentType(attachmentType);

                attachment.setFileCreatedTime(System.currentTimeMillis());
                attachment.setFileName("attachment");
                Logger.logDebug(this.getClass().getSimpleName(),
                        "Uploaded! Url " + fileTempName);
                attachment.setFileUrl(fileTempName);


                attachment.setSize(new File(fileUri).length());


                if (attachment.getSize() == 0) attachment.setSize(fileSize);

                if (attachment.getAttachmentType() == AttachmentType.IMAGE ||
                        attachment.getAttachmentType() == AttachmentType.BUBBLE ||
                        attachment.getAttachmentType() == AttachmentType.VIDEO) {

                    String previewUri = fileUri;
                    Bitmap bitmap = null;

                    if (attachment.getAttachmentType() == AttachmentType.VIDEO ||
                            attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                        bitmap = ThumbnailUtils.createVideoThumbnail(previewUri, MediaStore.Video.Thumbnails.MICRO_KIND);
                    } else if (attachment.getAttachmentType() == AttachmentType.IMAGE) {
                        bitmap = view.getBitmap(previewUri);
                    }


                    if (bitmap != null) {
                        float scaleFactor = bitmap.getHeight() / (float) getWidth();

                        if (scaleFactor < 0.3) scaleFactor = 0.3f;
                        if (scaleFactor > 4) scaleFactor = 4f;
                        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (10 * scaleFactor), 10, true);
                    }
                    attachment.setImagePreview(AppStorage.getBase64FromBitmap(bitmap));
                }


                if (attachment.getAttachmentType() == AttachmentType.VIDEO ||
                        attachment.getAttachmentType() == AttachmentType.IMAGE) {
                    attachment.setHeight(this.height);
                    attachment.setWidth(this.width);
                }

                if (attachment.getAttachmentType() == AttachmentType.FILE) {
                    String[] strings = fileUri.split("/");
                    String name = strings[strings.length - 1];
                    attachment.setDisplayFileName(name);
                }

                attachmentReadyListener.onReady(attachment);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
