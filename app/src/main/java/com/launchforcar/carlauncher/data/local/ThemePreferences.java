package com.launchforcar.carlauncher.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemePreferences {
    
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode"; // "light", "dark", "auto"
    private static final String KEY_AUTO_THEME_ENABLED = "auto_theme_enabled";
    
    private final SharedPreferences sharedPreferences;
    
    public ThemePreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public String getThemeMode() {
        return sharedPreferences.getString(KEY_THEME_MODE, "auto");
    }
    
    public void setThemeMode(String mode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode).apply();
    }
    
    public boolean isAutoThemeEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTO_THEME_ENABLED, true);
    }
    
    public void setAutoThemeEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_THEME_ENABLED, enabled).apply();
    }
}
