package ru.dira.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.dira.R;
import ru.dira.adapters.RoomMessagesAdapter;
import ru.dira.api.requests.SendMessageRequest;
import ru.dira.api.updates.NewMessageUpdate;
import ru.dira.api.updates.Update;
import ru.dira.api.updates.UpdateType;
import ru.dira.attachments.ImageStorage;
import ru.dira.bottomsheet.filepicker.FilePickerBottomSheet;
import ru.dira.components.FilePreview;
import ru.dira.db.DiraMessageDatabase;
import ru.dira.db.DiraRoomDatabase;
import ru.dira.db.entities.Member;
import ru.dira.db.entities.Message;
import ru.dira.db.entities.Room;
import ru.dira.exceptions.UnablePerformRequestException;
import ru.dira.notifications.Notifier;
import ru.dira.services.UpdateListener;
import ru.dira.services.UpdateProcessor;
import ru.dira.services.UpdateProcessorListener;
import ru.dira.utils.CacheUtils;
import ru.dira.utils.SliderActivity;

public class RoomActivity extends AppCompatActivity implements UpdateListener, UpdateProcessorListener {

    public static String pendingRoomName;
    public static String pendingRoomSecret;

    private String roomSecret;
    private Room room;
    private RoomMessagesAdapter roomMessagesAdapter;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);

        this.roomSecret = pendingRoomSecret;

        TextView nameView = findViewById(R.id.room_name);
        nameView.setText(pendingRoomName);

        UpdateProcessor.getInstance().addProcessorListener(this);

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.room_info_pan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomActivity.this, RoomInfoActivity.class);
                intent.putExtra(RoomInfoActivity.ROOM_SECRET_EXTRA, roomSecret);
                startActivity(intent);
            }
        });


        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.message_text_input);
                if(sendTextMessage(editText.getText().toString()))
                {
                    editText.setText("");
                }
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        UpdateProcessor.getInstance().addUpdateListener(this);

        Thread loadMessagesHistory = new Thread(new Runnable() {
            @Override
            public void run() {
                roomMessagesAdapter = new RoomMessagesAdapter(RoomActivity.this);

                messageList = DiraMessageDatabase.getDatabase(getApplicationContext()).getMessageDao().getAllMessageByUpdatedTime(roomSecret);
                roomMessagesAdapter.setMessages(messageList);
                loadMembers();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(roomMessagesAdapter);
                        roomMessagesAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        loadMessagesHistory.start();
        loadData();

        findViewById(R.id.attach_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePickerBottomSheet filePickerBottomSheet = new FilePickerBottomSheet();
                filePickerBottomSheet.show(getSupportFragmentManager(), "blocked");
                filePickerBottomSheet.setRunnable(new FilePickerBottomSheet.ItemClickListener() {
                    @Override
                    public void onItemClick(int pos, final View view) {
                        ImageSendActivity.open(RoomActivity.this, filePickerBottomSheet.getMedia().get(pos).getFilePath(), "",
                                (FilePreview) view, ImageSendActivity.IMAGE_PURPOSE_MESSAGE);


                    }
                });
            }
        });

        Notifier.cancelAllNotifications(getApplicationContext());

    }

    private void loadMembers()
    {
        List<Member> memberList = DiraRoomDatabase.getDatabase(getApplicationContext()).getMemberDao().getMembersByRoomSecret(roomSecret);

        HashMap<String, Member> memberHashMap = new HashMap<>();


        for(Member member : memberList)
        {
            System.out.println(member.getNickname());
            memberHashMap.put(member.getId(), member);
        }
        roomMessagesAdapter.setMembers(memberHashMap);
    }

    private void loadData()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RoomActivity.this.room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);
                room.setUpdatedRead(true);
                DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().update(room);

                loadMembers();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(room.getImagePath() != null)
                        {
                            ImageView roomPicture = findViewById(R.id.room_picture);
                            roomPicture.setImageBitmap(ImageStorage.getImage(room.getImagePath()));
                        }

                        TextView roomName = findViewById(R.id.room_name);
                        TextView membersCount = findViewById(R.id.members_count);

                        membersCount.setText(getString(R.string.members_count).replace("%s",
                                String.valueOf(roomMessagesAdapter.getMembers().size() + 1)));
                        roomName.setText(room.getName());
                    }
                });
            }
        });
        thread.start();
    }


    public boolean sendTextMessage(String text) {
        while (text.contains("  "))
        {
            text = text.replace(" ", "");
        }

        while (text.contains("\n\n\n"))
        {
            text = text.replace("\n\n\n", "\n");
        }

        if(text.replace(" ", "").replace("\n", "").length() != 0) {
            Message message = Message.generateMessage(getApplicationContext(), roomSecret);
            message.setText(text);
            SendMessageRequest sendMessageRequest = new SendMessageRequest(message);
            try {
                UpdateProcessor.getInstance().sendRequest(sendMessageRequest);
            } catch (UnablePerformRequestException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateProcessor.getInstance().removeUpdateListener(this);
        UpdateProcessor.getInstance().removeProcessorListener(this);
    }

    @Override
    public void onUpdate(Update update) {
        if(update.getUpdateType() == UpdateType.NEW_MESSAGE_UPDATE)
        {
            NewMessageUpdate newMessageUpdate = (NewMessageUpdate) update;
            if(!newMessageUpdate.getMessage().getRoomSecret().equals(roomSecret)) return;

            RoomActivity.this.room = DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().getRoomBySecretName(roomSecret);
            room.setUpdatedRead(true);
            DiraRoomDatabase.getDatabase(getApplicationContext()).getRoomDao().update(room);

            messageList.add(0, newMessageUpdate.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    roomMessagesAdapter.notifyItemInserted(0);
                    RecyclerView recyclerView = findViewById(R.id.recycler_view);
                    recyclerView.scrollToPosition(0);
                }
            });
        }
        else if(update.getUpdateType() == UpdateType.ROOM_UPDATE)
        {

            loadData();
        }
        else if(update.getUpdateType() == UpdateType.MEMBER_UPDATE)
        {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    loadMembers();
                }
            });
            thread.start();
        }
    }

    @Override
    public void onSocketsCountChange(float percentOpened) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = findViewById(R.id.status_light);
                if(percentOpened != 1)
                {
                    if(percentOpened == 0)
                    {
                        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                    else
                    {
                        imageView.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.yellow),android.graphics.PorterDuff.Mode.SRC_IN);

                    }
                    imageView.setVisibility(View.VISIBLE);
                }
                else
                {
                    findViewById(R.id.status_light).setVisibility(View.GONE);
                }
            }
        });
    }
}