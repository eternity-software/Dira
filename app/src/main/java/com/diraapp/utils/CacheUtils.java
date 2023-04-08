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


    // Инициализация контекста
    public static CacheUtils getInstance() {
        if (instance == null) {
            instance = new CacheUtils();
        }
        return instance;
    }

    // Запись строки по ключу
    public void setString(String key, String text, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.putString(key, text);
        editor.apply();
    }

    public void remove(String key, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.remove(key);

        editor.apply();
    }

    public void setLong(String key, long text, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.putLong(key, text);
        editor.apply();
    }


    public boolean hasKey(String key, Context context) {
        SharedPreferences pref = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE);
        return pref.contains(key);
    }

    // Получение long по ключу
    public long getLong(String name, Context context) {
        SharedPreferences pref = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE);
        return pref.getLong(name, 0);
    }

    // Получение boolean по ключу
    public boolean getBoolean(String name, Context context) {
        SharedPreferences pref = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE);
        return pref.getBoolean(name, false);
    }

    // Получение строки по ключу
    public String getString(String name, Context context) {
        SharedPreferences pref = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE);
        return pref.getString(name, null);
    }

    // Запись boolean по ключу
    public void setBoolean(String key, Boolean bool, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, bool);
        editor.apply();
    }

    // Стереть все сохранённые данные
    public void clean(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IDENTIFIER, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
    }


}
