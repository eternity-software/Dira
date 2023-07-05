package com.diraapp.appearance;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import android.graphics.BitmapFactory;

import com.diraapp.R;
import com.diraapp.activities.ChatAppearanceActivity;
import com.diraapp.storage.AppStorage;

import java.util.HashMap;

public class ChatBackground {

    private static final String BACKGROUND = "background_";

    private String name;

    private BackgroundType backgroundType;

    private String path;

    private static HashMap<BackgroundType, ChatBackground> backgrounds = new HashMap<>();

    public ChatBackground(String name, BackgroundType type) {
        this.name = name;
        this.backgroundType = type;
    }

    public ChatBackground(String name, String path, BackgroundType backgroundType) {
        this.name = name;
        this.path = path;
        this.backgroundType = backgroundType;
    }

    public static void initBackgrounds(Context context) {
        ChatBackground none = new ChatBackground(context.getResources().
                getString(R.string.background_none), BackgroundType.NONE);

        ChatBackground love = new ChatBackground(context.getResources().
                getString(R.string.background_love), BackgroundType.LOVE);

        ChatBackground pets = new ChatBackground(context.getResources().
                getString(R.string.background_pets), BackgroundType.PETS);

        ChatBackground education = new ChatBackground(context.getResources().
                getString(R.string.background_education), BackgroundType.EDUCATION);

        backgrounds.put(BackgroundType.NONE, none);
        backgrounds.put(BackgroundType.LOVE, love);
        backgrounds.put(BackgroundType.PETS, pets);
        backgrounds.put(BackgroundType.EDUCATION, education);
    }

    public static HashMap<BackgroundType, ChatBackground> getBackgrounds() {
        return backgrounds;
    }

    public String getName() {
        return name;
    }

    public BackgroundType getBackgroundType() {
        return backgroundType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getBitmap(Context context) {
        Bitmap bitmap = null;
        if (this.getBackgroundType().equals(BackgroundType.CUSTOM)) {
            bitmap = AppStorage.getBitmapFromPath(path);

            if (bitmap == null) {
                ChatBackground chatBackground = backgrounds.get(BackgroundType.NONE);
                AppTheme.getInstance().setChatBackground(chatBackground, context);
            }
        }
        return bitmap;
    }

    public Drawable getDrawable(Context context) {
        if (this.backgroundType.equals(BackgroundType.NONE)) {
            return null;
        }

        String name = BACKGROUND + this.backgroundType.toString().toLowerCase();
        int id = context.getResources().getIdentifier(name, "drawable",
                context.getPackageName());

        Drawable drawable = ContextCompat.getDrawable(context, id);

        return drawable;
    }

    public void applyBackground(ImageView view) {
        Bitmap bitmap = this.getBitmap(view.getContext());

        if (bitmap != null) {
            view.setImageBitmap(bitmap);
        } else {
            view.setImageDrawable(this.getDrawable(view.getContext()));
        }
    }

}
