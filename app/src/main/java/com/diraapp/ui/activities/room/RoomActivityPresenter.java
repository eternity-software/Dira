package com.diraapp.ui.activities.room;


import android.graphics.Bitmap;
import android.net.Uri;

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
import com.diraapp.userstatus.UserStatus;
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

public class RoomActivityPresenter implements RoomActivityContract.Presenter, UpdateListener {


    private static final int MAX_ADAPTER_MESSAGES_COUNT = 200;
    private final String roomSecret;
    private final String selfId;

    private List<Message> messageList;
    private RoomActivityContract.View view;
    private UserStatus currentUserStatus;
    private Room room;
    private boolean isNewestMessagesLoaded = false;

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
        } else if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
            initRoomInfo();
        } else if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
            view.runBackground(this::initMembers);
        } else if (update.getUpdateType() == UpdateType.READ_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;

            MessageReadUpdate readUpdate = (MessageReadUpdate) update;

            if (((MessageReadUpdate) update).getUserId().equals(selfId)) return;
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

            MessageReading thisReading = new MessageReading(readUpdate.getUserId(),
                    readUpdate.getReadTime());

            for (MessageReading reading : thisMessage.getMessageReadingList()) {
                if (reading.getUserId().equals(thisReading.getUserId())) return;
            }

            thisMessage.getMessageReadingList().add(thisReading);

            view.notifyAdapterItemChanged(index);
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

        HashMap<String, Member> memberHashMap = new HashMap<>();

        for (Member member : memberList) {
            memberHashMap.put(member.getId(), member);
        }
        view.setMembers(memberHashMap);
    }

    @Override
    public void loadMessagesBefore(Message message, int index) {
        view.runBackground(() -> {
            try {
                MessageDao messageDao = view.getMessagesDatabase().getMessageDao();
                List<Message> oldMessages = messageDao.getBeforeMessagesInRoom(roomSecret, message.getTime());
                if (oldMessages.size() == 0) return;
                int notifyingIndex = messageList.size() - 1;
                messageList.addAll(oldMessages);
                view.notifyMessagesChanged(index + 1, index + oldMessages.size(),
                        RoomActivity.doNotNeedToScroll);

                view.notifyAdapterItemChanged(notifyingIndex);

                if (messageList.size() > MAX_ADAPTER_MESSAGES_COUNT) {
                    messageList.subList(0, MessageDao.LOADING_COUNT).clear();
                    view.notifyAdapterItemsDeleted(0, MessageDao.LOADING_COUNT);

                    isNewestMessagesLoaded = false;
                }
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


                for (Message m : newMessages) {
                    messageList.add(0, m);
                }
                view.notifyMessagesChanged(0, newMessages.size(), newMessages.size());

                if (messageList.size() > MAX_ADAPTER_MESSAGES_COUNT) {
                    int size = messageList.size();
                    messageList.subList(size - MessageDao.LOADING_COUNT + 1, size).clear();
                    view.notifyAdapterItemsDeleted(size - MessageDao.LOADING_COUNT,
                            MessageDao.LOADING_COUNT);
                }
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
    public void loadMessages() {
        view.runBackground(() -> {
            MessageDao messageDao = view.getMessagesDatabase().getMessageDao();

            int scrollTo = RoomActivity.doNotNeedToScroll;

            int size = room.getUnreadMessagesIds().size();
            if (size == 0) {
                messageList = messageDao.getLatestMessagesInRoom(roomSecret);
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
            }

            initMembers();
            view.setMessages(messageList);
            view.notifyMessagesChanged(RoomActivity.isRoomOpen, 0, scrollTo);
        });

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

            SendMessageRequest sendMessageRequest = new SendMessageRequest(message);
            try {
                UpdateProcessor.getInstance().sendRequest(sendMessageRequest, room.getServerAddress());
            } catch (UnablePerformRequestException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }


    @Override
    public void uploadAttachmentAndSendMessage(AttachmentType attachmentType, String fileUri, String messageText) {
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
                        new RoomActivityPresenter.RoomAttachmentCallback(null, messageText, attachmentType),
                        room.getServerAddress(), room.getEncryptionKey(), bitrate);

            } else {
                view.uploadFile(fileUri,
                        new RoomAttachmentCallback(fileUri, messageText, attachmentType),
                        false,
                        room.getServerAddress(),
                        room.getEncryptionKey());
            }
        });

    }

    public class RoomAttachmentCallback implements Callback {

        private final String messageText;
        private final AttachmentType attachmentType;

        private int height;
        private int width;
        private String fileUri;

        private long fileSize;

        public RoomAttachmentCallback(String fileUri, String messageText, AttachmentType attachmentType) {
            this.fileUri = fileUri;
            this.messageText = messageText;
            this.attachmentType = attachmentType;
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

        public RoomAttachmentCallback setFileUri(String fileUri) {
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

                if(attachment.getSize() == 0) attachment.setSize(fileSize);


                if (attachment.getAttachmentType() == AttachmentType.VIDEO ||
                        attachment.getAttachmentType() == AttachmentType.IMAGE) {
                    attachment.setHeight(this.height);
                    attachment.setWidth(this.width);
                }

                Message message = Message.generateMessage(view.getCacheUtils(), roomSecret);

                message.setLastTimeEncryptionKeyUpdated(room.getTimeEncryptionKeyUpdated());

                if (room.getEncryptionKey().equals("")) {
                    message.setText(messageText);
                } else {
                    message.setText(EncryptionUtil.encrypt(messageText, room.getEncryptionKey()));
                }

                message.getAttachments().add(attachment);

                SendMessageRequest sendMessageRequest = new SendMessageRequest(message);

                UpdateProcessor.getInstance().sendRequest(sendMessageRequest, room.getServerAddress());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
