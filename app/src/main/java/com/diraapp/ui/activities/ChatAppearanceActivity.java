package com.diraapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.ui.adapters.ChatBackgroundAdapter;
import com.diraapp.ui.adapters.ColorThemeAdapter;
import com.diraapp.ui.adapters.MediaGridItemListener;
import com.diraapp.ui.adapters.messageAdapter.RoomMessagesAdapter;
import com.diraapp.ui.appearance.AppTheme;
import com.diraapp.ui.appearance.BackgroundType;
import com.diraapp.ui.appearance.ChatBackground;
import com.diraapp.ui.appearance.ColorTheme;
import com.diraapp.ui.bottomsheet.filepicker.FilePickerBottomSheet;
import com.diraapp.ui.components.FilePreview;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.utils.CacheUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatAppearanceActivity extends AppCompatActivity {

    private RecyclerView colorRecycler;

    private ColorThemeAdapter colorThemeAdapter;

    private RecyclerView backgroundRecycler;

    private ChatBackgroundAdapter chatBackgroundAdapter;

    private RoomMessagesAdapter roomMessagesAdapter;

    private FilePickerBottomSheet bottomSheet;

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
                roomMessagesAdapter.notifyDataSetChanged();
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

        Message senderMessage = new Message();

        int rand = new Random().nextInt(2);
        rand++;

        senderMessage.setText(this.getResources().getString(AppStorage.getResId("chat_appearance_example_message_self_" + rand, R.string.class)));
        senderMessage.setAuthorId(authorName);

        Message secondMessage = new Message();
        secondMessage.setText(this.getResources().getString(AppStorage.getResId("chat_appearance_example_message_" + rand, R.string.class)));
        secondMessage.setAuthorNickname("Ame");
        secondMessage.setAuthorId("0000");

        List<Message> messages = new ArrayList<>();
        messages.add(secondMessage);
        messages.add(senderMessage);

        RecyclerView recycler = findViewById(R.id.example_messages);
        roomMessagesAdapter = new RoomMessagesAdapter(this, "1111", null);
        roomMessagesAdapter.setMessages(messages);

        ImageView backgroundView = findViewById(R.id.example_background);
        AppTheme.getInstance().getChatBackground().applyBackground(backgroundView);

        recycler.setAdapter(roomMessagesAdapter);
    }

    private void initPickImageButton() {
        LinearLayout pickImage = findViewById(R.id.pick_image_button);

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheet = new FilePickerBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "blocked");

                bottomSheet.setRunnable(new MediaGridItemListener() {
                    @Override
                    public void onItemClick(int pos, View view) {
                        ImageSendActivity.open(ChatAppearanceActivity.this,
                                bottomSheet.getMedia().get(pos).getFilePath(), "",
                                (FilePreview) view, ImageSendActivity.IMAGE_PURPOSE_SELECT);
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

        if (resultCode != RESULT_OK && resultCode != ImageSendActivity.CODE) return;

        if (resultCode == ImageSendActivity.CODE) {
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

            String path = data.getStringExtra("uri");

            applyCustomBackground(path);
        }
    }

    private void applyCustomBackground(String path) {
        ChatBackground background = new ChatBackground(
                BackgroundType.CUSTOM.toString(), path, BackgroundType.CUSTOM);

        AppTheme.getInstance().setChatBackground
                (background, ChatAppearanceActivity.this);

        chatBackgroundAdapter.notifyDataSetChanged();

        System.out.println(path);
        ImageView backgroundView = findViewById(R.id.example_background);
        AppTheme.getInstance().getChatBackground().applyBackground(backgroundView);
    }
}