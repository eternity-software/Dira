package com.diraapp.ui.appearance;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.diraapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ColorTheme {

    private static final HashMap<ColorThemeType, ColorTheme> colorThemes = new HashMap<>();
    private int roomUpdateMessageColor;
    private ColorThemeType type;
    private int previewColor;

    private String name;

    public static void initColorThemes(Context context) {
        ColorTheme main = new ColorTheme();
        main.previewColor = ContextCompat.getColor(context, R.color.accent);
        main.name = context.getResources().getString(R.string.theme_main);
        main.type = ColorThemeType.DIRA;

        ColorTheme kitty = new ColorTheme();
        kitty.previewColor = ContextCompat.getColor(context, R.color.kitty_accent);
        kitty.name = context.getResources().getString(R.string.theme_kitty);
        kitty.type = ColorThemeType.KITTY;

        ColorTheme blue = new ColorTheme();
        blue.previewColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.name = context.getResources().getString(R.string.theme_blue);
        blue.type = ColorThemeType.BLUE;

        ColorTheme rock = new ColorTheme();
        rock.previewColor = ContextCompat.getColor(context, R.color.rock_accent);
        rock.name = context.getResources().getString(R.string.theme_rock);
        rock.type = ColorThemeType.ROCK;

        ColorTheme green = new ColorTheme();
        green.previewColor = ContextCompat.getColor(context, R.color.green_accent);
        green.name = context.getResources().getString(R.string.theme_green);
        green.type = ColorThemeType.GREEN;

        ColorTheme pink = new ColorTheme();
        pink.previewColor = ContextCompat.getColor(context, R.color.pink_accent);
        pink.name = "Pink";
        pink.type = ColorThemeType.PINK;

        colorThemes.put(ColorThemeType.DIRA, main);
        colorThemes.put(ColorThemeType.KITTY, kitty);
        colorThemes.put(ColorThemeType.BLUE, blue);
        colorThemes.put(ColorThemeType.ROCK, rock);
        colorThemes.put(ColorThemeType.GREEN, green);
        colorThemes.put(ColorThemeType.PINK, pink);
    }

    public static List<ColorTheme> getColorThemeList() {
        List<ColorTheme> list = new ArrayList<>();

        list.add(colorThemes.get(ColorThemeType.DIRA));
        list.add(colorThemes.get(ColorThemeType.KITTY));
        list.add(colorThemes.get(ColorThemeType.BLUE));
        list.add(colorThemes.get(ColorThemeType.ROCK));
        list.add(colorThemes.get(ColorThemeType.GREEN));

        if (list.size() != colorThemes.size()) {
            for (ColorTheme theme : colorThemes.values()) {
                if (!list.contains(theme)) {
                    list.add(theme);
                }
            }
        }

        return list;
    }

    public static HashMap<ColorThemeType, ColorTheme> getColorThemes() {
        return colorThemes;
    }

    public String getName() {
        return name;
    }

    public ColorThemeType getType() {
        return type;
    }

    public int getPreviewColor() {
        return previewColor;
    }

}
