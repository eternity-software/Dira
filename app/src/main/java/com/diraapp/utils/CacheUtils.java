package com.diraapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple cache implementation
 * Stores settings of Dira
 */
public class CacheUtils {


    public final static String ID = "id";
    public final static String NICKNAME = "nickname";
    public final static String PICTURE = "picture";
    public final static String AUTO_LOAD_SIZE = "autoload_size";

    public final static String COLOR_THEME_ID = "color_theme_id";

    public final static String BACKGROUND_ID = "background_id";
    public final static String BACKGROUND_PATH = "background_path";

    private final static String IDENTIFIER = "APP_SETTINGS";

    private final Context context;

    public CacheUtils(Context context) {
        this.context = context;
    }

    public SharedPreferences getSharedPreferences()
    {
        return context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE);
    }

    public SharedPreferences.Editor getEditor()
    {
        return getSharedPreferences().edit();
    }

    public boolean hasKey(String key) {
        return getSharedPreferences().contains(key);
    }
    public void clean() {
        SharedPreferences.Editor editor = getEditor();
        editor.clear();
        editor.apply();
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = getEditor();
        editor.remove(key);
        editor.apply();
    }

    public void setString(String key, String text) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(key, text);
        editor.apply();
    }

    public void setLong(String key, long value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putLong(key, value);
        editor.apply();
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = getEditor();
        editor.putInt(key, value);
        editor.apply();
    }

    public void setBoolean(String key, Boolean bool) {
        SharedPreferences.Editor editor = getEditor();
        editor.putBoolean(key, bool);
        editor.apply();
    }


    public int getInt(String key) {
        return getSharedPreferences().getInt(key, -1);
    }

    public long getLong(String key) {
        return getSharedPreferences().getLong(key, -1);
    }

    public boolean getBoolean(String key) {
        return getSharedPreferences().getBoolean(key, false);
    }

    public String getString(String key) {
        return getSharedPreferences().getString(key, null);
    }



}
