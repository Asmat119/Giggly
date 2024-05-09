package com.wit.giggly;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemeSaver {

    private static final String PREF_NAME = "ThemePref";
    private static final String KEY_THEME = "selectedTheme";

    public static void saveThemePreference(Context context, boolean isDarkMode) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_THEME, isDarkMode);
        editor.apply();
    }

    public static boolean getThemePreference(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_THEME, false); // Default to false (light mode)
    }
}
