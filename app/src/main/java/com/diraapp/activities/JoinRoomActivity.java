package com.diraapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import com.diraapp.R;
import com.diraapp.api.requests.JoinRoomRequest;
import com.diraapp.api.requests.UpdateMemberRequest;
import com.diraapp.api.updates.NewRoomUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.updates.UpdateProcessor;
import com.diraapp.updates.listeners.UpdateListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.SliderActivity;

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