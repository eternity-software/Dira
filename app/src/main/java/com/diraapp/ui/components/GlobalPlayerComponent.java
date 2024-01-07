package com.diraapp.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.res.Theme;
import com.diraapp.ui.components.dynamic.ThemeLinearLayout;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayer;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayerListener;
import com.diraapp.utils.Logger;
import com.diraapp.utils.android.DeviceUtils;

import java.io.File;
import java.util.Objects;

public class GlobalPlayerComponent extends ThemeLinearLayout implements GlobalMediaPlayerListener {

    private long currentMessageDuration = 0;

    private final int dp40;

    private String currentMessageDurationString = "00:00";

    private TextView authorText;
    private TextView timeView;

    private ImageView playButton;
    private ImageView closeButton;

    private boolean isPlayButtonActive = true;
    private int playButtonColor;

    private boolean isHidden = true;

    public GlobalPlayerComponent(Context context) {
        super(context);
        dp40 = DeviceUtils.dpToPx(40, getContext());
        init(context);
    }

    public GlobalPlayerComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dp40 = DeviceUtils.dpToPx(40, getContext());
        init(context);
    }

    public GlobalPlayerComponent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        dp40 = DeviceUtils.dpToPx(40, getContext());
        init(context);
    }


    private void init(Context context) {
        inflate(context);

        authorText = findViewById(R.id.author_name);
        timeView = findViewById(R.id.time_view);
        playButton = findViewById(R.id.play_button);
        closeButton = findViewById(R.id.close_button);

        playButtonColor = Theme.getColor(getContext(), R.color.accent);

        playButton.setOnClickListener((View v) -> {
            GlobalMediaPlayer.getInstance().onPaused();
        });

        closeButton.setOnClickListener((View v) -> {
            GlobalMediaPlayer.getInstance().onClose();
        });

        GlobalMediaPlayer.getInstance().registerListener(this);

        resume();

    }

    private void inflate(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.global_player_component, this);

        setBackground(ContextCompat.getDrawable(context, R.color.dark));

        setVisibility(GONE);

        LinearLayout.LayoutParams params = new
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp40);

        params.gravity = Gravity.CENTER_VERTICAL;
        this.setLayoutParams(params);
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

        ValueAnimator animator = ValueAnimator.ofInt(dp40, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                if (!isHidden) return;

                int value = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams params = GlobalPlayerComponent.this.getLayoutParams();
                params.height = value;
                GlobalPlayerComponent.this.setLayoutParams(params);

                if (value == 0) setVisibility(GONE);
            }
        });
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.setDuration(150);
        animator.start();

        isHidden = true;
    }

    private void show() {
        if (!isHidden) return;

        ValueAnimator animator = ValueAnimator.ofInt(0, dp40);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                if (isHidden) return;

                int value = (int) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams params = GlobalPlayerComponent.this.getLayoutParams();
                params.height = value;
                GlobalPlayerComponent.this.setLayoutParams(params);
            }
        });
        animator.setInterpolator(new DecelerateInterpolator(2f));
        animator.setDuration(150);

        setVisibility(VISIBLE);
        animator.start();

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

        currentMessageDurationString = getTimeString(currentMessageDuration);
    }

    private String getTimeString(long millis) {
        long seconds = millis / 1000;

        long s = seconds % 60;
        String st = String.valueOf(s);
        if (st.length() == 1) st = "0" + st;

        long m = (seconds / 60) % 60;
        String mt = String.valueOf(m);
        if (mt.length() == 1) mt = "0" + mt;

        return mt + ":" + st;
    }

    private void fillTimeView(float currentProgress) {
        fillTimeView(currentProgress, null);
    }

    private void fillTimeView(float currentProgress, File file) {
        currentProgress = currentProgress / 10;

        String s = getTimeString((long) (currentMessageDuration * currentProgress));

        if (file != null) readDuration(file);

        String time = s + " / " + currentMessageDurationString;

        timeView.setText(time);
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

        fillTimeView(progress);

        show();
    }

    @Override
    public void onGlobalMediaPlayerClose() {
        hide();
    }

    @Override
    public void onGlobalMediaPlayerStart(Message message, File file) {
        if (!GlobalMediaPlayer.getInstance().isActive()) return;

        setAuthorName(message);
        fillTimeView(0, file);

        setPauseButton();

        show();
    }

    @Override
    public void onGlobalMediaPlayerProgressChanged(float progress, Message message) {
        show();

        fillTimeView(progress);
    }

    private void resume() {
        GlobalMediaPlayer global = GlobalMediaPlayer.getInstance();

        if (!global.isActive()) return;

        setAuthorName(global.getCurrentMessage());
        fillTimeView(global.getCurrentProgress(), global.getCurrentFile());

        onGlobalMediaPlayerPauseClicked(global.isPaused(), global.getCurrentProgress());
    }

    public void release() {
        GlobalMediaPlayer.getInstance().removeListener(this);
    }
}
