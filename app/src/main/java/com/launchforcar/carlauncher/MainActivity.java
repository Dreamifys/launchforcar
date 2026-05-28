package com.launchforcar.carlauncher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.launchforcar.carlauncher.data.local.LauncherPreferences;
import com.launchforcar.carlauncher.service.FloatingMapService;
import com.launchforcar.carlauncher.ui.drawer.AppDrawerActivity;
import com.launchforcar.carlauncher.ui.floating.FloatingAppSelectorActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LauncherPreferences launcherPreferences;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcherPreferences = new LauncherPreferences(this);
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat = new SimpleDateFormat("yyyy-MM-dd EEEE", Locale.getDefault());
        handler = new Handler(Looper.getMainLooper());

        setupSystemBars();
        enableImmersiveMode();
        setContentView(R.layout.activity_main);
        setupListeners();
        updateDateTime();

        // 等待视图测量完成后再启动悬浮窗
        View mapContainer = findViewById(R.id.map_container);
        mapContainer.post(() -> {
            handler.postDelayed(this::autoLaunchFloatingMap, 300);
        });
    }

    private void setupSystemBars() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableImmersiveMode();
        updateDateTime();
        // 应用回到前台，重新启动悬浮窗
        autoLaunchFloatingMap();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 应用暂停，关闭悬浮窗
        closeFloatingMap();
    }

    private void closeFloatingMap() {
        String associatedPackage = launcherPreferences.getAssociatedFloatingPackage();
        String showAction = launcherPreferences.getFloatingShowAction();
        
        if (associatedPackage == null || associatedPackage.isEmpty() || 
            showAction == null || showAction.isEmpty()) {
            return;
        }
        
        // 启动 FloatingMapService 关闭悬浮窗
        Intent serviceIntent = new Intent(this, FloatingMapService.class);
        serviceIntent.setAction(FloatingMapService.ACTION_CLOSE_FLOATING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void setupListeners() {
        findViewById(R.id.music_play).setOnClickListener(v -> openMusicApp());
        findViewById(R.id.music_prev).setOnClickListener(v -> Toast.makeText(this, "上一曲", Toast.LENGTH_SHORT).show());
        findViewById(R.id.music_next).setOnClickListener(v -> Toast.makeText(this, "下一曲", Toast.LENGTH_SHORT).show());

        findViewById(R.id.map_container).setOnClickListener(v -> launchAssociatedFloatingApp());
        findViewById(R.id.map_container).setOnLongClickListener(v -> {
            openFloatingAppSelector();
            return true;
        });

        findViewById(R.id.map_add_button).setOnClickListener(v -> launchAssociatedFloatingApp());
        findViewById(R.id.map_add_button).setOnLongClickListener(v -> {
            openFloatingAppSelector();
            return true;
        });

        findViewById(R.id.app_drawer_btn).setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, AppDrawerActivity.class)));

        for (int i = 1; i <= 5; i++) {
            int id = getResources().getIdentifier("quick_app_" + i, "id", getPackageName());
            View btn = findViewById(id);
            if (btn != null) {
                final int finalI = i;
                btn.setOnClickListener(v -> Toast.makeText(this, "快捷应用 " + finalI, Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void updateDateTime() {
        TextView timeText = findViewById(R.id.time_text);
        TextView dateText = findViewById(R.id.date_text);
        if (timeText != null) timeText.setText(timeFormat.format(new Date()));
        if (dateText != null) dateText.setText(dateFormat.format(new Date()));
    }

    private void autoLaunchFloatingMap() {
        String associatedPackage = launcherPreferences.getAssociatedFloatingPackage();
        String showAction = launcherPreferences.getFloatingShowAction();

        if (associatedPackage == null || associatedPackage.isEmpty() ||
            showAction == null || showAction.isEmpty()) {
            return;
        }

        if (!Settings.canDrawOverlays(this)) {
            return;
        }

        // 启动 FloatingMapService 来触发悬浮窗，传递位置信息
        launchFloatingMapWithPosition();
    }

    private void launchAssociatedFloatingApp() {
        String associatedPackage = launcherPreferences.getAssociatedFloatingPackage();
        String showAction = launcherPreferences.getFloatingShowAction();

        if (associatedPackage == null || associatedPackage.isEmpty()) {
            showNoAssociatedAppDialog();
            return;
        }

        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission();
            return;
        }

        // 使用 FloatingMapService 来发送广播触发悬浮窗（不跳转），传递位置信息
        launchFloatingMapWithPosition();
    }

    private void launchFloatingMapWithPosition() {
        // 直接获取屏幕尺寸计算位置
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        
        // 左侧Dock栏是60dp，转换成像素
        int leftDockWidth = dpToPx(60);
        // 右侧插件区是280dp
        int rightPanelWidth = dpToPx(280);
        
        // 计算中间区域位置
        int x = leftDockWidth; // 从左侧Dock栏右边开始
        int y = 0; // 从屏幕顶部开始
        int width = screenWidth - leftDockWidth - rightPanelWidth; // 中间区域宽度
        int height = screenHeight; // 高度全屏
        
        Log.d("MainActivity", "Screen: " + screenWidth + "x" + screenHeight + 
              ", x=" + x + ", y=" + y + ", w=" + width + ", h=" + height);
        
        Intent serviceIntent = new Intent(this, FloatingMapService.class);
        serviceIntent.setAction(FloatingMapService.ACTION_TRIGGER_FLOATING);
        serviceIntent.putExtra("x", x);
        serviceIntent.putExtra("y", y);
        serviceIntent.putExtra("width", width);
        serviceIntent.putExtra("height", height);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private void openFloatingAppSelector() {
        Intent intent = new Intent(this, FloatingAppSelectorActivity.class);
        startActivity(intent);
    }

    private void showNoAssociatedAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("未关联悬浮应用");
        builder.setMessage("请长按地图区域，选择要关联的悬浮应用");
        builder.setPositiveButton("我知道了", null);
        builder.setNeutralButton("立即设置", (dialog, which) -> openFloatingAppSelector());
        builder.show();
    }

    private void requestOverlayPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("需要悬浮窗权限");
        builder.setMessage("使用悬浮应用需要授予悬浮窗权限，请前往设置开启");
        builder.setPositiveButton("前往设置", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void openMusicApp() {
        String[] musicPackages = {
            "com.tencent.qqmusiccar",
            "com.netease.cloudmusic",
            "com.kugou.android.car",
            "com.xiami.car"
        };

        for (String pkg : musicPackages) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent != null) {
                startActivity(intent);
                return;
            }
        }

        Toast.makeText(this, "请先安装音乐应用", Toast.LENGTH_SHORT).show();
    }

    private void enableImmersiveMode() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
