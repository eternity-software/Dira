package com.diraapp.ui.appearance;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.storage.AppStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatBackground {

    private static final String BACKGROUND = "background_";
    private static final HashMap<BackgroundType, ChatBackground> backgrounds = new HashMap<>();
    private final String name;
    private final BackgroundType backgroundType;
    private String path;

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

    public static List<ChatBackground> getChatBackgrounds() {
        List<ChatBackground> list = new ArrayList<>();

        list.add(backgrounds.get(BackgroundType.NONE));
        list.add(backgrounds.get(BackgroundType.LOVE));
        list.add(backgrounds.get(BackgroundType.PETS));
        list.add(backgrounds.get(BackgroundType.EDUCATION));

        if (list.size() != backgrounds.size()) {
            for (ChatBackground background : backgrounds.values()) {
                if (!list.contains(background)) {
                    list.add(background);
                }
            }
        }

        return list;
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
                ChatBackground chatBackground = backgrounds.get(BackgroundType.LOVE);
                AppTheme.getInstance(context).setChatBackground(chatBackground, context);
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
//        Bitmap bitmap = this.getBitmap(view.getContext());
//
//        if (bitmap != null) {
//            view.setImageBitmap(bitmap);
//        } else {
//            view.setImageDrawable(this.getDrawable(view.getContext()));
//        }

        if (this.backgroundType.equals(BackgroundType.CUSTOM)) {
            try {
                view.setImageTintList(null);
                Bitmap bitmap = this.getBitmap(view.getContext());
                view.setImageBitmap(bitmap);
            } catch (Exception e) {
                view.setImageTintList(ColorStateList.valueOf(view.getContext().
                        getResources().getColor(R.color.gray)));
                ChatBackground background = backgrounds.get(BackgroundType.NONE);
                AppTheme.getInstance().setChatBackground(background, view.getContext());
            }
            return;
        }

        view.setImageTintList(ColorStateList.valueOf(view.getContext().
                getResources().getColor(R.color.gray)));
        view.setImageDrawable(this.getDrawable(view.getContext()));
    }

}
