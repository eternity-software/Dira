package com.diraapp.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.diraapp.R;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CrashActivity extends DiraActivity {

    public static Throwable PENDING_ERROR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        findViewById(R.id.button_copy).setOnClickListener((v) -> {

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Dira Crash", getIntent().getExtras().getString("ex"));
            clipboard.setPrimaryClip(clip);
        });

        findViewById(R.id.button_reopen).setOnClickListener((v) -> {
            Intent intent = new Intent(CrashActivity.this, RoomSelectorActivity.class);
            startActivity(intent);
            finish();
        });
    }
}