package ru.dira.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import ru.dira.R;
import ru.dira.api.requests.JoinRoomRequest;
import ru.dira.api.requests.UpdateMemberRequest;
import ru.dira.api.updates.NewRoomUpdate;
import ru.dira.api.updates.Update;
import ru.dira.exceptions.UnablePerformRequestException;
import ru.dira.storage.AppStorage;
import ru.dira.updates.UpdateProcessor;
import ru.dira.updates.listeners.UpdateListener;
import ru.dira.utils.CacheUtils;
import ru.dira.utils.SliderActivity;

public class JoinRoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);
        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);


        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.button_create_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JoinRoomActivity.this, CreateRoomActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.button_join_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText codeInput = findViewById(R.id.invite_code_input);
                JoinRoomRequest joinRoomRequest = new JoinRoomRequest(codeInput.getText().toString());

                try {
                    UpdateProcessor.getInstance().sendRequest(joinRoomRequest, new UpdateListener() {
                        @Override
                        public void onUpdate(Update update) {
                            if (update instanceof NewRoomUpdate) {
                                UpdateProcessor.getInstance().sendSubscribeRequest();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        String nickname = CacheUtils.getInstance().getString(CacheUtils.NICKNAME, getApplicationContext());
                                        String id = CacheUtils.getInstance().getString(CacheUtils.ID, getApplicationContext());
                                        String picturePath = CacheUtils.getInstance().getString(CacheUtils.PICTURE, getApplicationContext());

                                        String base64Pic = null;

                                        if (picturePath != null) {
                                            base64Pic = AppStorage.getBase64FromBitmap(AppStorage.getImage(picturePath));
                                        }

                                        UpdateMemberRequest updateMemberRequest = new UpdateMemberRequest(nickname, base64Pic,
                                                Arrays.asList(((NewRoomUpdate) update).getInviteRoom().getSecretName()), id, System.currentTimeMillis());
                                        try {

                                            UpdateProcessor.getInstance().sendRequest(updateMemberRequest);
                                            UpdateProcessor.getInstance().reconnectSockets();
                                            Thread thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    UpdateProcessor.getInstance().sendSubscribeRequest();
                                                }
                                            });
                                            thread.start();

                                        } catch (UnablePerformRequestException e) {
                                            e.printStackTrace();
                                        }
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                } catch (UnablePerformRequestException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}