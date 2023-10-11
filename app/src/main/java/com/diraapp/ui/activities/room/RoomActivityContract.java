package com.diraapp.ui.activities.room;

import android.graphics.Bitmap;
import android.net.Uri;

import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.diraapp.api.userstatus.UserStatus;
import com.diraapp.api.views.UserStatusType;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.utils.CacheUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Callback;

public interface RoomActivityContract {

    interface View {
        void fillRoomInfo(Bitmap picture, Room name);

        void notifyRecyclerMessage(Message message, boolean needUpdateList);

        void notifyMessagesChanged(int start, int last, int scrollPosition);

        void notifyMessageChangedWithoutScroll(int start, int last);

        void notifyOnRoomOpenMessagesLoaded(int scrollPosition);

        void notifyAdapterItemChanged(int index);

        void notifyAdapterItemsDeleted(int start, int last);

        boolean isMessageVisible(int position);

        void setMessages(List<Message> messages);

        void uploadFile(String sourceFileUri,
                        Callback callback,
                        boolean deleteAfterUpload,
                        String serverAddress,
                        String encryptionKey);

        void compressVideo(List<Uri> urisToCompress,
                           String fileUri,
                           VideoQuality videoQuality,
                           Double videoHeight,
                           Double videoWidth,
                           RoomActivityPresenter.RoomAttachmentCallback callback,
                           String serverAddress,
                           String encryptionKey,
                           int bitrate);

        DiraRoomDatabase getRoomDatabase();

        DiraMessageDatabase getMessagesDatabase();

        CacheUtils getCacheUtils();

        void updateUserStatus(String roomSecret, ArrayList<UserStatus> usersUserStatusList);

        void runBackground(Runnable runnable);

        void runOnUiThread(Runnable runnable);

        void setReplyMessage(Message message);

        void smoothScrollTo(int index);

        Bitmap getBitmap(String path);

        void setOnScrollListener();
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void initRoomInfo();

        void initMembers();

        HashMap<String, Member> getMembers();

        Room getRoom();

        void loadMessagesBefore(Message message, int index);

        void loadNewerMessage(Message message, int index);

        void sendStatus(UserStatusType userStatusType);

        void loadMessagesAtRoomStart();

        void loadRoomBottomMessages();

        void loadMessagesNearByTime(long time);

        boolean sendTextMessage(String text);

        void uploadAttachmentAndSendMessage(AttachmentType attachmentType, String fileUri, String messageText);

        void sendMessage(Message message) throws UnablePerformRequestException;

        void setReplyingMessage(Message message);

        void onScrollArrowPressed();
    }

}
