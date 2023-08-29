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

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RoomActivityPresenter implements RoomActivityContract.Presenter, UpdateListener {


    private final String roomSecret;
    private Room room;
    private List<Message> messageList;

    private RoomActivityContract.View view;

    private final String selfId;

    private UserStatus currentUserStatus;

    public RoomActivityPresenter(String roomSecret, String selfId) {
        this.roomSecret = roomSecret;
        this.selfId = selfId;
    }

    @Override
    public void onUpdate(Update update) {
        if (update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE) {
            NewMessageUpdate newMessageUpdate = (NewMessageUpdate) update;
            if (!newMessageUpdate.getMessage().getRoomSecret().equals(roomSecret)) return;

            room = view.getRoomDatabase().getRoomDao().getRoomBySecretName(roomSecret);
            room.setUpdatedRead(true);
            view.getRoomDatabase().getRoomDao().update(room);

            messageList.add(0, newMessageUpdate.getMessage());

            view.notifyRecyclerMessage();
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

            view.notifyAdapterChanged(index);
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
                List<Message> oldMessages = messageDao.getLastMessagesInRoom(roomSecret, message.getTime());
                if (oldMessages.size() == 0) return;
                messageList.addAll(oldMessages);
                view.notifyMessagesChanged();
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
            messageList = view.getMessagesDatabase().getMessageDao().getLastMessagesInRoom(roomSecret);
            initMembers();
            view.setMessages(messageList);
            view.notifyMessagesChanged();
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
            System.out.println("uploading...");

            if (FileClassifier.isVideoFile(fileUri) && (attachmentType == AttachmentType.VIDEO
                    || attachmentType == AttachmentType.BUBBLE)) {
                List<Uri> urisToCompress = new ArrayList<>();
                urisToCompress.add(Uri.fromFile(new File(fileUri)));
                System.out.println("compression started");


                Double videoHeight = null;
                Double videoWidth = null;

                VideoQuality videoQuality = VideoQuality.VERY_LOW;

                if (attachmentType == AttachmentType.BUBBLE) {
                    view.uploadFile(fileUri,
                            new RoomAttachmentCallback(fileUri, messageText, attachmentType),
                            false,
                            room.getServerAddress(),
                            room.getEncryptionKey());
                    return;
                }

                view.compressVideo(urisToCompress, fileUri, videoQuality,
                        videoHeight, videoWidth,
                        new RoomActivityPresenter.RoomAttachmentCallback(null, messageText, attachmentType),
                        room.getServerAddress(), room.getEncryptionKey());

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
            return this;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
            System.out.println(":(");
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            System.out.println("uploading");

            try {
                String fileTempName = new JSONObject(response.body().string()).getString("message");
                Attachment attachment = new Attachment();


                attachment.setAttachmentType(attachmentType);

                attachment.setFileCreatedTime(System.currentTimeMillis());
                attachment.setFileName("attachment");
                System.out.println("uploaded url " + fileTempName);
                attachment.setFileUrl(fileTempName);
                attachment.setSize(new File(fileUri).length());

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
