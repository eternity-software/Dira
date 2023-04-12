package com.diraapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.diraapp.R;
import com.diraapp.api.RoomMember;
import com.diraapp.api.requests.CreateInviteRequest;
import com.diraapp.api.updates.NewInvitationUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.bottomsheet.InvitationCodeBottomSheet;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.updates.UpdateProcessor;
import com.diraapp.updates.listeners.UpdateListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.SliderActivity;

import java.util.ArrayList;
import java.util.List;

public class RoomInfoActivity extends AppCompatActivity implements UpdateListener, InvitationCodeBottomSheet.BottomSheetListener {

    public static final String ROOM_SECRET_EXTRA = "roomSecret";

    private String roomSecret;
    private Room room;
    private List<Member> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info);

        roomSecret = getIntent().getExtras().getString(ROOM_SECRET_EXTRA);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomInfoActivity.this, EditRoomActivity.class);
                intent.putExtra(EditRoomActivity.ROOM_SECRET_EXTRA, roomSecret);
                startActivity(intent);
            }
        });

        findViewById(R.id.invite_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ImageView inviteIcon = findViewById(R.id.icon_invite);
                ProgressBar progressBar = findViewById(R.id.progress_circular);

                if (progressBar.getVisibility() == View.VISIBLE) return;
                inviteIcon.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                List<RoomMember> roomMembers = new ArrayList<>();

                for (Member member : members) {
                    roomMembers.add(new RoomMember(member.getId(), member.getNickname(),
                            AppStorage.getBase64FromBitmap(AppStorage.getImage(member.getImagePath())),
                            member.getRoomSecret(),
                            member.getLastTimeUpdated()));
                }

                roomMembers.add(new RoomMember(CacheUtils.getInstance().getString(CacheUtils.ID, getApplicationContext()),
                        CacheUtils.getInstance().getString(CacheUtils.NICKNAME, getApplicationContext()),
                        AppStorage.getBase64FromBitmap(AppStorage.getImage(CacheUtils.getInstance().getString(CacheUtils.PICTURE, getApplicationContext()))),
                        room.getSecretName(), System.currentTimeMillis()));

                CreateInviteRequest createInviteRequest = new CreateInviteRequest(room.getName(),
                        room.getSecretName(),
                        AppStorage.getBase64FromBitmap(AppStorage.getImage(room.getImagePath())),
                        roomMembers);

                try {
                    UpdateProcessor.getInstance().sendRequest(createInviteRequest, new UpdateListener() {
                        @Override
                        public void onUpdate(Update update) {
                            if (update.getUpdateType() == UpdateType.ROOM_CREATE_INVITATION) {
                                NewInvitationUpdate newInvitationUpdate = (NewInvitationUpdate) update;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        inviteIcon.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);

                                        try {
                                            InvitationCodeBottomSheet invitationCodeBottomSheet = new InvitationCodeBottomSheet();
                                            invitationCodeBottomSheet.setCode(newInvitationUpdate.getInvitationCode());
                                            invitationCodeBottomSheet.setRoomName(room.getName());
                                            invitationCodeBottomSheet.show(getSupportFragmentManager(), "Invitation bottom sheet");
                                        } catch (Exception e) {

                                        }

                                    }
                                });
                            }
                        }
                    });
                } catch (UnablePerformRequestException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            inviteIcon.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                        }
                    });
                }
            }
        });

        loadData();
        UpdateProcessor.getInstance().addUpdateListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateProcessor.getInstance().removeUpdateListener(this);
    }

    private void loadData() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RoomInfoActivity.this.room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);

                members = DiraRoomDatabase.getDatabase(getApplicationContext()).getMemberDao().getMembersByRoomSecret(room.getSecretName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (room.getImagePath() != null) {
                            ImageView roomPicture = findViewById(R.id.room_picture);
                            roomPicture.setImageBitmap(AppStorage.getImage(room.getImagePath()));
                        }

                        TextView roomName = findViewById(R.id.room_name);
                        roomName.setText(room.getName());
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    public void onUpdate(Update update) {
        if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
            loadData();
        }
    }
}