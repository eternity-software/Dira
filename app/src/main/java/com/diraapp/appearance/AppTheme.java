package com.diraapp.appearance;

import android.content.Context;

import com.diraapp.utils.CacheUtils;

public class AppTheme {

    private static AppTheme instance;

    private ChatBackground chatBackground;

    private ColorTheme colorTheme;

    public AppTheme(Context context) {
        ColorTheme.initColorThemes(context);
        // Получить из памяти(если есть) номер темы
        ColorThemeType colorThemeType;

        CacheUtils cacheUtils = new CacheUtils(context);
        if (!cacheUtils.hasKey(CacheUtils.COLOR_THEME_ID)) {
            cacheUtils.setString(CacheUtils.COLOR_THEME_ID, ColorThemeType.DIRA.toString());
        }

        colorThemeType = ColorThemeType.valueOf(cacheUtils.getString(CacheUtils.COLOR_THEME_ID));

        colorTheme = ColorTheme.getColorThemes().get(colorThemeType);

        ChatBackground.initBackgrounds(context);
        // картинку
        if (!cacheUtils.hasKey(CacheUtils.BACKGROUND_ID)) {
            cacheUtils.setString(CacheUtils.BACKGROUND_ID, BackgroundType.LOVE.toString());
        }
        BackgroundType backgroundType;
        backgroundType = BackgroundType.valueOf(cacheUtils.getString(CacheUtils.BACKGROUND_ID));

        if (backgroundType.equals(BackgroundType.CUSTOM)) {
            chatBackground = new ChatBackground(backgroundType.toString(),
                    cacheUtils.getString(CacheUtils.BACKGROUND_PATH), backgroundType);
        } else {
            chatBackground = ChatBackground.getBackgrounds().get(backgroundType);

            if (chatBackground == null) {
                chatBackground = ChatBackground.getBackgrounds().get(BackgroundType.LOVE);
            }
        }

        instance = this;
    }

    public static AppTheme getInstance() {
        return instance;
    }

    public ColorTheme getColorTheme() {
        return colorTheme;
    }

    public ChatBackground getChatBackground() {
        return chatBackground;
    }

    public void setColorTheme(ColorThemeType type, Context context) {
        ColorTheme colorTheme = ColorTheme.getColorThemes().get(type);

        if (colorTheme != null) {
            this.colorTheme = colorTheme;

            CacheUtils cacheUtils = new CacheUtils(context);
            cacheUtils.setString(CacheUtils.COLOR_THEME_ID, colorTheme.getType().toString());
        } else {
            this.colorTheme = ColorTheme.getColorThemes().get(ColorThemeType.DIRA);

            CacheUtils cacheUtils = new CacheUtils(context);
            cacheUtils.setString(CacheUtils.COLOR_THEME_ID, this.colorTheme.getType().toString());
        }
    }

    public void setColorTheme(ColorTheme theme, Context context) {
        colorTheme = theme;

        CacheUtils cacheUtils = new CacheUtils(context);
        cacheUtils.setString(CacheUtils.COLOR_THEME_ID, theme.getType().toString());
    }

    public void setChatBackground(ChatBackground chatBackground, Context context) {
        CacheUtils cacheUtils = new CacheUtils(context);

        cacheUtils.setString(CacheUtils.BACKGROUND_ID, chatBackground.getBackgroundType().toString());

        if (chatBackground.getBackgroundType().equals(BackgroundType.CUSTOM)) {
            cacheUtils.setString(CacheUtils.BACKGROUND_PATH, chatBackground.getPath());
        } else {
            cacheUtils.remove(CacheUtils.BACKGROUND_PATH);
        }
        this.chatBackground = chatBackground;
    }

    public void clearBackground(Context context) {
        CacheUtils cacheUtils = new CacheUtils(context);
        if (this.chatBackground.getBackgroundType().equals(BackgroundType.CUSTOM)) {
            cacheUtils.remove(CacheUtils.BACKGROUND_PATH);
        }

        cacheUtils.remove(CacheUtils.BACKGROUND_ID);
        this.chatBackground = null;
    }

}
