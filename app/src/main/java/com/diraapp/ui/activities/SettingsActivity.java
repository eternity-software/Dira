package com.diraapp.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diraapp.BuildConfig;
import com.diraapp.R;
import com.diraapp.device.PerformanceTester;
import com.diraapp.utils.SliderActivity;

public class SettingsActivity extends DiraActivity {

    private ImageView arrowBack;

    private LinearLayout memoryManagementButton;

    private LinearLayout roomServersButton;

    private LinearLayout fileServersButton;

    private LinearLayout chatsButton;

    private TextView privacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SliderActivity sliderActivity = new SliderActivity();
        sliderActivity.attachSlider(this);



        initViews();
    }

    private void initViews() {
        arrowBack = findViewById(R.id.button_back);
        arrowBack.setOnClickListener((View v) -> onBackPressed());

        memoryManagementButton = findViewById(R.id.button_memory_management);
        memoryManagementButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(SettingsActivity.this, MemoryManagementActivity.class);
            startActivity(intent);
        });

        roomServersButton = findViewById(R.id.button_room_servers);
        roomServersButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(SettingsActivity.this, RoomServersActivity.class);
            startActivity(intent);
        });

        chatsButton = findViewById(R.id.button_chats);
        chatsButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(SettingsActivity.this, ChatAppearanceActivity.class);
            startActivity(intent);
        });

        privacyPolicy = findViewById(R.id.privacy_policy);
        privacyPolicy.setOnClickListener((View v) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://diraapp.com/"));
            startActivity(intent);
        });
    }

}