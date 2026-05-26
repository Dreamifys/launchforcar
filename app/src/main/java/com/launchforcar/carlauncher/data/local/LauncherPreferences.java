package com.launchforcar.carlauncher.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class LauncherPreferences {

    private static final String PREF_NAME = "car_ai_launcher_prefs";
    private static final String KEY_AUTO_NIGHT = "auto_night";
    private static final String KEY_SCENE_AUTOMATION = "scene_automation";
    private static final String KEY_MUSIC_CARD_VISIBLE = "music_card_visible";
    private static final String KEY_CONTACT_CARD_VISIBLE = "contact_card_visible";
    private static final String KEY_STATS_CARD_VISIBLE = "stats_card_visible";
    private static final String KEY_DEFAULT_MODE = "default_mode";
    private static final String KEY_NAVIGATION_APP_PACKAGE = "navigation_app_package";
    private static final String KEY_LEFT_STAGE_APP_PACKAGE = "left_stage_app_package";
    private static final String KEY_RIGHT_STAGE_APP_PACKAGE = "right_stage_app_package";

    // 地图显示模式
    private static final String KEY_MAP_DISPLAY_MODE = "map_display_mode"; // "embedded", "external" 或 "floating"
    
    // 地图悬浮窗状态
    private static final String KEY_MAP_FLOATING_ENABLED = "map_floating_enabled";
    
    // 高德地图 Web API Key
    private static final String KEY_AMAP_WEB_KEY = "amap_web_key";

    private final SharedPreferences sharedPreferences;

    public LauncherPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isAutoNightEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTO_NIGHT, true);
    }

    public void setAutoNightEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_NIGHT, enabled).apply();
    }

    public boolean isSceneAutomationEnabled() {
        return sharedPreferences.getBoolean(KEY_SCENE_AUTOMATION, true);
    }

    public void setSceneAutomationEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_SCENE_AUTOMATION, enabled).apply();
    }

    public boolean isMusicCardVisible() {
        return sharedPreferences.getBoolean(KEY_MUSIC_CARD_VISIBLE, true);
    }

    public void setMusicCardVisible(boolean visible) {
        sharedPreferences.edit().putBoolean(KEY_MUSIC_CARD_VISIBLE, visible).apply();
    }

    public boolean isContactCardVisible() {
        return sharedPreferences.getBoolean(KEY_CONTACT_CARD_VISIBLE, true);
    }

    public void setContactCardVisible(boolean visible) {
        sharedPreferences.edit().putBoolean(KEY_CONTACT_CARD_VISIBLE, visible).apply();
    }

    public boolean isStatsCardVisible() {
        return sharedPreferences.getBoolean(KEY_STATS_CARD_VISIBLE, true);
    }

    public void setStatsCardVisible(boolean visible) {
        sharedPreferences.edit().putBoolean(KEY_STATS_CARD_VISIBLE, visible).apply();
    }

    public String getDefaultMode() {
        return sharedPreferences.getString(KEY_DEFAULT_MODE, "drive");
    }

    public void setDefaultMode(String mode) {
        sharedPreferences.edit().putString(KEY_DEFAULT_MODE, mode).apply();
    }

    public String getNavigationAppPackage() {
        return sharedPreferences.getString(KEY_NAVIGATION_APP_PACKAGE, "");
    }

    public void setNavigationAppPackage(String packageName) {
        sharedPreferences.edit().putString(KEY_NAVIGATION_APP_PACKAGE, packageName == null ? "" : packageName).apply();
    }

    public String getLeftStageAppPackage() {
        return sharedPreferences.getString(KEY_LEFT_STAGE_APP_PACKAGE, "");
    }

    public void setLeftStageAppPackage(String packageName) {
        sharedPreferences.edit().putString(KEY_LEFT_STAGE_APP_PACKAGE, packageName == null ? "" : packageName).apply();
    }

    public String getRightStageAppPackage() {
        return sharedPreferences.getString(KEY_RIGHT_STAGE_APP_PACKAGE, "");
    }

    public void setRightStageAppPackage(String packageName) {
        sharedPreferences.edit().putString(KEY_RIGHT_STAGE_APP_PACKAGE, packageName == null ? "" : packageName).apply();
    }
    
    /**
     * 获取高德地图Web服务Key
     */
    public String getAmapWebKey() {
        return sharedPreferences.getString(KEY_AMAP_WEB_KEY, "");
    }
    
    /**
     * 设置高德地图Web服务Key
     */
    public void setAmapWebKey(String key) {
        sharedPreferences.edit().putString(KEY_AMAP_WEB_KEY, key).apply();
    }
    
    /**
     * 获取地图显示模式
     */
    public String getMapDisplayMode() {
        return sharedPreferences.getString(KEY_MAP_DISPLAY_MODE, "embedded");
    }
    
    /**
     * 设置地图显示模式
     */
    public void setMapDisplayMode(String mode) {
        sharedPreferences.edit().putString(KEY_MAP_DISPLAY_MODE, mode).apply();
    }
    
    /**
     * 是否启用地图悬浮窗
     */
    public boolean isMapFloatingEnabled() {
        return sharedPreferences.getBoolean(KEY_MAP_FLOATING_ENABLED, true);
    }
    
    /**
     * 设置地图悬浮窗启用状态
     */
    public void setMapFloatingEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_MAP_FLOATING_ENABLED, enabled).apply();
    }
}
