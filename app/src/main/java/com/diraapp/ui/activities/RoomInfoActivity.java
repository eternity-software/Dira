package com.diraapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.ui.adapters.MediaGridAdapter;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.api.views.RoomMember;
import com.diraapp.api.requests.CreateInviteRequest;
import com.diraapp.api.updates.NewInvitationUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.appearance.ColorTheme;
import com.diraapp.ui.bottomsheet.InvitationCodeBottomSheet;
import com.diraapp.ui.bottomsheet.filepicker.FileInfo;
import com.diraapp.ui.components.DiraPopup;
import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.Room;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.SliderActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RoomInfoActivity extends AppCompatActivity implements UpdateListener, InvitationCodeBottomSheet.BottomSheetListener {

    public static final String ROOM_SECRET_EXTRA = "roomSecret";

    private String roomSecret;
    private Room room;
    private List<Member> members;
    private MediaGridAdapter mediaGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info);

        roomSecret = getIntent().getExtras().getString(ROOM_SECRET_EXTRA);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        applyColorTheme();

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.leave_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiraPopup diraPopup = new DiraPopup(RoomInfoActivity.this);
                diraPopup.show(getString(R.string.delete_room_title),
                        getString(R.string.delete_room_text),
                        null,
                        null, new Runnable() {
                            @Override
                            public void run() {
                                Thread deletionThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        UpdateProcessor.getInstance(getApplicationContext()).deleteRoom(room);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(RoomInfoActivity.this, RoomSelectorActivity.class);
                                                intent.putExtra(RoomSelectorActivity.CAN_BE_BACK_PRESSED, false);
                                                startActivity(intent);
                                            }
                                        });
                                    }
                                });
                                deletionThread.start();

                            }
                        });
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

        findViewById(R.id.icon_invite).setOnClickListener(new View.OnClickListener() {
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
                            AppStorage.getBase64FromBitmap(AppStorage.getBitmapFromPath(member.getImagePath())),
                            member.getRoomSecret(),
                            member.getLastTimeUpdated()));
                }

                CacheUtils cacheUtils = new CacheUtils(getApplicationContext());

                roomMembers.add(new RoomMember(cacheUtils.getString(CacheUtils.ID),
                        cacheUtils.getString(CacheUtils.NICKNAME),
                        AppStorage.getBase64FromBitmap(AppStorage.getBitmapFromPath(cacheUtils.getString(CacheUtils.PICTURE))),
                        room.getSecretName(), System.currentTimeMillis()));

                CreateInviteRequest createInviteRequest = new CreateInviteRequest(room.getName(),
                        room.getSecretName(),
                        AppStorage.getBase64FromBitmap(AppStorage.getBitmapFromPath(room.getImagePath())),
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
                    }, room.getServerAddress());
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
                        initNotificationButton();

                        if (room.getImagePath() != null) {
                            ImageView roomPicture = findViewById(R.id.room_picture);
                            roomPicture.setImageBitmap(AppStorage.getBitmapFromPath(room.getImagePath()));
                        }

                        TextView roomName = findViewById(R.id.room_name);
                        TextView membersCount = findViewById(R.id.members_count);
                        ImageView memberImage_1 = findViewById(R.id.icon_user_1);
                        ImageView memberImage_2 = findViewById(R.id.icon_user_2);

                        if (members.size() == 1) {
                            memberImage_2.setVisibility(View.GONE);
                            memberImage_1.setImageBitmap(AppStorage.getBitmapFromPath(members.get(0).getImagePath()));
                        } else if (members.size() > 1) {
                            memberImage_2.setImageBitmap(AppStorage.getBitmapFromPath(members.get(1).getImagePath()));
                        }

                        roomName.setText(room.getName());
                        membersCount.setText(getString(R.string.members_count).replace("%s", String.valueOf(members.size() + 1)));


                    }
                });
                ArrayList<FileInfo> attachments = new ArrayList<>();

                for (Message message : DiraMessageDatabase.getDatabase(getApplicationContext()).getMessageDao().getAllMessageByUpdatedTime(roomSecret)) {
                    if (message.getAttachments() != null) {
                        if (message.getAttachments().size() > 0) {
                            Attachment attachment = message.getAttachments().get(0);
                            if (attachment.getAttachmentType() == AttachmentType.IMAGE ||
                                    attachment.getAttachmentType() == AttachmentType.VIDEO) {
                                File file = AttachmentsStorage.getFileFromAttachment(attachment, getApplicationContext(), message.getRoomSecret());

                                String mimeType = "image";

                                if (attachment.getAttachmentType() == AttachmentType.VIDEO) {
                                    mimeType = "video";
                                }

                                if (file != null) {
                                    attachments.add(new FileInfo(file.getName(), file.getPath(), mimeType));
                                }
                            }
                        }
                    }

                }


                RecyclerView gallery = findViewById(R.id.gridView);
                mediaGridAdapter = new MediaGridAdapter(RoomInfoActivity.this, attachments, new MediaGridItemListener() {
                    @Override
                    public void onItemClick(int pos, View view) {
                        FileInfo fileInfo = attachments.get(pos);
                        Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
                        intent.putExtra(PreviewActivity.URI, fileInfo.getFilePath());
                        intent.putExtra(PreviewActivity.IS_VIDEO, fileInfo.isVideo());
                        startActivity(intent);
                    }
                }, gallery);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        gallery.setLayoutManager(new GridLayoutManager(RoomInfoActivity.this, 3));


                        findViewById(R.id.members_button).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getApplicationContext(), MembersActivity.class);
                                intent.putExtra(MembersActivity.ROOM_SECRET, roomSecret);
                                startActivity(intent);
                            }
                        });


                        gallery.setAdapter(mediaGridAdapter);
                        mediaGridAdapter.notifyDataSetChanged();


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

    private void applyColorTheme() {
        ColorTheme theme = AppTheme.getInstance().getColorTheme();

        ImageView button_back = findViewById(R.id.button_back);
        button_back.setColorFilter(theme.getAccentColor());
    }

    private void initNotificationButton() {
        LinearLayout button = findViewById(R.id.notification_button);

        ImageView bellOn = findViewById(R.id.notification_enabled_icon);
        ImageView bellOff = findViewById(R.id.notification_disabled_icon);

        if (!room.isNotificationsEnabled()) {
            bellOn.setVisibility(View.GONE);
            bellOff.setVisibility(View.VISIBLE);

        } else {
            bellOn.setVisibility(View.VISIBLE);
            bellOff.setVisibility(View.GONE);

        }

        button.setOnClickListener((View v) -> {
            if (room.isNotificationsEnabled()) {
                bellOn.setVisibility(View.GONE);
                bellOff.setVisibility(View.VISIBLE);

                room.setNotificationsEnabled(false);

            } else {
                bellOn.setVisibility(View.VISIBLE);
                bellOff.setVisibility(View.GONE);

                room.setNotificationsEnabled(true);

            }

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().update(room);
                }
            });
            thread.start();
        });
    }
}