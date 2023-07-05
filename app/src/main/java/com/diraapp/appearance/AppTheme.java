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
        ColorThemeType colorThemeType;

        CacheUtils cacheUtils = new CacheUtils(context);
        colorThemeType = ColorThemeType.valueOf(cacheUtils.getString(CacheUtils.COLOR_THEME_ID));

        colorTheme = ColorTheme.getColorThemes().get(colorThemeType);

        if (colorTheme == null) {
            colorTheme = ColorTheme.getColorThemes().get(ColorThemeType.DIRA);
        }

        ChatBackground.initBackgrounds(context);
        // картинку
        BackgroundType backgroundName;
        backgroundName = BackgroundType.valueOf(cacheUtils.getString(CacheUtils.BACKGROUND_ID));
        chatBackground = ChatBackground.getBackgrounds().get(backgroundName);

        if (chatBackground != null) {
            if (chatBackground.getBackgroundType().equals(BackgroundType.CUSTOM)) {
                chatBackground.setPath(cacheUtils.getString(CacheUtils.BACKGROUND_PATH));
            }
        } else {
            chatBackground = ChatBackground.getBackgrounds().get(BackgroundType.NONE);
        }

        instance = this;
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

    public static AppTheme getInstance() {
        return instance;
    }

}
