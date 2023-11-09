package com.diraapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.LanguageParsingException;
import com.diraapp.res.Theme;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.adapters.messages.views.BalloonMessageMenu;
import com.diraapp.ui.waterfalls.WaterfallBalancer;
import com.diraapp.ui.adapters.ChatBackgroundAdapter;
import com.diraapp.ui.adapters.ColorThemeAdapter;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.MessagesAdapter;
import com.diraapp.ui.adapters.messages.legacy.MessageReplyListener;
import com.diraapp.ui.adapters.messages.views.viewholders.factories.RoomViewHolderFactory;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.appearance.BackgroundType;
import com.diraapp.ui.appearance.ChatBackground;
import com.diraapp.ui.appearance.ColorTheme;
import com.diraapp.ui.bottomsheet.filepicker.FilePickerBottomSheet;
import com.diraapp.ui.components.MediaGridItem;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.utils.CacheUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ChatAppearanceActivity extends DiraActivity {

    private RecyclerView colorRecycler;

    private ColorThemeAdapter colorThemeAdapter;

    private RecyclerView backgroundRecycler;

    private ChatBackgroundAdapter chatBackgroundAdapter;

    private MessagesAdapter messagesAdapter;

    private FilePickerBottomSheet bottomSheet;

    private int rand = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_appearance);

        initArrowBack();

        initSelectors();
        initExample();

        initPickImageButton();
    }

    private void initSelectors() {
        RecyclerView.LayoutManager colorManager = new LinearLayoutManager
                (getApplicationContext());
        LinearLayoutManager colorHorizontalLayout = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);

        colorRecycler = findViewById(R.id.color_scheme_recycler);
        colorRecycler.setLayoutManager(colorManager);
        colorRecycler.setLayoutManager(colorHorizontalLayout);

        RecyclerView.LayoutManager backgroundManager = new LinearLayoutManager
                (getApplicationContext());
        LinearLayoutManager backgroundHorizontalLayout = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        backgroundRecycler = findViewById(R.id.background_recycler);
        backgroundRecycler.setLayoutManager(backgroundManager);
        backgroundRecycler.setLayoutManager(backgroundHorizontalLayout);

        List<ColorTheme> colorThemes = ColorTheme.getColorThemeList();
        colorThemeAdapter = new
                ColorThemeAdapter(this, colorThemes);
        colorThemeAdapter.setListener(new ColorThemeAdapter.SelectorListener() {
            @Override
            public void onSelectorClicked() {
                try {
                    Theme.loadCurrentTheme(getApplicationContext());
                } catch (LanguageParsingException e) {

                }
                initExample();
            }
        });

        colorRecycler.setAdapter(colorThemeAdapter);

        List<ChatBackground> chatBackgrounds = ChatBackground.getChatBackgrounds();
        chatBackgroundAdapter = new
                ChatBackgroundAdapter(this, chatBackgrounds,
                new ChatBackgroundAdapter.SelectorListener() {
                    @Override
                    public void onSelectorClicked(ChatBackground background) {
                        ImageView imageView = findViewById(R.id.example_background);

                        background.applyBackground(imageView);
                    }
                });
        backgroundRecycler.setAdapter(chatBackgroundAdapter);
    }

    private void initExample() {
        CacheUtils cacheUtils = new CacheUtils(this);
        String authorName = cacheUtils.getString(CacheUtils.ID);

        long keyTime = 1;

        String secretName = "1111";
        Room room = new Room(null, keyTime, secretName, null, false, new ArrayList<>());
        room.setTimeEncryptionKeyUpdated(keyTime);

        Message senderMessage = new Message();

        int height = -1;
        ImageView backgroundView = findViewById(R.id.example_background);
        RecyclerView recycler = findViewById(R.id.example_messages);
        CardView exampleContainer = findViewById(R.id.example_container);
        if (rand == -1) {
            rand = new Random().nextInt(2);
            rand++;
        } else {
            height = exampleContainer.getMeasuredHeight();
        }

        Member ame = new Member("0000", "Ame", null, secretName, System.currentTimeMillis());
        HashMap<String, Member> members = new HashMap<>();
        members.put(ame.getId(), ame);

        senderMessage.setText(this.getResources().getString(AppStorage.getResId("chat_appearance_example_message_self_" + rand, R.string.class)));
        senderMessage.setAuthorId(authorName);
        senderMessage.setLastTimeEncryptionKeyUpdated(keyTime);

        Message secondMessage = new Message();
        secondMessage.setText(this.getResources().getString(AppStorage.getResId("chat_appearance_example_message_" + rand, R.string.class)));
        secondMessage.setAuthorNickname(ame.getNickname());
        secondMessage.setAuthorId(ame.getId());
        senderMessage.setRead(true);
        secondMessage.setLastTimeEncryptionKeyUpdated(keyTime);

        List<Message> messages = new ArrayList<>();
        messages.add(secondMessage);
        messages.add(senderMessage);

        MessageAdapterContract contract = new MessageAdapterContract() {
            @Override
            public WaterfallBalancer getWaterfallBalancer() {
                return null;
            }

            @Override
            public Room getRoom() {
                return room;
            }

            @Override
            public HashMap<String, Member> getMembers() {
                return members;
            }

            @Override
            public CacheUtils getCacheUtils() {
                return cacheUtils;
            }

            @Override
            public Context getContext() {
                return ChatAppearanceActivity.this.getApplicationContext();
            }

            @Override
            public MessageReplyListener getReplyListener() {
                return null;
            }

            @Override
            public void runOnUiThread(Runnable runnable) {
                runOnUiThread(runnable);
            }

            @Override
            public void onFirstMessageScrolled(Message message, int index) {
            }

            @Override
            public PreparedActivity preparePreviewActivity(String filePath, boolean isVideo, Bitmap preview, View transitionSource) {
                return null;
            }

            @Override
            public void attachVideoPlayer(DiraVideoPlayer player) {
            }

            @Override
            public void addListener(DiraActivityListener player) {

            }

            @Override
            public BalloonMessageMenu.BalloonMenuListener getBalloonMessageListener() {
                return null;
            }
        };

        messagesAdapter = new MessagesAdapter(contract, messages, room,
                new AsyncLayoutInflater(this.getBaseContext()), new RoomViewHolderFactory(), cacheUtils);

        AppTheme.getInstance().getChatBackground().applyBackground(backgroundView);

        recycler.setAdapter(messagesAdapter);

        if (height != -1) {
            exampleContainer.getLayoutParams().height = height;
        }
    }

    private void initPickImageButton() {
        LinearLayout pickImage = findViewById(R.id.pick_image_button);

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheet = new FilePickerBottomSheet();
                bottomSheet.setOnlyImages(true);
                bottomSheet.show(getSupportFragmentManager(), "blocked");

                bottomSheet.setRunnable(new MediaGridItemListener() {
                    @Override
                    public void onItemClick(int pos, View view) {
                        MediaSendActivity.open(ChatAppearanceActivity.this,
                                bottomSheet.getMedia().get(pos).getFilePath(), "",
                                (MediaGridItem) view, MediaSendActivity.IMAGE_PURPOSE_SELECT);
                    }

                    @Override
                    public void onLastItemLoaded(int pos, View view) {

                    }
                });
            }
        });
    }

    private void initArrowBack() {
        ImageView arrowBack = findViewById(R.id.button_back);
        arrowBack.setOnClickListener((View v) -> onBackPressed());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK && resultCode != MediaSendActivity.CODE) return;

        if (resultCode == MediaSendActivity.CODE) {
            if (bottomSheet != null) {

                /**
                 * Throws an java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                 * on some devices (tested on Android 5.1)
                 */
                try {
                    bottomSheet.dismiss();
                } catch (Exception ignored) {
                }
            }

            String path = data.getStringArrayListExtra("uris").get(0);

            applyCustomBackground(path);
        }
    }

    private void applyCustomBackground(String path) {
        ChatBackground background = new ChatBackground(
                BackgroundType.CUSTOM.toString(), path, BackgroundType.CUSTOM);

        AppTheme.getInstance().setChatBackground
                (background, ChatAppearanceActivity.this);

        chatBackgroundAdapter.notifyDataSetChanged();

        ImageView backgroundView = findViewById(R.id.example_background);
        AppTheme.getInstance().getChatBackground().applyBackground(backgroundView);
    }
}