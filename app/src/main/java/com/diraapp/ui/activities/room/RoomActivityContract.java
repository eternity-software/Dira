package com.diraapp.ui.activities.room;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.diraapp.api.updates.PinnedMessageAddedUpdate;
import com.diraapp.api.updates.PinnedMessageRemovedUpdate;
import com.diraapp.api.userstatus.UserStatus;
import com.diraapp.api.views.UserStatusType;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.utils.CacheUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface RoomActivityContract {

    interface View {
        void fillRoomInfo(Bitmap picture, Room name);

        void notifyRecyclerMessage(Message message, boolean needUpdateList);

        void notifyRecyclerMessageRead(Message message, int pos);

        void notifyMessagesInserted(int start, int last, int scrollPosition);

        void notifyMessageInsertedWithoutScroll(int start, int last);

        void notifyOnRoomOpenMessagesLoaded(int scrollPosition);

        void notifyAdapterItemsChanged(int from, int to);

        void notifyAdapterItemChanged(int index);

        void notifyAdapterItemsDeleted(int start, int last);

        void notifyAdapterItemRemoved(int pos);

        void notifyViewHolderUpdateTimeAndPicture(int pos, Message message, Message previousMessage);

        void notifyViewHolder(int pos, BaseMessageViewHolder.ViewHolderNotification notification);

        void blinkViewHolder(int position);

        boolean isMessageVisible(int position);

        void setMessages(List<Message> messages);

        void uploadFile(String sourceFileUri,
                        RoomActivityPresenter.AttachmentHandler callback,
                        boolean deleteAfterUpload,
                        String serverAddress,
                        String encryptionKey);

        void compressVideo(List<Uri> urisToCompress,
                           String fileUri,
                           VideoQuality videoQuality,
                           Double videoHeight,
                           Double videoWidth,
                           RoomActivityPresenter.AttachmentHandler callback,
                           String serverAddress,
                           String encryptionKey,
                           int bitrate);

        DiraRoomDatabase getRoomDatabase();

        DiraMessageDatabase getMessagesDatabase();

        CacheUtils getCacheUtils();

        void updateRoomStatus(String roomSecret, ArrayList<UserStatus> usersUserStatusList);

        void runBackground(Runnable runnable);

        void runOnUiThread(Runnable runnable);

        void setReplyMessage(Message message);

        void smoothScrollTo(int index);

        void scrollTo(int index);

        void scrollToAndStop(int index);

        Bitmap getBitmap(String path);

        void setOnScrollListener();

        void updateScrollArrow();

        void updateScrollArrowIndicator();

        void showLastPinned();

        void showNextPinned();

        void updatePinned(int messagePos);

        void addMessageToBlinkId(String messageId);
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void initRoomInfo();

        void initMembers();

        HashMap<String, Member> getMembers();

        Room getRoom();

        Message getMessageByPosition(int pos);

        int getItemsCount();

        boolean isNewestMessagesLoaded();

        void loadMessagesBefore(Message message, int index);

        void loadNewerMessage(Message message, int index);

        void sendStatus(UserStatusType userStatusType);

        void loadMessagesAtRoomStart();

        void loadRoomBottomMessages();

        void loadMessagesNearByTime(long time);

        void loadMessagesNearByTime(long time, boolean needBlink);

        void scrollToMessage(String messageId, long messageTime);

        void blinkMessage(Message message);

        boolean sendTextMessage(String text);

        void uploadAttachment(AttachmentType attachmentType, RoomActivityPresenter.AttachmentReadyListener attachmentReadyListener, String fileUri);

        void sendMessage(Message message) throws UnablePerformRequestException;

        void setReplyingMessage(Message message);

        void onScrollArrowPressed();

        String getAndClearReplyId();

        void sendMessage(ArrayList<Attachment> attachments, String messageText, String replyId);

        void updateDynamicRoomFields();

        void deleteMessage(Message message, Context context);

        ArrayList<Message> getPinnedMessages();

        boolean isPinned(String messageId);

        void addPinned(PinnedMessageAddedUpdate update);

        void removePinned(PinnedMessageRemovedUpdate update);
    }

}
