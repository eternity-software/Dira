package com.diraapp.ui.appearance;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.diraapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ColorTheme {

    private static final HashMap<ColorThemeType, ColorTheme> colorThemes = new HashMap<>();
    private int selfMessageColor;
    private int messageColor;

    private int roomUpdateMessageColor;
    private ColorThemeType type;
    private int accentColor;
    private int selfTextColor;
    private int textColor;
    private int sendButtonColor;
    private int previewColor;

    private int roomLickColor;

    private int selfLinkColor;

    private int downloadButtonTextColor;
    private int downloadButtonColor;

    private int selfDownloadButtonTextColor;
    private int selfDownButtonColor;
    private int unreadMessageBackground;
    private String name;

    public static void initColorThemes(Context context) {
        ColorTheme main = new ColorTheme();
        main.previewColor = ContextCompat.getColor(context, R.color.accent);
        main.selfMessageColor = ContextCompat.getColor(context, R.color.gray);
        main.accentColor = ContextCompat.getColor(context, R.color.accent);
        main.messageColor = ContextCompat.getColor(context, R.color.accent);
        main.selfTextColor = ContextCompat.getColor(context, R.color.light_gray);
        main.textColor = ContextCompat.getColor(context, R.color.dark);
        main.sendButtonColor = ContextCompat.getColor(context, R.color.dark);
        main.roomLickColor = ContextCompat.getColor(context, R.color.gray);
        main.selfLinkColor = ContextCompat.getColor(context, R.color.accent_dark);
        main.downloadButtonTextColor = ContextCompat.getColor(context, R.color.white);
        main.downloadButtonColor = ContextCompat.getColor(context, R.color.dark);
        main.selfDownloadButtonTextColor = ContextCompat.getColor(context, R.color.accent);
        main.selfDownButtonColor = ContextCompat.getColor(context, R.color.dark);
        main.roomUpdateMessageColor = ContextCompat.getColor(context, R.color.gray);
        main.unreadMessageBackground = ContextCompat.getColor(context, R.color.unread_message_background);
        main.name = context.getResources().getString(R.string.theme_main);
        main.type = ColorThemeType.DIRA;

        ColorTheme kitty = new ColorTheme();
        kitty.previewColor = ContextCompat.getColor(context, R.color.kitty_accent);
        kitty.selfMessageColor = ContextCompat.getColor(context, R.color.kitty_accent);
        kitty.accentColor = ContextCompat.getColor(context, R.color.kitty_accent);
        kitty.messageColor = ContextCompat.getColor(context, R.color.kitty_message);
        kitty.selfTextColor = ContextCompat.getColor(context, R.color.white);
        kitty.textColor = ContextCompat.getColor(context, R.color.white);
        kitty.sendButtonColor = ContextCompat.getColor(context, R.color.white);
        kitty.roomLickColor = ContextCompat.getColor(context, R.color.light_gray);
        kitty.selfLinkColor = ContextCompat.getColor(context, R.color.light_gray);
        kitty.downloadButtonTextColor = ContextCompat.getColor(context, R.color.white);
        kitty.downloadButtonColor = ContextCompat.getColor(context, R.color.dark);
        kitty.selfDownloadButtonTextColor = ContextCompat.getColor(context, R.color.white);
        kitty.selfDownButtonColor = ContextCompat.getColor(context, R.color.dark);
        kitty.roomUpdateMessageColor = ContextCompat.getColor(context, R.color.light_gray);
        kitty.unreadMessageBackground = ContextCompat.getColor(context, R.color.unread_message_background_kitty);
        kitty.name = context.getResources().getString(R.string.theme_kitty);
        kitty.type = ColorThemeType.KITTY;

        ColorTheme blue = new ColorTheme();
        blue.previewColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.selfMessageColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.accentColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.messageColor = ContextCompat.getColor(context, R.color.blue_message);
        blue.selfTextColor = ContextCompat.getColor(context, R.color.white);
        blue.textColor = ContextCompat.getColor(context, R.color.white);
        blue.sendButtonColor = ContextCompat.getColor(context, R.color.white);
        blue.roomLickColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.selfLinkColor = ContextCompat.getColor(context, R.color.light_gray);
        blue.downloadButtonTextColor = ContextCompat.getColor(context, R.color.white);
        blue.downloadButtonColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.selfDownloadButtonTextColor = ContextCompat.getColor(context, R.color.white);
        blue.selfDownButtonColor = ContextCompat.getColor(context, R.color.blue_accent);
        blue.roomUpdateMessageColor = ContextCompat.getColor(context, R.color.light_gray);
        blue.unreadMessageBackground = ContextCompat.getColor(context, R.color.unread_message_background_blue);
        blue.name = context.getResources().getString(R.string.theme_blue);
        blue.type = ColorThemeType.BLUE;

        ColorTheme rock = new ColorTheme();
        rock.previewColor = ContextCompat.getColor(context, R.color.rock_accent);
        rock.selfMessageColor = ContextCompat.getColor(context, R.color.rock_accent);
        rock.accentColor = ContextCompat.getColor(context, R.color.rock_accent);
        rock.messageColor = ContextCompat.getColor(context, R.color.rock_message);
        rock.selfTextColor = ContextCompat.getColor(context, R.color.white);
        rock.textColor = ContextCompat.getColor(context, R.color.white);
        rock.sendButtonColor = ContextCompat.getColor(context, R.color.white);
        rock.roomLickColor = ContextCompat.getColor(context, R.color.rock_accent);
        rock.selfLinkColor = ContextCompat.getColor(context, R.color.light_gray);
        rock.downloadButtonTextColor = ContextCompat.getColor(context, R.color.white);
        rock.downloadButtonColor = ContextCompat.getColor(context, R.color.dark);
        rock.selfDownloadButtonTextColor = ContextCompat.getColor(context, R.color.white);
        rock.selfDownButtonColor = ContextCompat.getColor(context, R.color.dark);
        rock.roomUpdateMessageColor = ContextCompat.getColor(context, R.color.light_gray);
        rock.unreadMessageBackground = ContextCompat.getColor(context, R.color.unread_message_background_rock);
        rock.name = context.getResources().getString(R.string.theme_rock);
        rock.type = ColorThemeType.ROCK;

        ColorTheme green = new ColorTheme();
        green.previewColor = ContextCompat.getColor(context, R.color.green_accent);
        green.selfMessageColor = ContextCompat.getColor(context, R.color.green_accent);
        green.accentColor = ContextCompat.getColor(context, R.color.accent);
        green.messageColor = ContextCompat.getColor(context, R.color.green_message);
        green.selfTextColor = ContextCompat.getColor(context, R.color.white);
        green.textColor = ContextCompat.getColor(context, R.color.white);
        green.sendButtonColor = ContextCompat.getColor(context, R.color.dark);
        green.roomLickColor = ContextCompat.getColor(context, R.color.light_gray);
        green.selfLinkColor = ContextCompat.getColor(context, R.color.light_gray);
        green.downloadButtonTextColor = ContextCompat.getColor(context, R.color.white);
        green.downloadButtonColor = ContextCompat.getColor(context, R.color.dark);
        green.selfDownloadButtonTextColor = ContextCompat.getColor(context, R.color.white);
        green.selfDownButtonColor = ContextCompat.getColor(context, R.color.dark);
        green.roomUpdateMessageColor = ContextCompat.getColor(context, R.color.light_gray);
        green.unreadMessageBackground = ContextCompat.getColor(context, R.color.unread_message_background_green);
        green.name = context.getResources().getString(R.string.theme_green);
        green.type = ColorThemeType.GREEN;

        colorThemes.put(ColorThemeType.DIRA, main);
        colorThemes.put(ColorThemeType.KITTY, kitty);
        colorThemes.put(ColorThemeType.BLUE, blue);
        colorThemes.put(ColorThemeType.ROCK, rock);
        colorThemes.put(ColorThemeType.GREEN, green);
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

    public int getTextColor() {
        return textColor;
    }

    public int getSendButtonColor() {
        return sendButtonColor;
    }

    public int getPreviewColor() {
        return previewColor;
    }

    public int getRoomLickColor() {
        return roomLickColor;
    }

    public int getSelfLinkColor() {
        return selfLinkColor;
    }

    public int getDownloadButtonTextColor() {
        return downloadButtonTextColor;
    }

    public int getDownloadButtonColor() {
        return downloadButtonColor;
    }

    public int getRoomUpdateMessageColor() {
        return roomUpdateMessageColor;
    }

    public int getSelfDownloadButtonTextColor() {
        return selfDownloadButtonTextColor;
    }

    public int getSelfDownButtonColor() {
        return selfDownButtonColor;
    }

    public int getUnreadMessageBackground() {
        return unreadMessageBackground;
    }
}
