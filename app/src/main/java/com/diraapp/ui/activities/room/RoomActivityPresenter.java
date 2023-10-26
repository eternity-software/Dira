package com.diraapp.ui.activities.room;


import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.SendMessageRequest;
import com.diraapp.api.requests.SendUserStatusRequest;
import com.diraapp.api.updates.MessageReadUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.api.userstatus.UserStatus;
import com.diraapp.api.userstatus.UserStatusHandler;
import com.diraapp.api.userstatus.UserStatusListener;
import com.diraapp.api.views.UserStatusType;
import com.diraapp.db.daos.MessageDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.MessageReading;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.adapters.messages.legacy.MessageReplyListener;
import com.diraapp.ui.components.viewswiper.ViewSwiperListener;
import com.diraapp.utils.EncryptionUtil;
import com.diraapp.utils.Logger;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RoomActivityPresenter implements RoomActivityContract.Presenter, UpdateListener,
        ViewSwiperListener, MessageReplyListener {


    private static final int MAX_ADAPTER_MESSAGES_COUNT = 200;
    private final String roomSecret;
    private final String selfId;

    private List<Message> messageList;
    private RoomActivityContract.View view;
    private UserStatus currentUserStatus;
    private Room room;
    private boolean isNewestMessagesLoaded = false;

    private Message replyingMessage = null;

    private HashMap<String, Member> members = new HashMap<>();

    private Message lastReadMessage;

    public RoomActivityPresenter(String roomSecret, String selfId) {
        this.roomSecret = roomSecret;
        this.selfId = selfId;
    }

    @Override
    public void onUpdate(Update update) {
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

            view.notifyRecyclerMessage(newMessageUpdate.getMessage(), needUpdateList);

            if (message.hasAuthor()) {
                if (message.getAuthorId().equals(selfId)) return;

                room.getUnreadMessagesIds().add(message.getId());
                view.updateScrollArrowIndicator();

            }
        } else if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
            initRoomInfo();
        } else if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
            view.runBackground(this::initMembers);
        } else if (update.getUpdateType() == UpdateType.READ_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;

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
            if (((MessageReadUpdate) update).getUserId().equals(selfId)) {

                if (lastReadMessage == null) lastReadMessage = thisMessage;
                else if (thisMessage.getTime() > lastReadMessage.getTime()) {
                    lastReadMessage = thisMessage;
                }

                int pos = room.getUnreadMessagesIds().indexOf(thisMessage.getId());

                ArrayList<String> toDelete = new ArrayList<>();
                if (pos != -1) {
                    for (int i = 0; i <= pos; i++) {
                        toDelete.add(room.getUnreadMessagesIds().get(i));
                    }
                    room.removeFromUnreadMessages(toDelete);
                }

                view.updateScrollArrowIndicator();
                return;
            }

            MessageReading thisReading = new MessageReading(readUpdate.getUserId(),
                    readUpdate.getReadTime());

            for (MessageReading reading : thisMessage.getMessageReadingList()) {
                if (reading.getUserId().equals(thisReading.getUserId())) return;
            }

            thisMessage.getMessageReadingList().add(thisReading);

            view.notifyRecyclerMessageRead(thisMessage, index);
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

                DiraActivity.runOnMainThread(() -> {
                    messageList.addAll(oldMessages);
                    view.notifyMessageInsertedWithoutScroll(
                            index + 1, index + oldMessages.size());

                    view.notifyAdapterItemChanged(notifyingIndex);

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
                DiraActivity.runOnMainThread(() -> {
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
            if (System.currentTimeMillis() - currentUserStatus.getTime() < UserStatus.REQUEST_DELAY)
                return;
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

            int scrollTo;

            int size = room.getUnreadMessagesIds().size();
            if (size == 0) {
                scrollTo = 0;
                loadRoomBottomMessages();
            } else {
                Message message = messageDao.getMessageById(room.getUnreadMessagesIds().get(0));

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
                DiraActivity.runOnMainThread(() -> {
                    view.notifyOnRoomOpenMessagesLoaded(scrollTo);
                });

            }

            initMembers();
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
                repliedMessage = messageDao.getMessageById(
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
    public void uploadAttachment(AttachmentType attachmentType, AttachmentReadyListener attachmentReadyListener, String fileUri) {

        view.runBackground(() -> {
            Logger.logDebug(this.getClass().getSimpleName(),
                    "Uploading started.. ");

            if (FileClassifier.isVideoFile(fileUri) && (attachmentType == AttachmentType.VIDEO
                    || attachmentType == AttachmentType.BUBBLE)) {
                List<Uri> urisToCompress = new ArrayList<>();
                urisToCompress.add(Uri.fromFile(new File(fileUri)));
                Logger.logDebug(this.getClass().getSimpleName(),
                        "Compression started.. ");

                int bitrate = 2;

                Double videoHeight = null;
                Double videoWidth = null;

                VideoQuality videoQuality = VideoQuality.VERY_LOW;

                if (attachmentType == AttachmentType.BUBBLE) {
                    videoHeight = 340D;
                    videoWidth = 340D;
                    bitrate = 1;
                }


                view.compressVideo(urisToCompress, fileUri, videoQuality,
                        videoHeight, videoWidth,
                        new AttachmentHandler(null, attachmentReadyListener,
                                attachmentType),
                        room.getServerAddress(), room.getEncryptionKey(), bitrate);

            } else {
                view.uploadFile(fileUri,
                        new AttachmentHandler(fileUri, attachmentReadyListener, attachmentType),
                        false,
                        room.getServerAddress(),
                        room.getEncryptionKey());
            }
        });

    }


    @Override
    public void sendMessage(ArrayList<Attachment> attachments, String messageText) {
        String replyId;
        if (replyingMessage != null) replyId = replyingMessage.getId();
        else {
            replyId = null;
        }

        view.setReplyMessage(null);
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
    public void onSwiped(int position) {
        replyingMessage = messageList.get(position);

        view.setReplyMessage(replyingMessage);
    }

    @Override
    public void sendMessage(Message message) throws UnablePerformRequestException {
        if (replyingMessage != null) {
            if (message.getRepliedMessageId() == null) {
                message.setRepliedMessageId(replyingMessage.getId());
            }
            view.setReplyMessage(null);
        }

        SendMessageRequest sendMessageRequest = new SendMessageRequest(message, room.getUpdateExpireSec());

        UpdateProcessor.getInstance().sendRequest(sendMessageRequest, room.getServerAddress());

    }

    @Override
    public void setReplyingMessage(Message message) {
        replyingMessage = message;
    }

    @Override
    public void onScrollArrowPressed() {
        view.runBackground(() -> {
            final int NOT_FOUND = -1;
            final int START_VAL = -2;
            int position = START_VAL;

            if (messageList.size() == 0) return;

            MessageDao messageDao = view.getMessagesDatabase().getMessageDao();
            if (lastReadMessage == null) {
                String lastReadMessageId;
                if (room.getUnreadMessagesIds().size() > 0) {
                    lastReadMessageId = room.getUnreadMessagesIds().get(0);
                } else {
                    lastReadMessageId = room.getLastMessageId();
                }

                for (int i = 0; i < messageList.size(); i++) {
                    Message m = messageList.get(i);
                    if (m.getId().equals(lastReadMessageId)) {
                        position = i;
                        lastReadMessage = m;
                        break;
                    }
                }

                if (position == START_VAL) {
                    lastReadMessage = messageDao.getMessageById(lastReadMessageId);
                    position = NOT_FOUND;
                }
            }

            if (position == START_VAL) {
                position = messageList.indexOf(lastReadMessage);
            }

            if (position == NOT_FOUND) {
                if (lastReadMessage == null) return;
                if (!lastReadMessage.getRoomSecret().equals(roomSecret)) return;
                loadMessagesNearByTime(lastReadMessage.getTime());
                return;
            }

            // if lastReadMessage is loaded
            if (room.getUnreadMessagesIds().size() > 0 && view.isMessageVisible(position)) {
                if (isNewestMessagesLoaded) view.smoothScrollTo(0);
                else loadRoomBottomMessages();
                return;
            }

            int finalPosition = position;
            view.runOnUiThread(() -> {
                view.smoothScrollTo(finalPosition);
            });
        });
    }

    @Override
    public void onReplyClicked(Message message) {
        if (message == null) return;
        int messageIndex = -1;

        for (int i = 0; i < messageList.size(); i++) {
            Message m = messageList.get(i);
            if (m.getId().equals(message.getId())) {
                messageIndex = i;
                break;
            }
        }

        if (messageIndex != -1) {
            view.scrollToAndStop(messageIndex);
            view.blinkViewHolder(messageIndex);
            return;
        }

        loadMessagesNearByTime(message.getTime(), true);
    }

    @Override
    public Room getRoom() {
        return room;
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
        private int height;
        private int width;
        private String fileUri;
        private long fileSize;
        private AttachmentReadyListener attachmentReadyListener;

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
                        attachment.getAttachmentType() == AttachmentType.VIDEO
                ) {

                    String previewUri = fileUri;
                    Bitmap bitmap = null;

                    if (attachment.getAttachmentType() == AttachmentType.VIDEO ||
                            attachment.getAttachmentType() == AttachmentType.BUBBLE) {
                        bitmap = ThumbnailUtils.createVideoThumbnail(previewUri, MediaStore.Video.Thumbnails.MINI_KIND);
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

                attachmentReadyListener.onReady(attachment);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
