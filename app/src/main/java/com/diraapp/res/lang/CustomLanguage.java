package com.diraapp.res.lang;

import android.app.Activity;
import android.content.Context;

import com.diraapp.exceptions.LanguageParsingException;
import com.diraapp.exceptions.NotCachedException;


public class CustomLanguage {

    private static StringsRepository stringsRepository = new StringsRepository();



    public static StringsRepository getStringsRepository() {
        return stringsRepository;
    }

    public static void loadExisting(Activity context) throws NotCachedException, LanguageParsingException {

        // load CURRENT LANG
    }

    public static void reset(Context context) {
        CachedValues.removeCustomLanguage(context);
    }
}
