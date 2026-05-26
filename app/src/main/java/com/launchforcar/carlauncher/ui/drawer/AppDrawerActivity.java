package com.launchforcar.carlauncher.ui.drawer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.launchforcar.carlauncher.data.model.AppItem;
import com.launchforcar.carlauncher.databinding.ActivityAppDrawerBinding;

import java.util.ArrayList;
import java.util.List;

public class AppDrawerActivity extends AppCompatActivity {

    private ActivityAppDrawerBinding binding;
    private AppDrawerAdapter adapter;
    private final List<AppItem> allApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        seedApps();
        bindSearch();

        binding.backButton.setOnClickListener(v -> finish());
        adapter.submitList(allApps);
    }

    private void setupRecyclerView() {
        adapter = new AppDrawerAdapter();
        binding.appList.setLayoutManager(new GridLayoutManager(this, 4));
        binding.appList.setAdapter(adapter);
    }

    private void bindSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterApps(String query) {
        String normalized = query.trim().toLowerCase();
        if (normalized.isEmpty()) {
            adapter.submitList(allApps);
            return;
        }

        List<AppItem> filtered = new ArrayList<>();
        for (AppItem appItem : allApps) {
            if (appItem.getName().toLowerCase().contains(normalized)
                    || appItem.getCategory().toLowerCase().contains(normalized)) {
                filtered.add(appItem);
            }
        }
        adapter.submitList(filtered);
    }

    private void seedApps() {
        allApps.add(new AppItem("高德地图", "导航出行"));
        allApps.add(new AppItem("百度地图", "导航出行"));
        allApps.add(new AppItem("QQ音乐", "音乐电台"));
        allApps.add(new AppItem("网易云音乐", "音乐电台"));
        allApps.add(new AppItem("蓝牙电话", "通讯"));
        allApps.add(new AppItem("最近联系人", "通讯"));
        allApps.add(new AppItem("哔哩哔哩", "娱乐"));
        allApps.add(new AppItem("爱奇艺", "娱乐"));
        allApps.add(new AppItem("艺术相框", "娱乐"));
        allApps.add(new AppItem("NAS 云盘", "工具"));
        allApps.add(new AppItem("车况中心", "车辆工具"));
        allApps.add(new AppItem("停车记录", "车辆工具"));
    }
}