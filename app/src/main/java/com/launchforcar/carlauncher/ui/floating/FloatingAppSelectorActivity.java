package com.launchforcar.carlauncher.ui.floating;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.launchforcar.carlauncher.R;
import com.launchforcar.carlauncher.data.local.LauncherPreferences;
import com.launchforcar.carlauncher.service.FloatingMapService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FloatingAppSelectorActivity extends AppCompatActivity {

    private LauncherPreferences launcherPreferences;
    private List<AppInfo> allApps = new ArrayList<>();
    private AppListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcherPreferences = new LauncherPreferences(this);
        setContentView(R.layout.activity_floating_app_selector_simple);
        loadApps();
        setupViews();
        checkAccessibilityService();
    }

    private void loadApps() {
        allApps.clear();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : installedApps) {
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 ||
                (appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                    AppInfo info = new AppInfo();
                    info.packageName = appInfo.packageName;
                    info.appName = pm.getApplicationLabel(appInfo).toString();
                    info.icon = pm.getApplicationIcon(appInfo);
                    info.category = guessAppCategory(appInfo.packageName);
                    allApps.add(info);
                }
            }
        }

        Collections.sort(allApps, (a1, a2) -> a1.appName.compareToIgnoreCase(a2.appName));
        adapter = new AppListAdapter(allApps);
    }

    private void setupViews() {
        ListView listView = findViewById(R.id.app_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            AppInfo app = allApps.get(position);
            showActionDialog(app);
        });

        findViewById(R.id.close_button).setOnClickListener(v -> finish());
    }

    private void checkAccessibilityService() {
        // 这里可以添加检查无障碍服务是否已开启的逻辑
    }

    private void showActionDialog(AppInfo app) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择应用: " + app.appName);

        final EditText input = new EditText(this);
        input.setHint("输入悬浮窗广播Action");
        input.setText(getDefaultAction(app.packageName));
        builder.setView(input);

        builder.setPositiveButton("关联并启动", (dialog, which) -> {
            String action = input.getText().toString().trim();
            if (!action.isEmpty()) {
                associateApp(app, action);
            } else {
                Toast.makeText(this, "请输入有效的Action", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private String getDefaultAction(String packageName) {
        String lower = packageName.toLowerCase();
        if (lower.contains("amap") || lower.contains("autonavi") || lower.contains("高德")) {
            // 使用正确的高德悬浮窗广播
            return "com.autonavi.plus.showmap";
        }
        if (lower.contains("baidu") || lower.contains("百度")) {
            return "com.baidu.map.action.SHOW_WINDOW";
        }
        return "";
    }

    private String guessAppCategory(String packageName) {
        String lower = packageName.toLowerCase();
        if (lower.contains("map") || lower.contains("navi") || lower.contains("高德") || lower.contains("百度")) {
            return "导航";
        }
        if (lower.contains("music") || lower.contains("qqmusic") || lower.contains("网易")) {
            return "音乐";
        }
        return "其他";
    }

    private void associateApp(AppInfo app, String action) {
        launcherPreferences.setAssociatedFloatingPackage(app.packageName);
        launcherPreferences.setAssociatedFloatingAppName(app.appName);
        launcherPreferences.setFloatingShowAction(action);
        
        // 高德地图特殊处理：关闭Action是 com.autonavi.plus.closemap
        String closeAction = action;
        if ("com.autonavi.plus.showmap".equals(action)) {
            closeAction = "com.autonavi.plus.closemap";
        } else {
            closeAction = action.replace("show", "close");
        }
        launcherPreferences.setFloatingCloseAction(closeAction);

        Toast.makeText(this, "已关联: " + app.appName, Toast.LENGTH_SHORT).show();
        
        finish();
    }

    private class AppInfo {
        String packageName;
        String appName;
        Drawable icon;
        String category;
    }

    private class AppListAdapter extends ArrayAdapter<AppInfo> {
        public AppListAdapter(List<AppInfo> apps) {
            super(FloatingAppSelectorActivity.this, R.layout.item_app_simple, apps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_app_simple, parent, false);
            }

            AppInfo app = getItem(position);

            ImageView icon = view.findViewById(R.id.app_icon);
            TextView name = view.findViewById(R.id.app_name);
            TextView packageName = view.findViewById(R.id.app_package);
            TextView category = view.findViewById(R.id.app_category);

            icon.setImageDrawable(app.icon);
            name.setText(app.appName);
            packageName.setText(app.packageName);
            category.setText(app.category);

            boolean isAssociated = app.packageName.equals(launcherPreferences.getAssociatedFloatingPackage());
            if (isAssociated) {
                name.setText(app.appName + " ✓");
            }

            return view;
        }
    }
}
