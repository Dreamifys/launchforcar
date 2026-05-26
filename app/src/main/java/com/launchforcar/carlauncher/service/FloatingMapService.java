package com.launchforcar.carlauncher.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.launchforcar.carlauncher.R;

/**
 * 地图悬浮窗服务
 * 用于在桌面上层显示高德地图导航窗口（画中画模式）
 */
public class FloatingMapService extends Service {
    private static final String TAG = "FloatingMapService";
    private static final String CHANNEL_ID = "floating_map_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private WindowManager windowManager;
    private View floatingView;
    private WebView mapView;
    private WindowManager.LayoutParams params;
    
    // 悬浮窗初始位置
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "悬浮窗服务创建");
        
        // 创建通知渠道
        createNotificationChannel();
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification());
        
        // 创建悬浮窗
        createFloatingWindow();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "悬浮窗服务启动");
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
        
        // 移除悬浮窗
        if (windowManager != null && floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }
    
    /**
     * 创建通知渠道
     */
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
    
    /**
     * 创建前台服务通知
     */
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("地图悬浮窗运行中")
            .setContentText("点击关闭悬浮窗")
            .setSmallIcon(R.drawable.ic_maps)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }
    
    /**
     * 创建悬浮窗
     */
    private void createFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // 加载悬浮窗布局
        floatingView = LayoutInflater.from(this).inflate(R.layout.view_floating_map, null);
        
        // 设置悬浮窗参数
        params = new WindowManager.LayoutParams(
            dpToPx(400),  // 宽度
            dpToPx(300),  // 高度
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
                : WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        );
        
        // 设置初始位置（右上角）
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = dpToPx(20);
        params.y = dpToPx(100);
        
        // 添加悬浮窗
        windowManager.addView(floatingView, params);
        
        // 初始化地图 WebView
        initMapView();
        
        // 设置拖动功能
        setupDraggable();
        
        // 设置关闭按钮
        setupCloseButton();
        
        Log.d(TAG, "悬浮窗已创建");
    }
    
    /**
     * 初始化地图 WebView
     */
    private void initMapView() {
        mapView = floatingView.findViewById(R.id.mapWebView);
        
        if (mapView != null) {
            WebSettings webSettings = mapView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setGeolocationEnabled(true);
            webSettings.setBuiltInZoomControls(false);
            webSettings.setDisplayZoomControls(false);
            
            mapView.setWebViewClient(new WebViewClient());
            
            // 加载高德地图 HTML
            mapView.loadUrl("file:///android_asset/amap.html");
            
            Log.d(TAG, "地图 WebView 已初始化");
        }
    }
    
    /**
     * 设置拖动功能
     */
    private void setupDraggable() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }
    
    /**
     * 设置关闭按钮
     */
    private void setupCloseButton() {
        ImageButton closeButton = floatingView.findViewById(R.id.btnClose);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                stopSelf();
                Toast.makeText(this, "悬浮窗已关闭", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    /**
     * dp 转 px
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
