package com.launchforcar.carlauncher.ui.main;

import com.launchforcar.carlauncher.R;
import com.launchforcar.carlauncher.databinding.ActivityMainBinding;

public class HomeStateRenderer {

    private final ActivityMainBinding binding;

    public HomeStateRenderer(ActivityMainBinding binding) {
        this.binding = binding;
    }

    public void render(HomeUiState homeUiState) {
        // 新布局使用深色主题，不需要单独设置背景
    }
}