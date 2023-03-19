package ru.dira.activities;

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

import ru.dira.R;
import ru.dira.api.requests.SendMessageRequest;
import ru.dira.api.requests.VerifyRoomInfoRequest;
import ru.dira.api.updates.AcceptedStatusAnswer;
import ru.dira.api.updates.Update;
import ru.dira.db.DiraRoomDatabase;
import ru.dira.db.daos.RoomDao;
import ru.dira.db.entities.Message;
import ru.dira.db.entities.Room;
import ru.dira.exceptions.UnablePerformRequestException;
import ru.dira.updates.UpdateProcessor;
import ru.dira.updates.listeners.UpdateListener;
import ru.dira.utils.CacheUtils;
import ru.dira.utils.KeyGenerator;
import ru.dira.utils.SliderActivity;

public class CreateRoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        TextView roomSecretText = findViewById(R.id.secret_code_text);
        TextView roomWelcomeMessageText = findViewById(R.id.welcome_message);
        EditText roomNameText = findViewById(R.id.room_name);

        String roomSecret = KeyGenerator.generateRoomSecret();


        roomSecretText.setText(roomSecret.substring(0, 8) + "***********");

        findViewById(R.id.copy_secret_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Secret key", roomSecret);
                clipboard.setPrimaryClip(clip);
            }
        });


        findViewById(R.id.button_create_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String roomName = roomNameText.getText().toString();
                    String welcomeMessage = roomWelcomeMessageText.getText().toString();

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

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}