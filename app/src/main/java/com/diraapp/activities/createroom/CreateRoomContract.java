package com.diraapp.activities.createroom;

import android.content.ClipboardManager;

public interface CreateRoomContract {

    interface Presenter {
        void onBackButtonClicked();

        void onCreateButtonClick();

        void onCopyButtonClick(ClipboardManager clipboardManager);

        void setServer(String serverAddress);
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
    }

    interface Model {
        void createRoom(String roomName, String secretName, String welcomeMessage,
                        String authorId, String authorName, String serverAddress);
    }

}
