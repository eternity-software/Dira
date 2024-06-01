package com.diraapp.ui.activities.roominfo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.UpdateListener;
import com.diraapp.api.requests.CreateInviteRequest;
import com.diraapp.api.updates.MemberUpdate;
import com.diraapp.api.updates.NewInvitationUpdate;
import com.diraapp.api.updates.NewMessageUpdate;
import com.diraapp.api.updates.Update;
import com.diraapp.api.updates.UpdateType;
import com.diraapp.api.views.RoomMember;
import com.diraapp.db.DiraRoomDatabase;
import com.diraapp.db.daos.RoomDao;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.entities.rooms.RoomType;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.images.ImagesWorker;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.activities.EditRoomActivity;
import com.diraapp.ui.activities.MembersActivity;
import com.diraapp.ui.activities.NavigationActivity;
import com.diraapp.ui.adapters.roominfo.RoomInfoPagerAdapter;
import com.diraapp.ui.bottomsheet.InvitationCodeBottomSheet;
import com.diraapp.ui.bottomsheet.RoomEncryptionBottomSheet;
import com.diraapp.ui.bottomsheet.roomoptions.RoomOptionsBottomSheet;
import com.diraapp.ui.bottomsheet.roomoptions.RoomOptionsBottomSheetListener;
import com.diraapp.ui.components.DiraPopup;
import com.diraapp.ui.components.FadingImageView;
import com.diraapp.ui.components.mediatypeselector.MediaTypeSelector;
import com.diraapp.ui.components.mediatypeselector.MediaTypeSelectorListener;
import com.diraapp.ui.fragments.navigation.selector.RoomSelectorFragment;
import com.diraapp.ui.fragments.roominfo.BaseRoomInfoFragment;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.SliderActivity;
import com.diraapp.utils.StringFormatter;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.blurry.Blurry;

public class RoomInfoActivity extends DiraActivity implements UpdateListener,
        InvitationCodeBottomSheet.BottomSheetListener, RoomOptionsBottomSheetListener,
        MediaTypeSelectorListener, BaseRoomInfoFragment.RoomInfoFragmentListener {

    public static final String ROOM_SECRET_EXTRA = "roomSecret";
    public static final String MESSAGE_TO_SCROLL_TIME = "messageToScrollTime";
    public static final String MESSAGE_TO_SCROLL_ID = "messageToScrollId";

    public static final int RESULT_CODE_SCROLL_TO_MESSAGE = 99;

    private String roomSecret;
    private Room room;
    private List<Member> members;

    private ViewPager2 viewPager2;

    private RoomInfoPagerAdapter pagerAdapter;

    private MediaTypeSelector selector;

    private boolean isFragmentsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info);

        roomSecret = getIntent().getExtras().getString(ROOM_SECRET_EXTRA);

//        SliderActivity sliderActivity = new SliderActivity();
//        sliderActivity.attachSlider(this);

        findViewById(R.id.toolbar).getBackground().setAlpha(0);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)
                findViewById(R.id.app_bar_layout).getLayoutParams();
        params.setBehavior(new RoomInfoBarLayoutBehavior(this, null));

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


        findViewById(R.id.encryption_button).setOnClickListener((View v) -> {
            onEncryptionButtonClicked();
        });

        initMemberButton();
        UpdateProcessor.getInstance().addUpdateListener(this);

        selector = findViewById(R.id.media_type_selector);
        selector.setListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateProcessor.getInstance().removeUpdateListener(this);
    }

    private void initFragments() {
        isFragmentsInitialized = true;
        viewPager2 = findViewById(R.id.pager);

        pagerAdapter = new RoomInfoPagerAdapter(getSupportFragmentManager(),
                getLifecycle(), room, members);

        viewPager2.setAdapter(pagerAdapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                selector.setPosition(position);
            }
        });
    }

    private void initMemberButton() {

        findViewById(R.id.members_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MembersActivity.class);
                intent.putExtra(MembersActivity.ROOM_SECRET, roomSecret);
                startActivity(intent);
            }
        });


    }

    private void loadData() {
        runBackground(new Runnable() {
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
                            Bitmap bitmap = AppStorage.getBitmapFromPath(room.getImagePath(), getApplicationContext());
                            if (bitmap != null) {
                                roomPicture.setImageBitmap(bitmap);

                                FadingImageView blurryBackground = findViewById(R.id.blurred_picture);
                                blurryBackground.setEdgeLength(128);
                                blurryBackground.setFadeTop(true);
                                blurryBackground.setFadeBottom(true);
                                blurryBackground.setFadeLeft(true);
                                blurryBackground.setFadeRight(true);
                                blurryBackground.setScaleX(2f);
                                blurryBackground.setScaleY(2f);
                                blurryBackground.setAlpha(0.7f);

                                Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, false);

                                blurryBackground.setImageBitmap(bitmap1);


                                Blurry.with(getApplicationContext()).radius(18)
                                        .sampling(8).from(ImagesWorker.getRoundedCroppedBitmap(bitmap1))

                                        .into(blurryBackground);

                            }
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

                        initInviteButton();
                        initOptionsButton();
                        initStatuses();

                        membersCount.setText(getString(R.string.members_count).replace("%s", String.valueOf(members.size() + 1)));

                        if (!isFragmentsInitialized) initFragments();

                    }
                });

            }
        });

    }

    @Override
    public void onUpdate(Update update) {
        if (update.getUpdateType() == UpdateType.ROOM_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;
            loadData();
        } else if (update.getUpdateType() == UpdateType.MEMBER_UPDATE) {
            if (!update.getRoomSecret().equals(roomSecret)) return;
            MemberUpdate memberUpdate = (MemberUpdate) update;

            boolean isSelf = memberUpdate.getId().equals(
                    new CacheUtils(this).getString(CacheUtils.ID));
            if (isSelf) return;

            initInviteButton();

            if (room.getRoomType() == RoomType.PRIVATE) loadData();
        } else if (update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE) {
            NewMessageUpdate messageUpdate = (NewMessageUpdate) update;

            if (!messageUpdate.getMessage().getRoomSecret().equals(roomSecret)) return;

            boolean isSelf = new CacheUtils(this).getString(CacheUtils.ID).equals(
                    messageUpdate.getMessage().getAuthorId());
            if (isSelf) return;

            initInviteButton();
        } else if (update.getUpdateType() == UpdateType.RENEWING_CONFIRMED) {
            runBackground(() -> {
                room = DiraRoomDatabase.getDatabase(this).
                        getRoomDao().getRoomBySecretName(roomSecret);

                if (room.getEncryptionKey() != null) {
                    runOnMainThread(this::initStatuses);
                }
            });

        }
    }


    private void initNotificationButton() {
        LinearLayout button = findViewById(R.id.notification_button);

        updateNotification();

        button.setOnClickListener((View v) -> {
            onNotificationButtonClicked();
        });
    }

    private void updateNotification() {

        ImageView bellOn = findViewById(R.id.notification_enabled_icon);
        ImageView bellOff = findViewById(R.id.notification_disabled_icon);
        if (!room.isNotificationsEnabled()) {
            bellOn.setVisibility(View.VISIBLE);
            bellOff.setVisibility(View.GONE);

        } else {
            bellOn.setVisibility(View.GONE);
            bellOff.setVisibility(View.VISIBLE);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initInviteButton() {
        View inviteIcon = findViewById(R.id.icon_invite);

        if (room == null) return;

        if (room.getRoomType() == RoomType.PRIVATE && members.size() > 0) {
            inviteIcon.setVisibility(View.GONE);
            return;
        }

        inviteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInviteButtonClicked();
            }
        });
    }

    private void initOptionsButton() {
        findViewById(R.id.room_options_button).setOnClickListener((View v) -> {
            boolean showInviteButton = !(room.getRoomType() == RoomType.PRIVATE && members.size() > 0);

            RoomOptionsBottomSheet bottomSheet = new RoomOptionsBottomSheet(
                    showInviteButton, room.getName(), room.getImagePath(), room.isNotificationsEnabled(), this);
            bottomSheet.show(getSupportFragmentManager(), "Options bottom sheet");
        });
    }

    private void initStatuses() {

        boolean noKey = room.getEncryptionKey().equals(StringFormatter.EMPTY_STRING);
        if (noKey) {
            findViewById(R.id.room_info_room_not_encrypted).setVisibility(View.VISIBLE);
            findViewById(R.id.room_info_room_encrypted).setVisibility(View.GONE);
        } else {
            findViewById(R.id.room_info_room_not_encrypted).setVisibility(View.GONE);
            findViewById(R.id.room_info_room_encrypted).setVisibility(View.VISIBLE);
        }

        boolean isEmptyPrivate = room.getRoomType() == RoomType.PRIVATE && members.size() == 0;
        if (isEmptyPrivate) {
            findViewById(R.id.room_info_empty_private_room).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.room_name)).setText(getText(R.string.room_type_private));
        }

        boolean isPrivateNotEmpty = room.getRoomType() == RoomType.PRIVATE && members.size() != 0;
        if (isPrivateNotEmpty) {
            findViewById(R.id.room_info_empty_private_room).setVisibility(View.GONE);
        }

        boolean showEncryptionButton = room.getRoomType() == RoomType.PRIVATE && members.size() > 0;
        if (showEncryptionButton) {
            showEncryptionButton();
        }

        boolean isPrivate = room.getRoomType() == RoomType.PRIVATE;
        if (isPrivate) {
            findViewById(R.id.public_room_panel).setVisibility(View.GONE);
        }
    }

    private void showInviteButton() {
        findViewById(R.id.encryption_button).setVisibility(View.GONE);
        findViewById(R.id.icon_invite).setVisibility(View.VISIBLE);

    }

    private void showEncryptionButton() {
        findViewById(R.id.icon_invite).setVisibility(View.GONE);
        findViewById(R.id.encryption_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onNotificationButtonClicked() {
        if (room.isNotificationsEnabled()) {
            room.setNotificationsEnabled(false);
            Toast.makeText(getApplicationContext(), getString(R.string.notifications_disabled),
                    Toast.LENGTH_SHORT).show();

        } else {
            room.setNotificationsEnabled(true);
            Toast.makeText(getApplicationContext(), getString(R.string.notifications_enabled),
                    Toast.LENGTH_SHORT).show();
        }

        updateNotification();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RoomDao roomDao = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao();
                Room dbRoom = roomDao.getRoomBySecretName(roomSecret);

                dbRoom.setNotificationsEnabled(room.isNotificationsEnabled());
                roomDao.update(room);
            }
        });
        thread.start();
    }

    @Override
    public void onInviteButtonClicked() {
        View inviteIcon = findViewById(R.id.icon_invite);
        if (room.getRoomType() == RoomType.PRIVATE && members.size() > 0) {
            inviteIcon.setVisibility(View.GONE);
            inviteIcon.setClickable(false);
            return;
        }

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
                roomMembers, room.getRoomType());

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
                                    invitationCodeBottomSheet.setOfficialServer(
                                            room.getServerAddress().equals(UpdateProcessor.OFFICIAL_ADDRESS));

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

    @Override
    public void onEncryptionButtonClicked() {
        if (room == null) return;
        RoomEncryptionBottomSheet roomEncryptionBottomSheet = new RoomEncryptionBottomSheet(room);
        roomEncryptionBottomSheet.show(getSupportFragmentManager(), "");
    }

    @Override
    public void onRoomDeleteClicked() {
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
                                        Intent intent = new Intent(RoomInfoActivity.this, NavigationActivity.class);
                                        intent.putExtra(RoomSelectorFragment.CAN_BE_BACK_PRESSED, false);
                                        startActivity(intent);
                                    }
                                });
                            }
                        });
                        deletionThread.start();

                    }
                });
    }

    @Override
    public void onSelected(int position) {

        viewPager2.setCurrentItem(position);
    }

    @Override
    public void scrollToMessage(String messageId, long messageTime) {
        Intent data = new Intent();

        data.putExtra(MESSAGE_TO_SCROLL_ID, messageId);
        data.putExtra(MESSAGE_TO_SCROLL_TIME, messageTime);

        setResult(RESULT_OK, data);
        finish();
    }
}