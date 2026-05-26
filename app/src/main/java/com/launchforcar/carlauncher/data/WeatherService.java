package com.launchforcar.carlauncher.data;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {
    
    private static final String TAG = "WeatherService";
    // 使用和风天气 API（需要注册获取 key）
    // 这里使用一个免费的示例 API
    private static final String WEATHER_API_URL = "https://wttr.in/%s?format=j1";
    
    public interface WeatherCallback {
        void onWeatherLoaded(WeatherData weather);
        void onError(String error);
    }
    
    public static class WeatherData {
        public String temperature;
        public String description;
        public String icon;
        public String humidity;
        public String windSpeed;
        
        public WeatherData(String temp, String desc, String icon) {
            this.temperature = temp;
            this.description = desc;
            this.icon = icon;
        }
    }
    
    /**
     * 异步获取天气数据
     */
    public static void getWeather(String city, WeatherCallback callback) {
        new Thread(() -> {
            try {
                String urlString = String.format(WEATHER_API_URL, city);
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();
                
                // 解析 JSON（简化版，实际需要根据 API 返回结构调整）
                WeatherData weather = parseWeatherData(response.toString());
                
                // 在主线程回调
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onWeatherLoaded(weather);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "获取天气失败", e);
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError(e.getMessage());
                });
            }
        }).start();
    }
    
    private static WeatherData parseWeatherData(String jsonData) {
        try {
            // 这里需要根据实际 API 返回的 JSON 结构来解析
            // 暂时返回模拟数据
            return new WeatherData("21°C", "晴朗", "☀️");
        } catch (Exception e) {
            Log.e(TAG, "解析天气数据失败", e);
            return new WeatherData("--", "未知", "❓");
        }
    }
}
