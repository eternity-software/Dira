package com.diraapp.appearance;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.diraapp.R;
import com.diraapp.storage.AppStorage;

import java.util.HashMap;

public class ChatBackground {

    private static final String BACKGROUND = "background_";

    private String name;

    private BackgroundType backgroundType;

    private String path;

    private static HashMap<String, ChatBackground> backgrounds = new HashMap<>();

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

        backgrounds.put(BackgroundType.NONE.toString(), none);
        backgrounds.put(BackgroundType.LOVE.toString(), love);
        backgrounds.put(BackgroundType.PETS.toString(), pets);
        backgrounds.put(BackgroundType.EDUCATION.toString(), education);
    }

    public static HashMap<String, ChatBackground> getBackgrounds() {
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

    public Bitmap getBitMap(Context context) {
        Bitmap bitmap = null;
        if (this.getName().toUpperCase().equals(BackgroundType.CUSTOM.toString())) {
            bitmap = AppStorage.getBitmapFromPath(path);

            if (bitmap == null) {
                ChatBackground chatBackground = backgrounds.get(BackgroundType.NONE.toString());
                AppTheme.getInstance().setChatBackground(chatBackground, context);
            }
            return bitmap;
        }

        String name = BACKGROUND + this.name.toLowerCase();
        int id = context.getResources().getIdentifier(name, "drawable",
                context.getPackageName());

        bitmap = BitmapFactory.decodeResource(context.getResources(), id);

        return bitmap;
    }
}
