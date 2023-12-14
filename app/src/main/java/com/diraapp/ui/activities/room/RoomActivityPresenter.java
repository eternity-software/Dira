package com.diraapp.ui.activities.room;


import android.content.Context;
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
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.MessageReading;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
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

    private ArrayList<Message> pinnedMessages = new ArrayList<>();

    private HashSet<String> pinnedIds = new HashSet<>();
    private final String roomSecret;
    private final String selfId;

    private List<Message> messageList = new ArrayList<>();
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
            room.setLastUpdatedTime(message.getTime());

            view.notifyRecyclerMessage(newMessageUpdate.getMessage(), needUpdateList);

            if (message.hasAuthor()) {
                if (message.getAuthorId().equals(selfId)) {
                    room.getUnreadMessagesIds().clear();
                    lastReadMessage = message;
                } else {
                    room.getUnreadMessagesIds().add(message.getId());
                }

                view.updateScrollArrow();
                view.updateScrollArrowIndicator();

            }
        } else if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
            initRoomInfo();
        } else if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
            view.runBackground(this::initMembers);
        } else if (update.getUpdateType() == UpdateType.READ_UPDATE) {
            Logger.logDebug("ReadingDebug", "1");
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
            AttachmentListenedUpdate listenedUpdate = (AttachmentListenedUpdate) update;

            onAttachmentListenedUpdate(listenedUpdate);
        } else if (update.getUpdateType() == UpdateType.PINNED_MESSAGE_ADDED_UPDATE) {
            addPinned((PinnedMessageAddedUpdate) update);
        } else if (update.getUpdateType() == UpdateType.PINNED_MESSAGE_REMOVED_UPDATE) {
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
        view = null;
        UpdateProcessor.getInstance().removeUpdateListener(this);
      //  this.view = null;
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

        for (String id: room.getPinnedMessagesIds()) {
            Message message = messageDao.getMessageById(id);

            if (message == null) continue;

            pinnedMessages.add(message);
            pinnedIds.add(id);
        }

        view.runOnUiThread(() -> {
            view.showLastPinned();
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
    public void scrollToMessage(Message message) {
        if (message == null) return;
        int messageIndex = getMessagePos(message);

        if (messageIndex != -1) {
            if (view.isMessageVisible(messageIndex)) {
                view.blinkViewHolder(messageIndex);
            } else {
                view.addMessageToBlinkId(message.getId());
            }

            view.scrollToAndStop(messageIndex);
            return;
        }

        view.addMessageToBlinkId(message.getId());
        loadMessagesNearByTime(message.getTime(), true);
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

    private int getMessagePos(Message message) {
        int messageIndex = -1;

        for (int i = 0; i < messageList.size(); i++) {
            Message m = messageList.get(i);
            if (m.getId().equals(message.getId())) {
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
    public void updateUnsentText() {
        RoomDao roomDao = view.getRoomDatabase().getRoomDao();

        view.runBackground(() -> {
            roomDao.update(room);
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

                if (lastReadMessage == null) lastReadMessage = thisMessage;
                else if (thisMessage.getTime() > lastReadMessage.getTime()) {
                    lastReadMessage = thisMessage;
                }
            }

            view.updateScrollArrow();
            view.updateScrollArrowIndicator();
            return;
        }
        Logger.logDebug("ReadingDebug", "4");
        MessageReading thisReading = new MessageReading(userId,
                readTime);
        thisReading.setHasListened(isListened);

        for (MessageReading reading : thisMessage.getMessageReadingList()) {
            if (reading.getUserId().equals(thisReading.getUserId())) return;
        }
        Logger.logDebug("ReadingDebug", "5");
        thisMessage.getMessageReadingList().add(thisReading);

        view.notifyRecyclerMessageRead(thisMessage, index);
    }

    private void onAttachmentListenedUpdate(AttachmentListenedUpdate listenedUpdate) {
        if (!listenedUpdate.getRoomSecret().equals(roomSecret)) return;

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
        } );

        if (isSelf) return;

        MessageReading messageReading = null;
        for (MessageReading mr: thisMessage.getMessageReadingList()) {
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

            for (Attachment attachment: message.getAttachments()) {
                AppStorage.deleteAttachment(context, attachment, message.getRoomSecret());
            }

            String messageId = message.getId();
            if (lastReadMessage != null) {
                if (messageId.equals(lastReadMessage.getId())) lastReadMessage = null;
            }

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
            }

            if (room.getUnreadMessagesIds().contains(messageId)) {
                updateRoom = true;

                room.getUnreadMessagesIds().remove(messageId);
            }

            if (pinnedIds.contains(messageId)) {
                updateRoom = true;

                removePinned(new PinnedMessageRemovedUpdate(
                                roomSecret, messageId, message.getAuthorId()));
            }

            if (updateRoom) {
                view.getRoomDatabase().getRoomDao().update(room);
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
        if (!update.getRoomSecret().equals(roomSecret)) return;
        if (pinnedIds.contains(update.getMessageId())) return;

        pinnedIds.add(update.getMessageId());

        view.runBackground(() -> {
            Message message = view.getMessagesDatabase().getMessageDao().
                    getMessageById(update.getMessageId());

            pinnedMessages.add(message);

            room.getPinnedMessagesIds().add(update.getMessageId());

            view.runOnUiThread(() -> {
                view.showLastPinned();
            });
        });
    }

    @Override
    public void removePinned(PinnedMessageRemovedUpdate update) {
        if (!update.getRoomSecret().equals(roomSecret)) return;
        if (!pinnedIds.contains(update.getMessageId())) return;

        pinnedIds.remove(update.getMessageId());

        Message message = null;
        for (Message m: pinnedMessages) {
            if (m.getId().equals(update.getMessageId())) {
                message = m;
                break;
            }
        }

        if (message == null) return;
        pinnedMessages.remove(message);

        room.getPinnedMessagesIds().remove(update.getMessageId());

        view.showLastPinned();
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
            int unreadMessagesSize = room.getUnreadMessagesIds().size();
            if (lastReadMessage == null) {
                String lastReadMessageId;
                if (unreadMessagesSize > 0) {
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
                    if (lastReadMessageId == null) {
                        Logger.logDebug("Scroll arrow",
                                "Can't find last read message in database");
                        return;
                    }
                    position = NOT_FOUND;
                }
            }

            if (position == START_VAL) {
                for (int i = 0; i < messageList.size(); i++) {
                    Message m = messageList.get(i);
                    if (m.getId().equals(lastReadMessage.getId())) {
                        position = i;
                        break;
                    }
                }

                if (position == START_VAL) position = NOT_FOUND;
            }

            if (position == NOT_FOUND) {
                Logger.logDebug("Scroll arrow", "Not found!");
                if (lastReadMessage == null) return;
                if (!lastReadMessage.getRoomSecret().equals(roomSecret)) return;
                loadMessagesNearByTime(lastReadMessage.getTime());
                return;
            }

            Logger.logDebug("Scroll arrow", "position: " + position);
            if (unreadMessagesSize > 0 && view.isMessageVisible(position)) {
                Logger.logDebug("Scroll arrow", "Has been scrolled to bottom");
                if (isNewestMessagesLoaded) {
                    view.runOnUiThread(() -> {
                        view.scrollTo(0);
                    });
                } else loadRoomBottomMessages();
                return;
            }

            int finalPosition = position;
            view.runOnUiThread(() -> {
                Logger.logDebug("Scroll arrow", "Has been scrolled to position");
                view.scrollTo(finalPosition);
            });
        });
    }

    @Override
    public void onReplyClicked(Message message) {
        scrollToMessage(message);
    }

    @Override
    public Room getRoom() {
        return room;
    }

    @Override
    public Message getMessageByPosition(int pos) {
        if (pos >= messageList.size()) return null;
        if(pos == -1) return null;
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
