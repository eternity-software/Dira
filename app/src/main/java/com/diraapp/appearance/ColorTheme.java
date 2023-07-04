package com.diraapp.appearance;

import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.diraapp.R;

import java.util.ArrayList;
import java.util.HashMap;

public class ColorTheme {

    private int senderMessageColor;

    private int messageColor;

    private int accentColor;

    private String name;

    private static HashMap<String, ColorTheme> colorThemes = new HashMap<>();

    public static void initColorThemes(Context context) {
        ColorTheme main = new ColorTheme();
        main.senderMessageColor = ContextCompat.getColor(context, R.color.accent);
        main.accentColor = ContextCompat.getColor(context, R.color.accent);
        main.messageColor = ContextCompat.getColor(context, R.color.gray);
        main.name = context.getResources().getString(R.string.theme_main);

        ColorTheme kitty = new ColorTheme();
        kitty.senderMessageColor = ContextCompat.getColor(context, R.color.kitty_accent);
        kitty.accentColor = ContextCompat.getColor(context, R.color.kitty_accent);
        kitty.messageColor = ContextCompat.getColor(context, R.color.kitty_message);
        kitty.name = context.getResources().getString(R.string.theme_kitty);

        ColorTheme blue = new ColorTheme();
        blue.senderMessageColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.accentColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.messageColor = ContextCompat.getColor(context, R.color.blue_message);
        blue.name = context.getResources().getString(R.string.theme_blue);

        ColorTheme rock = new ColorTheme();
        rock.senderMessageColor = ContextCompat.getColor(context, R.color.rock_accent);
        rock.accentColor = ContextCompat.getColor(context, R.color.rock_accent);
        rock.messageColor = ContextCompat.getColor(context, R.color.rock_message);
        rock.name = context.getResources().getString(R.string.theme_rock);

        ColorTheme green = new ColorTheme();
        green.senderMessageColor = ContextCompat.getColor(context, R.color.green_accent);
        green.accentColor = ContextCompat.getColor(context, R.color.green_accent);
        green.messageColor = ContextCompat.getColor(context, R.color.green_message);
        green.name = context.getResources().getString(R.string.theme_green);

        colorThemes.put(ColorThemeType.DIRA.toString(), main);
        colorThemes.put(ColorThemeType.KITTY.toString(), kitty);
        colorThemes.put(ColorThemeType.BLUE.toString(), blue);
        colorThemes.put(ColorThemeType.ROCK.toString(), rock);
        colorThemes.put(ColorThemeType.GREEN.toString(), green);
    }

    public static HashMap<String, ColorTheme> getColorThemes() {
        return colorThemes;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public String getName() {
        return name;
    }
}
