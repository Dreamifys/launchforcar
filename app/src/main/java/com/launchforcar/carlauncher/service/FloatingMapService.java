package com.launchforcar.carlauncher.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.launchforcar.carlauncher.R;
import com.launchforcar.carlauncher.data.local.LauncherPreferences;

/**
 * 地图悬浮窗触发服务
 * 用于向高德地图发送广播显示悬浮窗
 */
public class FloatingMapService extends Service {
    private static final String TAG = "FloatingMapService";
    private static final String CHANNEL_ID = "floating_map_channel";
    private static final int NOTIFICATION_ID = 1001;
    public static final String ACTION_TRIGGER_FLOATING = "com.launchforcar.TRIGGER_FLOATING";
    public static final String ACTION_CLOSE_FLOATING = "com.launchforcar.CLOSE_FLOATING";
    
    private LauncherPreferences preferences;
    
    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new LauncherPreferences(this);
        Log.d(TAG, "悬浮窗服务创建");
        
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "悬浮窗服务启动, action=" + (intent != null ? intent.getAction() : "null"));
        
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_TRIGGER_FLOATING.equals(action)) {
                // 读取位置信息
                int x = intent.getIntExtra("x", 0);
                int y = intent.getIntExtra("y", 0);
                int width = intent.getIntExtra("width", 0);
                int height = intent.getIntExtra("height", 0);
                triggerFloatingWindow(x, y, width, height);
            } else if (ACTION_CLOSE_FLOATING.equals(action)) {
                closeFloatingWindow();
            }
        }
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "悬浮窗服务销毁");
    }
    
    private void triggerFloatingWindow() {
        triggerFloatingWindow(0, 0, 0, 0);
    }
    
    private void triggerFloatingWindow(int x, int y, int width, int height) {
        String showAction = preferences.getFloatingShowAction();
        String associatedPackage = preferences.getAssociatedFloatingPackage();
        
        Log.d(TAG, "Triggering floating window, action=" + showAction + ", pkg=" + associatedPackage + 
              ", x=" + x + ", y=" + y + ", w=" + width + ", h=" + height);
        
        if (showAction != null && !showAction.isEmpty()) {
            try {
                Intent intent = new Intent(showAction);
                
                // 如果有传入位置信息，使用传入的；否则使用默认值
                int finalX = (x > 0) ? x : 50;
                int finalY = (y > 0) ? y : 100;
                int finalWidth = (width > 0) ? width : 600;
                int finalHeight = (height > 0) ? height : 400;
                
                // 高德地图悬浮窗广播参数
                if ("com.autonavi.plus.showmap".equals(showAction)) {
                    intent.putExtra("x", finalX);
                    intent.putExtra("y", finalY);
                    intent.putExtra("w", finalWidth);
                    intent.putExtra("h", finalHeight);
                } else {
                    // 其他应用使用通用参数
                    intent.putExtra("x", finalX);
                    intent.putExtra("y", finalY);
                    intent.putExtra("width", finalWidth);
                    intent.putExtra("height", finalHeight);
                }
                
                // 不设置Package，让所有应用都能收到这个广播
                // if (associatedPackage != null && !associatedPackage.isEmpty()) {
                //     intent.setPackage(associatedPackage);
                // }
                
                sendBroadcast(intent);
                Log.d(TAG, "Sent show broadcast: " + showAction + ", x=" + finalX + ", y=" + finalY + ", w=" + finalWidth + ", h=" + finalHeight);
                Toast.makeText(this, "已触发悬浮窗", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Failed to send show broadcast", e);
                Toast.makeText(this, "触发悬浮窗失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "未配置悬浮窗广播", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void closeFloatingWindow() {
        String closeAction = preferences.getFloatingCloseAction();
        String associatedPackage = preferences.getAssociatedFloatingPackage();
        
        Log.d(TAG, "Closing floating window, action=" + closeAction);
        
        if (closeAction != null && !closeAction.isEmpty()) {
            try {
                Intent intent = new Intent(closeAction);
                
                if (associatedPackage != null && !associatedPackage.isEmpty()) {
                    intent.setPackage(associatedPackage);
                }
                
                sendBroadcast(intent);
                Log.d(TAG, "Sent close broadcast: " + closeAction);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send close broadcast", e);
            }
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "地图悬浮窗",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("显示地图导航悬浮窗");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("悬浮窗服务运行中")
                .setContentText("监控应用切换并触发悬浮窗")
                .setSmallIcon(R.drawable.ic_maps)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
