package com.diraapp.appearance;

import android.content.Context;
import android.graphics.Bitmap;

import com.diraapp.utils.CacheUtils;

public class AppTheme {

    private static AppTheme instance;

    private ChatBackground chatBackground;

    private ColorTheme colorTheme;

    public AppTheme(Context context) {
        ColorTheme.initColorThemes(context);
        // Получить из памяти(если есть) номер темы
        String colorThemeName;
        try {
            CacheUtils cacheUtils = new CacheUtils(context);
            colorThemeName = cacheUtils.getString(CacheUtils.COLOR_THEME_ID);
        } catch (Exception e) {
            colorThemeName = ColorThemeType.DIRA.toString();
        }

        colorTheme = ColorTheme.getColorThemes().get(colorThemeName);

        ChatBackground.initBackgrounds(context);
        // картинку
        String backgroundName;
        try {
            CacheUtils cacheUtils = new CacheUtils(context);
            backgroundName = cacheUtils.getString(CacheUtils.BACKGROUND_ID);
            chatBackground = ChatBackground.getBackgrounds().get(backgroundName);

            if (chatBackground.getName().equalsIgnoreCase(BackgroundType.CUSTOM.toString())) {
                chatBackground.setPath(cacheUtils.getString(CacheUtils.BACKGROUND_PATH));
            }
        } catch (Exception e) {
            chatBackground = null;
        }

        instance = this;
    }

    public ColorTheme getColorTheme() {
        return colorTheme;
    }

    public ChatBackground getChatBackground() {
        return chatBackground;
    }

    public void setColorTheme(String name, Context context) {
        ColorTheme colorTheme = ColorTheme.getColorThemes().get(name.toUpperCase());

        if (colorTheme != null) {
            this.colorTheme = colorTheme;

            CacheUtils cacheUtils = new CacheUtils(context);
            cacheUtils.setString(CacheUtils.COLOR_THEME_ID, name);
        } else {
            this.colorTheme = ColorTheme.getColorThemes().get(ColorThemeType.DIRA.toString());

            CacheUtils cacheUtils = new CacheUtils(context);
            cacheUtils.setString(CacheUtils.COLOR_THEME_ID, this.colorTheme.getName());
        }
    }

    public void setColorTheme(ColorTheme theme, Context context) {
        colorTheme = theme;

        CacheUtils cacheUtils = new CacheUtils(context);
        cacheUtils.setString(CacheUtils.COLOR_THEME_ID, theme.getName());
    }

    public void setChatBackground(ChatBackground chatBackground, Context context) {
        CacheUtils cacheUtils = new CacheUtils(context);

        cacheUtils.setString(CacheUtils.BACKGROUND_ID, chatBackground.getName().toUpperCase());

        if (chatBackground.getName().toUpperCase().equals(BackgroundType.CUSTOM.toString())) {
            cacheUtils.setString(CacheUtils.BACKGROUND_PATH, chatBackground.getPath());
        } else {
            cacheUtils.remove(CacheUtils.BACKGROUND_PATH);
        }
        this.chatBackground = chatBackground;
    }

    public void clearBackground(Context context) {
        CacheUtils cacheUtils = new CacheUtils(context);
        if (this.chatBackground.getName().toUpperCase().equals(BackgroundType.CUSTOM.toString())) {
            cacheUtils.remove(CacheUtils.BACKGROUND_PATH);
        }

        cacheUtils.remove(CacheUtils.BACKGROUND_ID);
        this.chatBackground = null;
    }

    public static AppTheme getInstance() {
        return instance;
    }

}
