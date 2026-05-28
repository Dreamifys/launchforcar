package com.launchforcar.carlauncher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.launchforcar.carlauncher.data.local.LauncherPreferences;
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

        // 延迟启动悬浮窗，避免立即崩溃
        handler.postDelayed(this::autoLaunchFloatingMap, 500);
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
    }

    private void setupListeners() {
        findViewById(R.id.nav_btn).setOnClickListener(v -> launchAssociatedFloatingApp());
        findViewById(R.id.home_btn).setOnClickListener(v -> {
            Toast.makeText(this, "导航回家", Toast.LENGTH_SHORT).show();
            launchAssociatedFloatingApp();
        });
        findViewById(R.id.work_btn).setOnClickListener(v -> {
            Toast.makeText(this, "导航去公司", Toast.LENGTH_SHORT).show();
            launchAssociatedFloatingApp();
        });

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

        try {
            Intent intent = new Intent(showAction);
            sendBroadcast(intent);
            Toast.makeText(this, "正在启动悬浮窗...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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

        if (showAction != null && !showAction.isEmpty()) {
            try {
                Intent intent = new Intent(showAction);
                sendBroadcast(intent);
                Toast.makeText(this, "正在启动悬浮窗...", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "启动失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(associatedPackage);
            if (launchIntent != null) {
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
                Toast.makeText(this, "正在启动应用...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "无法启动应用", Toast.LENGTH_SHORT).show();
            }
        }
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