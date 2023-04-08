package com.diraapp.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.java_websocket.exceptions.WebsocketNotConnectedException;

import com.diraapp.R;
import com.diraapp.api.requests.SendMessageRequest;
import com.diraapp.api.requests.VerifyRoomInfoRequest;
import com.diraapp.api.updates.AcceptedStatusAnswer;
import com.diraapp.api.updates.Update;
import com.diraapp.databinding.ActivityCreateRoomBinding;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Message;
import com.diraapp.db.entities.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.updates.UpdateProcessor;
import com.diraapp.updates.listeners.UpdateListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.KeyGenerator;
import com.diraapp.utils.SliderActivity;

public class CreateRoomActivity extends AppCompatActivity {

    private ActivityCreateRoomBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        String roomSecret = KeyGenerator.generateRoomSecret();

        binding.secretCodeText.setText(roomSecret.substring(0, 8) + "***********");

        binding.copySecretCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Secret key", roomSecret);
                clipboard.setPrimaryClip(clip);
            }
        });

        binding.buttonCreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String roomName = binding.roomNameEditText.getText().toString();
                    String welcomeMessage = binding.welcomeMessageEditText.getText().toString();

                    UpdateProcessor.getInstance(getApplicationContext()).sendRequest(new VerifyRoomInfoRequest(roomName, roomSecret), new UpdateListener() {
                        @Override
                        public void onUpdate(Update update) {
                            AcceptedStatusAnswer acceptedStatusAnswer = (AcceptedStatusAnswer) update;

                            if (acceptedStatusAnswer.isAccepted()) {
                                RoomDao roomDao = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao();

                                Room room = new Room(roomName, System.currentTimeMillis(), roomSecret);

                                roomDao.insertAll(room);

                                Message message = new Message();
                                message.setText(welcomeMessage);
                                message.setAuthorId(CacheUtils.getInstance().getString(CacheUtils.ID, getApplicationContext()));
                                message.setAuthorNickname(CacheUtils.getInstance().getString(CacheUtils.NICKNAME, getApplicationContext()));
                                message.setId(KeyGenerator.generateId());
                                message.setRoomSecret(roomSecret);

                                SendMessageRequest sendMessageRequest = new SendMessageRequest(message);

                                UpdateProcessor.getInstance().sendSubscribeRequest();
                                try {
                                    UpdateProcessor.getInstance().sendRequest(sendMessageRequest);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                finish();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(CreateRoomActivity.this, acceptedStatusAnswer.isAccepted() + "- ans", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });
                } catch (WebsocketNotConnectedException e) {
                    Toast.makeText(CreateRoomActivity.this, "Not connected", Toast.LENGTH_LONG).show();
                } catch (UnablePerformRequestException e) {
                    e.printStackTrace();
                }

            }
        });

        binding.buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}