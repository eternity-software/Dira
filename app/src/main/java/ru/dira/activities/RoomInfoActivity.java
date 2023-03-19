package ru.dira.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import ru.dira.R;
import ru.dira.api.RoomMember;
import ru.dira.api.requests.CreateInviteRequest;
import ru.dira.api.updates.NewInvitationUpdate;
import ru.dira.api.updates.Update;
import ru.dira.api.updates.UpdateType;
import ru.dira.bottomsheet.InvitationCodeBottomSheet;
import ru.dira.db.DiraRoomDatabase;
import ru.dira.db.entities.Member;
import ru.dira.db.entities.Room;
import ru.dira.exceptions.UnablePerformRequestException;
import ru.dira.storage.AppStorage;
import ru.dira.updates.UpdateProcessor;
import ru.dira.updates.listeners.UpdateListener;
import ru.dira.utils.CacheUtils;
import ru.dira.utils.SliderActivity;

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