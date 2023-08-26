package com.diraapp.res;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatDelegate;

import com.diraapp.R;
import com.diraapp.exceptions.LanguageParsingException;
import com.diraapp.exceptions.NoSuchValueException;
import com.diraapp.exceptions.NotCachedException;
import com.diraapp.res.lang.StringsRepository;
import com.diraapp.utils.CacheUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class Theme {

    private final static String DARK_THEME = "APP_THEME_NIGHT";
    private static StringsRepository stringsRepository = new StringsRepository();
    private static List<ThemeChangeHandler> themeChangeHandlers = new ArrayList<>();

    public static List<ThemeChangeHandler> getThemeChangeHandlers() {
        return themeChangeHandlers;
    }

    public static boolean isDayTheme(Context context) {
        // TODO: day theme
        return true;
    }

    public static void addThemeChangeHandler(ThemeChangeHandler themeChangeHandler) {
        themeChangeHandlers.add(themeChangeHandler);
    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            return -1;
        }
    }

    public static int getColor(Context context, int resId) {
        try {
            return getColor(context.getResources().getResourceName(resId).split(":")[1].split("/")[1]);
        } catch (Exception e) {
            try {
                return context.getResources().getColor(resId);
            }
            catch (Exception exception)
            {

                return Color.RED;
            }
        }
    }

    public static int getColor(String name) throws NoSuchValueException {

        if(name.startsWith("@")) name = name.split("/")[1];
        if(name == null) return Color.MAGENTA;
        String colorHex = stringsRepository.getValue(name);


        if (colorHex.startsWith("#")) {
            try
            {
                return Color.parseColor(colorHex);
            }
            catch (Exception e)
            {
                System.out.println("Unknown color: " + colorHex);
                e.printStackTrace();
                return Color.RED;
            }

        } else {
            return getColor(colorHex);
        }


    }



    public static void applyThemeToActivity(Activity activity)
    {
        Window window = activity.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            //window.setStatusBarColor(getColor(activity, R.color.colorBackground));
        }
    }

    public static void loadFromUrl(final String urlToXml, final Activity context, final boolean silentMode) {
   /*     Thread loading = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String xmlString = GET.executeForTimeout(urlToXml, 5000);

                    stringsRepository.applyXml(xmlString);
                    CachedUrls.setThemeUrl(context, urlToXml);
                    if (isDayTheme(context)) {
                        CachedValues.saveDayTheme(context, xmlString);
                    } else {
                        CachedValues.saveNightTheme(context, xmlString);
                    }
                } catch (ResponseException | LanguageParsingException e) {
                    if (!silentMode) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CuteToast.show(CustomLanguage.getStringsRepository().getOrDefault(R.string.err_theme_url, context), R.drawable.icon_error, context);
                            }
                        });

                    }
                    e.printStackTrace();
                }
            }
        });
        loading.start();*/
    }

    public static void clear() {
        stringsRepository.clear();
    }

    public static void applyXml(String xmlString) throws LanguageParsingException {
        stringsRepository.applyXml(xmlString);
    }

    public static void applyBackground(View view) {
        //view.setBackgroundColor(getColor(view.getContext(), R.color.colorBackground));
        notifyUpdate();
    }

    public static void notifyUpdate() {
        for (ThemeChangeHandler themeChangeHandler : themeChangeHandlers) {
            themeChangeHandler.onThemeChange();
        }
    }

    public interface ThemeChangeHandler {
        void onThemeChange();
    }
}
