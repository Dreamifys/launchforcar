package com.launchforcar.carlauncher.utils;

import java.util.Calendar;

/**
 * 主题工具类
 * 根据时间自动判断应该使用深色还是浅色主题
 */
public class ThemeUtils {
    
    private static final int DARK_THEME_START_HOUR = 18; // 18:00 开始深色
    private static final int DARK_THEME_END_HOUR = 7;    // 07:00 结束深色
    
    /**
     * 根据当前时间判断是否应该使用深色主题
     * @return true 如果应该使用深色主题
     */
    public static boolean shouldUseDarkTheme() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        // 晚上 18:00 - 早上 7:00 使用深色主题
        return hour >= DARK_THEME_START_HOUR || hour < DARK_THEME_END_HOUR;
    }
    
    /**
     * 获取当前时间的主题描述
     * @return "深色" 或 "浅色"
     */
    public static String getCurrentThemeDescription() {
        return shouldUseDarkTheme() ? "深色" : "浅色";
    }
}
