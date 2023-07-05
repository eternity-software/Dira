package com.diraapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.diraapp.R;
import com.diraapp.adapters.ChatBackgroundAdapter;
import com.diraapp.adapters.ColorThemeAdapter;
import com.diraapp.adapters.RoomMessagesAdapter;
import com.diraapp.appearance.AppTheme;
import com.diraapp.appearance.BackgroundType;
import com.diraapp.appearance.ChatBackground;
import com.diraapp.appearance.ColorTheme;
import com.diraapp.appearance.ColorThemeType;
import com.diraapp.db.entities.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.utils.CacheUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChatAppearanceActivity extends AppCompatActivity {

    private RecyclerView colorRecycler;
    private RecyclerView backgroundRecycler;

    private RoomMessagesAdapter roomMessagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_appearance);

        initArrowBack();

        initSelectors();
        initExample();
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

        List<ColorTheme> colorThemes = new ArrayList<>();
        for (ColorThemeType key: ColorTheme.getColorThemes().keySet()) {
            colorThemes.add(ColorTheme.getColorThemes().get(key));
        }
        ColorThemeAdapter colorThemeAdapter = new
                ColorThemeAdapter(this, colorThemes);
        colorThemeAdapter.setListener(new ColorThemeAdapter.SelectorListener() {
            @Override
            public void onSelectorClicked() {
                roomMessagesAdapter.notifyDataSetChanged();
            }
        });
        colorRecycler.setAdapter(colorThemeAdapter);

        List<ChatBackground> chatBackgrounds = new ArrayList<>();
        for (BackgroundType key: ChatBackground.getBackgrounds().keySet()) {
            chatBackgrounds.add(ChatBackground.getBackgrounds().get(key));
        }
        ChatBackgroundAdapter chatBackgroundAdapter = new
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
        roomMessagesAdapter = new RoomMessagesAdapter(this, "1111");
        roomMessagesAdapter.setMessages(messages);

        ImageView backgroundView = findViewById(R.id.example_background);
        AppTheme.getInstance().getChatBackground().applyBackground(backgroundView);

        recycler.setAdapter(roomMessagesAdapter);
    }

    private void initArrowBack() {
        ImageView arrowBack = findViewById(R.id.button_back);
        arrowBack.setOnClickListener((View v) -> onBackPressed());
    }
}