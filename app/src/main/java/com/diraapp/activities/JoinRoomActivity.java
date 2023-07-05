package com.diraapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.R;
import com.diraapp.activities.createroom.CreateRoomActivity;
import com.diraapp.api.requests.JoinRoomRequest;
import com.diraapp.api.requests.UpdateMemberRequest;
import com.diraapp.api.updates.NewRoomUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.bottomsheet.ServerSelectorBottomSheet;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.updates.UpdateProcessor;
import com.diraapp.updates.listeners.UpdateListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.SliderActivity;

import java.util.Collections;

public class JoinRoomActivity extends AppCompatActivity implements ServerSelectorBottomSheet.BottomSheetListener {

    private String serverAddress = UpdateProcessor.OFFICIAL_ADDRESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);
        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        findViewById(R.id.button_server_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerSelectorBottomSheet serverSelectorBottomSheet = new ServerSelectorBottomSheet();

                serverSelectorBottomSheet.show(getSupportFragmentManager(), "Server selector  bottom sheet");
            }
        });

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

                                        CacheUtils cacheUtils = new CacheUtils(getApplicationContext());

                                        String nickname = cacheUtils.getString(CacheUtils.NICKNAME);
                                        String id = cacheUtils.getString(CacheUtils.ID);
                                        String picturePath = cacheUtils.getString(CacheUtils.PICTURE);

                                        String base64Pic = null;

                                        if (picturePath != null) {
                                            base64Pic = AppStorage.getBase64FromBitmap(AppStorage.getBitmapFromPath(picturePath));
                                        }

                                        UpdateMemberRequest updateMemberRequest = new UpdateMemberRequest(nickname, base64Pic,
                                                Collections.singletonList(((NewRoomUpdate) update).getInviteRoom().getSecretName()), id, System.currentTimeMillis());
                                        try {


                                            UpdateProcessor.getInstance().sendRequest(updateMemberRequest, serverAddress);
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
                    }, serverAddress);
                } catch (UnablePerformRequestException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onServerSelected(String serverAddress) {
        TextView textView = findViewById(R.id.button_server_select);
        this.serverAddress = serverAddress;
        textView.setText(serverAddress);
    }
}