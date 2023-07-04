package com.diraapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.diraapp.R;
import com.diraapp.adapters.ChatBackgroundAdapter;
import com.diraapp.adapters.ColorThemeAdapter;
import com.diraapp.appearance.ChatBackground;
import com.diraapp.appearance.ColorTheme;
import com.diraapp.db.entities.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatAppearanceActivity extends AppCompatActivity {

    private RecyclerView colorRecycler;
    private RecyclerView backgroundRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_appearance);

        initArrowBack();

        initChoosers();
        initExample();
    }

    private void initChoosers() {
        RecyclerView.LayoutManager manager = new LinearLayoutManager
                (getApplicationContext());
        LinearLayoutManager horizontalLayout = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);

        colorRecycler = findViewById(R.id.color_scheme_recycler);
        colorRecycler.setLayoutManager(manager);
        colorRecycler.setLayoutManager(horizontalLayout);

        RecyclerView.LayoutManager manager1 = new LinearLayoutManager
                (getApplicationContext());
        LinearLayoutManager horizontalLayout1 = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        backgroundRecycler = findViewById(R.id.background_recycler);
        backgroundRecycler.setLayoutManager(manager1);
        backgroundRecycler.setLayoutManager(horizontalLayout1);

        List<ColorTheme> colorThemes = new ArrayList<>();
        for (String key: ColorTheme.getColorThemes().keySet()) {
            colorThemes.add(ColorTheme.getColorThemes().get(key));
        }
        ColorThemeAdapter colorThemeAdapter = new
                ColorThemeAdapter(this, colorThemes);
        colorRecycler.setAdapter(colorThemeAdapter);

        List<ChatBackground> chatBackgrounds = new ArrayList<>();
        for (String key: ChatBackground.getBackgrounds().keySet()) {
            chatBackgrounds.add(ChatBackground.getBackgrounds().get(key));
        }
        ChatBackgroundAdapter chatBackgroundAdapter = new
                ChatBackgroundAdapter(this, chatBackgrounds);
        backgroundRecycler.setAdapter(chatBackgroundAdapter);
    }

    private void initExample() {

    }

    private void initArrowBack() {
        ImageView arrowBack = findViewById(R.id.button_back);
        arrowBack.setOnClickListener((View v) -> onBackPressed());
    }
}