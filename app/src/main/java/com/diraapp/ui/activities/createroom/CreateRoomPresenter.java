package com.diraapp.ui.activities.createroom;

import android.content.ClipData;
import android.content.ClipboardManager;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.VerifyRoomInfoRequest;
import com.diraapp.api.updates.AcceptedStatusAnswer;
import com.diraapp.api.updates.Update;
import com.diraapp.db.entities.rooms.RoomType;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.utils.KeyGenerator;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

public class CreateRoomPresenter implements CreateRoomContract.Presenter {

    private final CreateRoomContract.View view;
    private final CreateRoomContract.Model model;

    private final String roomSecret;
    private int updateExpireSec = Update.DEFAULT_UPDATE_EXPIRE_SEC;
    private String serverAddress = UpdateProcessor.OFFICIAL_ADDRESS;

    public CreateRoomPresenter(CreateRoomContract.View view, CreateRoomContract.Model model) {
        this.view = view;
        this.model = model;
        roomSecret = KeyGenerator.generateRoomSecret();
        view.setSecretCodeText(roomSecret);
    }

    @Override
    public void onBackButtonClicked() {
        view.onBackPressed();
    }

    @Override
    public void onCreateButtonClick() {
        try {
            String roomName = view.getRoomName();
            String welcomeMessage = view.getWelcomeMessage();
            RoomType type = view.getRoomType();

            UpdateProcessor.getInstance().sendRequest(
                    new VerifyRoomInfoRequest(roomName, roomSecret),
                    (Update update) -> {
                        AcceptedStatusAnswer acceptedStatusAnswer = (AcceptedStatusAnswer) update;

                        if (acceptedStatusAnswer.isAccepted()) {
                            model.createRoom(roomName, roomSecret, welcomeMessage,
                                    view.getAuthorId(), view.getAuthorName(), serverAddress,
                                    type, updateExpireSec);
                            view.finish();
                        }
                    }, serverAddress);

        } catch (WebsocketNotConnectedException e) {
            view.showToast("Not connected");
        } catch (UnablePerformRequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCopyButtonClick(ClipboardManager clipboardManager) {
        ClipData clip = ClipData.newPlainText("Secret key", roomSecret);
        clipboardManager.setPrimaryClip(clip);
    }

    @Override
    public void setServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    @Override
    public void setUpdateExpireSec(int seconds) {
        updateExpireSec = seconds;
    }


}
