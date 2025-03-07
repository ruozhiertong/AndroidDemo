package com.example.safetywalk2.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.safetywalk2.R;

public class ThemeManager {
    private static final String PREF_NAME = Config.SHAREFILE_NAME;
    private static final String KEY_THEME = Config.SELECTED_THEME;

    public static final int THEME_BLUE = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_GREEN = 2;
    public static final int THEME_PURPLE = 3;

    public static void setTheme(Context context, int themeId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_THEME, themeId);
        editor.apply();
    }

    public static int getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, THEME_BLUE); // Default to Blue theme
    }

    public static void applyTheme(Context context) {
        int themeId = getTheme(context);

        switch (themeId) {
            case THEME_BLUE:
                context.setTheme(R.style.Theme_MotionGuard_Blue);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                context.setTheme(R.style.Theme_MotionGuard_Dark);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_GREEN:
                context.setTheme(R.style.Theme_MotionGuard_Green);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_PURPLE:
                context.setTheme(R.style.Theme_MotionGuard_Purple);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }
}