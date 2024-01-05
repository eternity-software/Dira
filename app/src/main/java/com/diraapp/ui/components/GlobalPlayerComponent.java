package com.diraapp.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.res.Theme;
import com.diraapp.ui.components.dynamic.ThemeLinearLayout;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayer;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayerListener;
import com.diraapp.utils.android.DeviceUtils;

import java.io.File;
import java.util.Objects;

public class GlobalPlayerComponent extends ThemeLinearLayout implements GlobalMediaPlayerListener {

    private long currentMessageDuration = 0;

    private String currentMessageDurationString = "00:00";

    private TextView authorText;
    private TextView dateText;

    private ImageView playButton;
    private ImageView closeButton;

    private boolean isPlayButtonActive = false;
    private int playButtonColor;

    private boolean isHidden = true;

    public GlobalPlayerComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        inflate();

        // scale animation

        authorText = findViewById(R.id.author_name);
        dateText = findViewById(R.id.time_view);
        playButton = findViewById(R.id.play_button);
        closeButton = findViewById(R.id.close_button);

        playButtonColor = Theme.getColor(getContext(), R.color.accent);

        playButton.setOnClickListener((View v) -> {
            GlobalMediaPlayer.getInstance().onPaused();
        });

        closeButton.setOnClickListener((View v) -> {
            GlobalMediaPlayer.getInstance().onClose();
            hide();
        });

    }

    private void inflate() {
        int dp32 = DeviceUtils.dpToPx(32, this.getContext());
        setPadding(dp32, 0 , 0, dp32);

        setOrientation(HORIZONTAL);

        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp32);

        params.gravity = Gravity.CENTER_VERTICAL;
        this.setLayoutParams(params);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.global_player_component, this);
    }

    public void setPlayButton() {
        if (isPlayButtonActive) return;

        playButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play));
        playButton.setColorFilter(playButtonColor);

        isPlayButtonActive = true;
    }

    public void setPauseButton() {
        if (!isPlayButtonActive) return;

        playButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_pause));
        playButton.setColorFilter(playButtonColor);

        isPlayButtonActive = false;
    }

    private void hide() {
        if (isHidden) return;

        //anim
        isHidden = true;
    }

    private void show() {
        if (!isHidden) return;

        //anim
        isHidden = false;
    }

    private void readDuration(File file) {
        try {
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(getContext(), Uri.fromFile(file));

            currentMessageDuration = Long.parseLong(Objects.requireNonNull(
                    metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

            metaRetriever.release();
        } catch (Exception e) {
            e.printStackTrace();
            currentMessageDuration = 60_000;
        }
    }

    private void setAuthorName(Message message) {
        String text;

        if (message.hasAuthor()) {
            text = message.getShortAuthorNickname();
        } else {
            text = getContext().getString(R.string.unknown);
        }

        authorText.setText(text);
    }

    @Override
    public void onGlobalMediaPlayerPauseClicked(boolean isPaused, float progress) {
        if (isPaused) {
            setPlayButton();
        } else {
            setPauseButton();
        }

        //update progress text
    }

    @Override
    public void onGlobalMediaPlayerClose() {
        hide();
    }

    @Override
    public void onGlobalMediaPlayerStart(Message message, File file) {
        show();

        readDuration(file);
        setAuthorName(message);

        setPauseButton();
    }

    @Override
    public void onGlobalMediaPlayerProgressChanged(float progress, Message message) {
        show();
        long progressMillis = (long) progress * currentMessageDuration;
        //update progress text
    }
}
