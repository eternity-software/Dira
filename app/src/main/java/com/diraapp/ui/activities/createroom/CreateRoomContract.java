package com.diraapp.ui.activities.createroom;

import android.content.ClipboardManager;

import com.diraapp.db.entities.rooms.RoomType;

public interface CreateRoomContract {

    interface Presenter {
        void onBackButtonClicked();

        void onCreateButtonClick();

        void onCopyButtonClick(ClipboardManager clipboardManager);

        void setServer(String serverAddress);

        void setUpdateExpireSec(int seconds);
    }

    interface View {
        void attachSlider();

        void setSecretCodeText(String secretCode);

        void onBackPressed();

        void finish();

        void showToast(String text);

        String getRoomName();

        String getWelcomeMessage();

        String getAuthorName();

        String getAuthorId();

        RoomType getRoomType();
    }

    interface Model {
        void createRoom(String roomName, String secretName, String welcomeMessage,
                        String authorId, String authorName, String serverAddress,
                        RoomType roomType, int updateExpireSec);
    }

}
