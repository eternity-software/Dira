package com.diraapp.appearance;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.diraapp.R;

import java.util.HashMap;

public class ColorTheme {

    private int selfMessageColor;

    private int messageColor;

    private ColorThemeType type;

    private int accentColor;

    private int selfTextColor;

    private int textColor;

    private String name;

    private static HashMap<ColorThemeType, ColorTheme> colorThemes = new HashMap<>();

    public static void initColorThemes(Context context) {
        ColorTheme main = new ColorTheme();
        main.selfMessageColor = ContextCompat.getColor(context, R.color.accent);
        main.accentColor = ContextCompat.getColor(context, R.color.accent);
        main.messageColor = ContextCompat.getColor(context, R.color.gray);
        main.selfTextColor = ContextCompat.getColor(context, R.color.dark);
        main.textColor = ContextCompat.getColor(context, R.color.white);
        main.name = context.getResources().getString(R.string.theme_main);
        main.type = ColorThemeType.DIRA;

        ColorTheme kitty = new ColorTheme();
        kitty.selfMessageColor = ContextCompat.getColor(context, R.color.kitty_accent);
        kitty.accentColor = ContextCompat.getColor(context, R.color.kitty_accent);
        kitty.messageColor = ContextCompat.getColor(context, R.color.kitty_message);
        kitty.selfTextColor = ContextCompat.getColor(context, R.color.white);
        kitty.textColor = ContextCompat.getColor(context, R.color.white);
        kitty.name = context.getResources().getString(R.string.theme_kitty);
        kitty.type = ColorThemeType.KITTY;

        ColorTheme blue = new ColorTheme();
        blue.selfMessageColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.accentColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.messageColor = ContextCompat.getColor(context, R.color.blue_message);
        blue.selfTextColor = ContextCompat.getColor(context, R.color.white);
        blue.textColor = ContextCompat.getColor(context, R.color.white);
        blue.name = context.getResources().getString(R.string.theme_blue);
        blue.type = ColorThemeType.BLUE;

        ColorTheme rock = new ColorTheme();
        rock.selfMessageColor = ContextCompat.getColor(context, R.color.rock_accent);
        rock.accentColor = ContextCompat.getColor(context, R.color.rock_accent);
        rock.messageColor = ContextCompat.getColor(context, R.color.rock_message);
        rock.selfTextColor = ContextCompat.getColor(context, R.color.white);
        rock.textColor = ContextCompat.getColor(context, R.color.white);
        rock.name = context.getResources().getString(R.string.theme_rock);
        rock.type = ColorThemeType.ROCK;

        ColorTheme green = new ColorTheme();
        green.selfMessageColor = ContextCompat.getColor(context, R.color.green_accent);
        green.accentColor = ContextCompat.getColor(context, R.color.green_accent);
        green.messageColor = ContextCompat.getColor(context, R.color.green_message);
        green.selfTextColor = ContextCompat.getColor(context, R.color.white);
        green.textColor = ContextCompat.getColor(context, R.color.white);
        green.name = context.getResources().getString(R.string.theme_green);
        green.type = ColorThemeType.GREEN;

        colorThemes.put(ColorThemeType.DIRA, main);
        colorThemes.put(ColorThemeType.KITTY, kitty);
        colorThemes.put(ColorThemeType.BLUE, blue);
        colorThemes.put(ColorThemeType.ROCK, rock);
        colorThemes.put(ColorThemeType.GREEN, green);
    }

    public static HashMap<ColorThemeType, ColorTheme> getColorThemes() {
        return colorThemes;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public String getName() {
        return name;
    }

    public ColorThemeType getType() {
        return type;
    }

    public int getSelfMessageColor() {
        return selfMessageColor;
    }

    public int getMessageColor() {
        return messageColor;
    }

    public int getSelfTextColor() {
        return selfTextColor;
    }

    public int getTextColor(){
        return textColor;
    }
}
