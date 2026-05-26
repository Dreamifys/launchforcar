package com.launchforcar.carlauncher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.launchforcar.carlauncher.data.CarDataService;
import com.launchforcar.carlauncher.data.WeatherService;
import com.launchforcar.carlauncher.data.local.LauncherPreferences;
import com.launchforcar.carlauncher.data.local.ThemePreferences;
import com.launchforcar.carlauncher.databinding.ActivityMainBinding;
import com.launchforcar.carlauncher.ui.drawer.AppDrawerActivity;
import com.launchforcar.carlauncher.ui.edit.EditCardsActivity;
import com.launchforcar.carlauncher.ui.settings.SettingsActivity;
import com.launchforcar.carlauncher.utils.ThemeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.DialogInterface;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private LauncherPreferences launcherPreferences;
    
    // 手势检测
    private GestureDetector gestureDetector;

    private ThemePreferences themePreferences;
    private SimpleDateFormat timeFormat;
    
    // 悬浮窗权限请求 Launcher
    private ActivityResultLauncher<Intent> overlayPermissionLauncher;
    private int currentModuleIndex = 0; // 0=天气, 1=车辆, 2=快捷按钮
    private View[] moduleCards;
    private View[] indicatorDots;
    
    // 常用应用包名映射
    private static final String PACKAGE_MAPS = "com.autonavi.amapauto"; // 高德地图
    private static final String PACKAGE_QQ_MUSIC = "com.tencent.qqmusiccar"; // QQ音乐车机版
    private static final String PACKAGE_NETEASE_MUSIC = "com.netease.cloudmusic"; // 网易云音乐
    private static final String PACKAGE_DIALER = "com.android.dialer"; // 电话

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化偏好设置
        launcherPreferences = new LauncherPreferences(this);
        
        // 设置状态栏和导航栏
        setupSystemBars();

        themePreferences = new ThemePreferences(this);
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        // 初始化权限请求
        initPermissionLauncher();
        
        // 根据时间自动应用主题
        applyAutoTheme();
        
        // 检查悬浮窗权限
        checkOverlayPermission();
        
        enableImmersiveMode();
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupUI();
        setupListeners();
        updateDateTime();
    }
    
    /**
     * 设置状态栏和导航栏样式
     */
    private void setupSystemBars() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);
        }
    }
    
    /**
     * 检查悬浮窗权限
     */
    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // 不主动请求权限，仅在用户使用悬浮窗功能时提示
            android.util.Log.d("MainActivity", "悬浮窗权限未授予");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableImmersiveMode();
        updateDateTime();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveMode();
        }
    }

    private void setupUI() {
        // 设置默认文本
        if (binding.musicTitle != null) {
            binding.musicTitle.setText("未播放音乐");
        }
        if (binding.musicArtist != null) {
            binding.musicArtist.setText("点击选择音乐应用");
        }
        
        // 初始化地图显示
        initMapDisplay();
        
        // 加载天气数据
        loadWeatherData();
        
        // 加载车辆数据
        loadCarData();
        
        // 初始化模块切换
        initModuleSwitcher();
        
        // 应用主题到所有视图
        applyThemeToAllViews();
    }

    private void setupListeners() {
        // 快捷操作按钮
        if (binding.quickNav != null) {
            binding.quickNav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNavigationApp();
                }
            });
        }
        
        if (binding.quickMusic != null) {
            binding.quickMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMusicApp();
                }
            });
        }
        
        if (binding.quickPhone != null) {
            binding.quickPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialer();
                }
            });
        }
        
        if (binding.quickApps != null) {
            binding.quickApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, AppDrawerActivity.class));
                }
            });
        }
        
        if (binding.quickSettings != null) {
            binding.quickSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleTheme();
                }
            });
        }
        
        // 音乐控制按钮
        if (binding.musicPlay != null) {
            binding.musicPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openMusicApp();
                }
            });
        }
        
        if (binding.musicPrev != null) {
            binding.musicPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "上一曲", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (binding.musicNext != null) {
            binding.musicNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "下一曲", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // 卡片点击事件
        if (binding.mapContainer != null) {
            binding.mapContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 根据设置的显示模式执行不同操作
                    String displayMode = launcherPreferences.getMapDisplayMode();
                    
                    if ("floating".equals(displayMode)) {
                        // 悬浮窗模式 - 启动高德地图悬浮版
                        launchAmapFloating();
                    } else if ("external".equals(displayMode)) {
                        // 外部应用模式 - 启动已设置的地图应用
                        openNavigationApp();
                    } else {
                        // 嵌入式地图 - 显示提示（地图已在 WebView 中加载）
                        Toast.makeText(MainActivity.this, 
                            "地图已加载，长按可切换模式", 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            // 长按地图容器打开设置对话框
            binding.mapContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showMapAppSelector();
                    return true;
                }
            });
        }
        
        if (binding.weatherCard != null) {
            binding.weatherCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadWeatherData(); // 点击刷新天气
                }
            });
        }
        
        if (binding.carStatusCard != null) {
            binding.carStatusCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "车辆详情", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * 初始化模块切换器（左右滑动切换）
     */
    private void initModuleSwitcher() {
        // 获取三个模块卡片
        moduleCards = new View[]{
            findViewById(R.id.weather_card),
            findViewById(R.id.car_status_card),
            findViewById(R.id.status_module_card)
        };
        
        // 获取指示器
        indicatorDots = new View[]{
            findViewById(R.id.indicator_weather),
            findViewById(R.id.indicator_car),
            findViewById(R.id.indicator_quick)
        };
        
        // 获取可滑动的 FrameLayout 容器
        final View swipeContainer = findViewById(R.id.swipe_container);
        
        // 初始化手势检测器
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                
                // 确保主要是水平滑动
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // 向右滑动 - 切换到上一个模块
                            switchToPreviousModule();
                        } else {
                            // 向左滑动 - 切换到下一个模块
                            switchToNextModule();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        
        // 在可滑动区域设置触摸监听
        if (swipeContainer != null) {
            swipeContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
            
            // 确保容器可以接收触摸事件
            swipeContainer.setClickable(true);
            swipeContainer.setFocusableInTouchMode(true);
        }
        
        // 也为每个模块卡片设置触摸监听（作为备用）
        for (View card : moduleCards) {
            if (card != null) {
                card.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return gestureDetector.onTouchEvent(event);
                    }
                });
            }
        }
        
        // 默认显示天气模块
        switchToModule(0);
    }
    
    /**
     * 切换到指定模块
     */
    private void switchToModule(int index) {
        if (index < 0 || index >= moduleCards.length) return;
        
        currentModuleIndex = index;
        
        // 隐藏所有模块
        for (View card : moduleCards) {
            if (card != null) {
                card.setVisibility(View.GONE);
            }
        }
        
        // 显示当前模块
        if (moduleCards[index] != null) {
            moduleCards[index].setVisibility(View.VISIBLE);
        }
        
        // 更新指示器
        updateIndicators();
    }
    
    /**
     * 切换到下一个模块
     */
    private void switchToNextModule() {
        int nextIndex = (currentModuleIndex + 1) % moduleCards.length;
        switchToModule(nextIndex);
    }
    
    /**
     * 切换到上一个模块
     */
    private void switchToPreviousModule() {
        int prevIndex = (currentModuleIndex - 1 + moduleCards.length) % moduleCards.length;
        switchToModule(prevIndex);
    }
    
    /**
     * 更新页面指示器
     */
    private void updateIndicators() {
        if (indicatorDots == null) return;
        
        int activeColor = getResources().getColor(R.color.accent_blue_ios, getTheme());
        int inactiveColor = getResources().getColor(R.color.text_quaternary_light, getTheme());
        
        // 重置所有指示器
        for (int i = 0; i < indicatorDots.length; i++) {
            if (indicatorDots[i] != null) {
                if (i == currentModuleIndex) {
                    // 激活状态：宽条 + 蓝色
                    indicatorDots[i].setLayoutParams(
                        new LinearLayout.LayoutParams(dpToPx(24), dpToPx(4)));
                    indicatorDots[i].setBackgroundColor(activeColor);
                } else {
                    // 非激活状态：窄条 + 灰色
                    indicatorDots[i].setLayoutParams(
                        new LinearLayout.LayoutParams(dpToPx(8), dpToPx(4)));
                    indicatorDots[i].setBackgroundColor(inactiveColor);
                }
            }
        }
    }

    private void openNavigationApp() {
        // 优先使用用户设置的导航应用
        String navPackage = launcherPreferences.getNavigationAppPackage();
        if (navPackage != null && !navPackage.isEmpty()) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(navPackage);
            if (intent != null) {
                startActivity(intent);
                return;
            }
        }
        
        // 尝试高德地图
        Intent intent = getPackageManager().getLaunchIntentForPackage(PACKAGE_MAPS);
        if (intent != null) {
            launcherPreferences.setNavigationAppPackage(PACKAGE_MAPS);
            startActivity(intent);
        } else {
            // 尝试百度地图
            intent = getPackageManager().getLaunchIntentForPackage("com.baidu.BaiduMap");
            if (intent != null) {
                launcherPreferences.setNavigationAppPackage("com.baidu.BaiduMap");
                startActivity(intent);
            } else {
                Toast.makeText(this, "请先安装地图应用", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openMusicApp() {
        // 尝试常见的音乐应用
        String[] musicPackages = {
            PACKAGE_QQ_MUSIC,
            PACKAGE_NETEASE_MUSIC,
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

    private void openDialer() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "无法打开拨号器", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化权限请求
     */
    private void initPermissionLauncher() {
        overlayPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "悬浮窗权限已授予", Toast.LENGTH_SHORT).show();
                        // 启动高德地图悬浮版
                        launchAmapFloating();
                    } else {
                        Toast.makeText(this, "需要悬浮窗权限才能使用此功能", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }
    
    /**
     * 启动高德地图悬浮窗（画中画模式）
     */
    private void launchAmapFloating() {
        // 检查悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            // 请求悬浮窗权限
            Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            overlayPermissionLauncher.launch(intent);
            return;
        }
        
        // 启动悬浮窗服务，显示画中画地图
        Intent serviceIntent = new Intent(this, com.launchforcar.carlauncher.service.FloatingMapService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        Toast.makeText(this, "已启动地图悬浮窗", Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", "悬浮窗服务已启动");
    }
    
    /**
     * 显示安装高德地图悬浮版的提示
     */
    private void showInstallAmapDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("需要安装高德地图悬浮版");
        builder.setMessage("此功能需要高德地图车机悬浮版。\n\n请下载安装后重试。");
        builder.setPositiveButton("我知道了", null);
        builder.setNegativeButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 打开浏览器下载页面
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://lbs.amap.com/"));
                startActivity(intent);
            }
        });
        builder.show();
    }

    private void updateDateTime() {
        if (binding.statusTime != null) {
            binding.statusTime.setText(timeFormat.format(new Date()));
        }
    }
    
    private void loadWeatherData() {
        // 默认获取深圳的天气（可以根据定位动态调整）
        WeatherService.getWeather("Shenzhen", new WeatherService.WeatherCallback() {
            @Override
            public void onWeatherLoaded(WeatherService.WeatherData weather) {
                if (binding.weatherTemp != null) {
                    binding.weatherTemp.setText(weather.temperature);
                }
                if (binding.weatherDesc != null) {
                    binding.weatherDesc.setText(weather.description);
                }
                if (binding.statusWeather != null) {
                    binding.statusWeather.setText(weather.icon + " " + weather.temperature);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e("MainActivity", "天气加载失败: " + error);
                // 使用默认数据
                if (binding.weatherTemp != null) {
                    binding.weatherTemp.setText("21°C");
                }
                if (binding.weatherDesc != null) {
                    binding.weatherDesc.setText("晴朗");
                }
            }
        });
    }
    
    private void loadCarData() {
        CarDataService.getCarData(new CarDataService.CarDataCallback() {
            @Override
            public void onCarDataLoaded(CarDataService.CarData data) {
                if (binding.carRange != null) {
                    binding.carRange.setText(data.range);
                }
                if (binding.carFuel != null) {
                    binding.carFuel.setText(data.fuel);
                }
                if (binding.carTire != null) {
                    binding.carTire.setText(data.tirePressure);
                }
                if (binding.carMileage != null) {
                    binding.carMileage.setText(data.mileage);
                }
            }
        });
        
        // 启动定期更新（每 30 秒）
        CarDataService.startPeriodicUpdate(new CarDataService.CarDataCallback() {
            @Override
            public void onCarDataLoaded(CarDataService.CarData data) {
                if (binding.carRange != null) {
                    binding.carRange.setText(data.range);
                }
                if (binding.carFuel != null) {
                    binding.carFuel.setText(data.fuel);
                }
                if (binding.carTire != null) {
                    binding.carTire.setText(data.tirePressure);
                }
                if (binding.carMileage != null) {
                    binding.carMileage.setText(data.mileage);
                }
            }
        });
    }
    
    private boolean isDarkTheme = false;
    
    private void applyAutoTheme() {
        String themeMode = themePreferences.getThemeMode();
        
        if ("auto".equals(themeMode) || themePreferences.isAutoThemeEnabled()) {
            // 自动模式：根据时间判断
            isDarkTheme = ThemeUtils.shouldUseDarkTheme();
        } else if ("dark".equals(themeMode)) {
            isDarkTheme = true;
        } else {
            isDarkTheme = false;
        }
        
        Log.d("MainActivity", "应用主题: " + (isDarkTheme ? "深色" : "浅色") + 
              " (当前时间: " + timeFormat.format(new Date()) + ")");
    }
    
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        
        // 保存用户选择（关闭自动模式）
        themePreferences.setAutoThemeEnabled(false);
        themePreferences.setThemeMode(isDarkTheme ? "dark" : "light");
        
        applyTheme();
        Toast.makeText(this, isDarkTheme ? "已切换到深色主题" : "已切换到浅色主题", Toast.LENGTH_SHORT).show();
    }
    
    private void applyTheme() {
        if (binding.rootContainer == null) return;
        
        if (isDarkTheme) {
            // iOS 深色主题
            binding.rootContainer.setBackgroundResource(R.color.background_dark_start);
            
            // 更新地图容器背景
            if (binding.mapContainer != null) {
                binding.mapContainer.setBackgroundResource(R.drawable.bg_map_glass);
            }
            
            // 更新所有卡片背景为深色
            updateCardBackgrounds(true);
            
            // 更新文字颜色
            updateTextColors(true);
            
        } else {
            // iOS 浅色主题
            binding.rootContainer.setBackgroundResource(R.color.background_light_start);
            
            // 更新地图容器背景
            if (binding.mapContainer != null) {
                binding.mapContainer.setBackgroundResource(R.drawable.bg_map_glass);
            }
            
            // 更新所有卡片背景为浅色
            updateCardBackgrounds(false);
            
            // 更新文字颜色
            updateTextColors(false);
        }
    }
    
    private void applyThemeToAllViews() {
        applyTheme();
    }
    
    private void updateCardBackgrounds(boolean isDark) {
        int cardColor = isDark ? R.color.card_background_dark : R.color.card_background_light;
        
        // 音乐卡片
        if (binding.musicCard != null) {
            binding.musicCard.setCardBackgroundColor(getResources().getColor(cardColor, getTheme()));
        }
        
        // 天气卡片
        if (binding.weatherCard != null) {
            binding.weatherCard.setCardBackgroundColor(getResources().getColor(cardColor, getTheme()));
        }
        
        // 车辆状态卡片
        if (binding.carStatusCard != null) {
            binding.carStatusCard.setCardBackgroundColor(getResources().getColor(cardColor, getTheme()));
        }
        
        // 状态模块卡片
        View statusModuleCard = findViewById(R.id.status_module_card);
        if (statusModuleCard instanceof androidx.cardview.widget.CardView) {
            ((androidx.cardview.widget.CardView) statusModuleCard).setCardBackgroundColor(
                getResources().getColor(cardColor, getTheme()));
        }
    }
    
    private void updateTextColors(boolean isDark) {
        int primaryColor = isDark ? R.color.text_primary_dark : R.color.text_primary_light;
        int secondaryColor = isDark ? R.color.text_secondary_dark : R.color.text_secondary_light;
        int tertiaryColor = isDark ? R.color.text_tertiary_dark : R.color.text_tertiary_light;
        
        // 状态栏文字
        if (binding.statusTime != null) {
            binding.statusTime.setTextColor(getResources().getColor(primaryColor, null));
        }
        if (binding.statusWeather != null) {
            binding.statusWeather.setTextColor(getResources().getColor(secondaryColor, null));
        }
        if (binding.statusLocation != null) {
            binding.statusLocation.setTextColor(getResources().getColor(secondaryColor, null));
        }
        
        // 音乐卡片文字
        if (binding.musicTitle != null) {
            binding.musicTitle.setTextColor(getResources().getColor(primaryColor, null));
        }
        if (binding.musicArtist != null) {
            binding.musicArtist.setTextColor(getResources().getColor(secondaryColor, null));
        }
        
        // 天气卡片文字
        TextView weatherTemp = findViewById(R.id.weather_temp);
        TextView weatherDesc = findViewById(R.id.weather_desc);
        if (weatherTemp != null) {
            weatherTemp.setTextColor(getResources().getColor(primaryColor, null));
        }
        if (weatherDesc != null) {
            weatherDesc.setTextColor(getResources().getColor(secondaryColor, null));
        }
        
        // 车辆状态卡片文字
        updateCarStatusTextColors(isDark);
        
        // 快捷按钮文字
        updateQuickButtonColors(isDark);
    }
    
    private void updateQuickButtonColors(boolean isDark) {
        int textColor = isDark ? R.color.text_primary_dark : R.color.text_primary_light;
        
        String[] buttonIds = {"quick_nav", "quick_music", "quick_phone", "quick_apps", "quick_settings"};
        for (String id : buttonIds) {
            int resId = getResources().getIdentifier(id, "id", getPackageName());
            TextView button = findViewById(resId);
            if (button != null) {
                button.setTextColor(getResources().getColor(textColor, null));
            }
        }
    }
    
    private void updateCarStatusTextColors(boolean isDark) {
        int labelColor = isDark ? R.color.text_tertiary_dark : R.color.text_tertiary_light;
        int valueColor = isDark ? R.color.text_primary_dark : R.color.text_primary_light;
        
        // 这里需要根据实际的 TextView ID 来更新
        // 暂时跳过，因为布局中使用了 style
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

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    /**
     * 初始化地图显示
     */
    private void initMapDisplay() {
        String navPackage = launcherPreferences.getNavigationAppPackage();
        if (navPackage != null && !navPackage.isEmpty()) {
            // 获取应用名称
            try {
                PackageManager pm = getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(navPackage, 0);
                String appName = pm.getApplicationLabel(appInfo).toString();
                updateMapContainerDisplay(appName);
            } catch (PackageManager.NameNotFoundException e) {
                // 应用未找到，使用默认文本
                launcherPreferences.setNavigationAppPackage("");
            }
        }
    }
    
    /**
     * 显示地图应用选择器对话框
     */
    private void showMapAppSelector() {
        // 获取当前模式
        String currentMode = launcherPreferences.getMapDisplayMode();
        int defaultChoice = 0;
        if ("floating".equals(currentMode)) {
            defaultChoice = 0;
        } else if ("external".equals(currentMode)) {
            defaultChoice = 1;
        } else {
            defaultChoice = 2;
        }
        
        // 检测已安装的高德地图应用
        PackageManager pm = getPackageManager();
        String[] amapPackages = {
            "com.autonavi.amapautolite",  // 高德地图车机悬浮版 Lite
            "com.autonavi.amapauto",      // 高德地图车机悬浮版
            "com.autonavi.minimap"         // 高德地图主应用
        };
        
        boolean hasFloatingVersion = false;
        
        for (String pkg : amapPackages) {
            try {
                pm.getPackageInfo(pkg, 0);
                
                // 检查是否为悬浮版
                if ("com.autonavi.amapautolite".equals(pkg) || "com.autonavi.amapauto".equals(pkg)) {
                    hasFloatingVersion = true;
                    break;
                }
            } catch (PackageManager.NameNotFoundException e) {
                // 未安装
            }
        }
        
        // 构建选项列表
        String[] options;
        String floatingOptionText;
        
        if (hasFloatingVersion) {
            floatingOptionText = "高德地图悬浮窗（推荐）";
        } else {
            floatingOptionText = "高德地图悬浮窗（未安装）";
        }
        
        options = new String[]{
            floatingOptionText,
            "外部地图应用",
            "嵌入式地图（WebView）"
        };
        
        // 显示选择对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("地图显示模式");
        
        builder.setSingleChoiceItems(options, defaultChoice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // 悬浮窗模式
                    launcherPreferences.setMapDisplayMode("floating");
                    dialog.dismiss();
                    // 直接启动高德地图悬浮版
                    launchAmapFloating();
                } else if (which == 1) {
                    // 外部地图应用
                    launcherPreferences.setMapDisplayMode("external");
                    dialog.dismiss();
                    showExternalMapAppSelector();
                } else {
                    // 嵌入式地图
                    launcherPreferences.setMapDisplayMode("embedded");
                    Toast.makeText(MainActivity.this, 
                        "已切换到嵌入式地图", 
                        Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    /**
     * 显示外部地图应用选择器对话框
     */
    private void showExternalMapAppSelector() {
        // 获取所有已安装的地图应用
        List<MapAppInfo> mapApps = getInstalledMapApps();
        
        if (mapApps.isEmpty()) {
            Toast.makeText(this, "未检测到地图应用，请先安装", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 构建选项列表
        String[] appNames = new String[mapApps.size()];
        for (int i = 0; i < mapApps.size(); i++) {
            appNames[i] = mapApps.get(i).appName;
        }
        
        // 获取当前选中的应用
        String currentPackage = launcherPreferences.getNavigationAppPackage();
        int currentIndex = -1;
        for (int i = 0; i < mapApps.size(); i++) {
            if (mapApps.get(i).packageName.equals(currentPackage)) {
                currentIndex = i;
                break;
            }
        }
        
        // 显示选择对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择默认地图应用");
        builder.setSingleChoiceItems(appNames, currentIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MapAppInfo selectedApp = mapApps.get(which);
                launcherPreferences.setNavigationAppPackage(selectedApp.packageName);
                Toast.makeText(MainActivity.this, 
                    "已设置 " + selectedApp.appName + " 为默认地图", 
                    Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                
                // 更新地图容器显示
                updateMapContainerDisplay(selectedApp.appName);
            }
        });
        
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    /**
     * 获取已安装的地图应用列表
     */
    private List<MapAppInfo> getInstalledMapApps() {
        List<MapAppInfo> mapApps = new ArrayList<>();
        PackageManager pm = getPackageManager();
        
        // 常见的地图应用包名
        String[] mapPackages = {
            "com.autonavi.minimap",           // 高德地图
            "com.baidu.BaiduMap",            // 百度地图
            "com.tencent.map",               // 腾讯地图
            "com.google.android.apps.maps",  // Google Maps
            "com.mapswithme.maps.pro",       // MAPS.ME
            "app.organicmaps"                // Organic Maps
        };
        
        for (String packageName : mapPackages) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                String appName = pm.getApplicationLabel(appInfo).toString();
                mapApps.add(new MapAppInfo(packageName, appName));
            } catch (PackageManager.NameNotFoundException e) {
                // 应用未安装，跳过
            }
        }
        
        return mapApps;
    }
    
    /**
     * 更新地图容器显示
     */
    private void updateMapContainerDisplay(String appName) {
        if (binding.mapContainer != null && binding.mapHintText != null) {
            // 更新提示文字，显示当前选择的地图应用
            binding.mapHintText.setText(appName);
        }
    }
    
    /**
     * 根据包名获取应用名称
     */
    private String getAppNameByPackage(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }
    
    /**
     * 地图应用信息类
     */
    private static class MapAppInfo {
        String packageName;
        String appName;
        
        MapAppInfo(String packageName, String appName) {
            this.packageName = packageName;
            this.appName = appName;
        }
    }
}
