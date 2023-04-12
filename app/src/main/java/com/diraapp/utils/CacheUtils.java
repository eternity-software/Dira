package com.diraapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class CacheUtils {


    public final static String ID = "id";
    public final static String NICKNAME = "nickname";
    public final static String PICTURE = "picture";
    public final static String AUTO_LOAD_SIZE = "autoload_size";

    private final static String IDENTIFIER = "APP_SETTINGS";
    private static CacheUtils instance;

    private final Context context;

    public CacheUtils(Context context) {
        this.context = context;
    }


    // Запись строки по ключу
    public void setString(String key, String text) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.putString(key, text);
        editor.apply();
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.remove(key);

        editor.apply();
    }

    public void setLong(String key, long text) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.putLong(key, text);
        editor.apply();
    }


    public boolean hasKey(String key) {
        SharedPreferences pref = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE);
        return pref.contains(key);
    }

    // Получение long по ключу
    public long getLong(String name) {
        SharedPreferences pref = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE);
        return pref.getLong(name, 0);
    }

    // Получение boolean по ключу
    public boolean getBoolean(String name) {
        SharedPreferences pref = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE);
        return pref.getBoolean(name, false);
    }

    // Получение строки по ключу
    public String getString(String name) {
        SharedPreferences pref = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE);
        return pref.getString(name, null);
    }

    // Запись boolean по ключу
    public void setBoolean(String key, Boolean bool) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, bool);
        editor.apply();
    }

    // Стереть все сохранённые данные
    public void clean() {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }


}
