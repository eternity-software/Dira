package com.diraapp.activities.createroom;

import android.content.ClipData;
import android.content.ClipboardManager;

import com.diraapp.api.requests.VerifyRoomInfoRequest;
import com.diraapp.api.updates.AcceptedStatusAnswer;
import com.diraapp.api.updates.Update;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.updates.UpdateProcessor;
import com.diraapp.updates.listeners.UpdateListener;
import com.diraapp.utils.KeyGenerator;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

public class CreateRoomPresenter implements CreateRoomContract.Presenter {


    private final CreateRoomContract.View view;
    private final CreateRoomContract.Model model;

    private final String roomSecret;

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

            UpdateProcessor.getInstance().sendRequest(
                    new VerifyRoomInfoRequest(roomName, roomSecret),
                    (Update update) -> {
                        AcceptedStatusAnswer acceptedStatusAnswer = (AcceptedStatusAnswer) update;

                        if (acceptedStatusAnswer.isAccepted()) {
                            model.createRoom(roomName, roomSecret, welcomeMessage,
                                    view.getAuthorId(), view.getAuthorName());
                            view.finish();
                        }
                    });
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


}
