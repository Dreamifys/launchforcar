package com.launchforcar.carlauncher.ui.main;

import com.launchforcar.carlauncher.R;
import com.launchforcar.carlauncher.databinding.ActivityMainBinding;

public class HomeStateRenderer {

    private final ActivityMainBinding binding;

    public HomeStateRenderer(ActivityMainBinding binding) {
        this.binding = binding;
    }

    public void render(HomeUiState homeUiState) {
        // 氢OS风格使用统一的浅色背景，不再根据模式切换
        // 如果需要深色/浅色主题切换，可以在这里添加逻辑
        if (binding.rootContainer != null) {
            binding.rootContainer.setBackgroundResource(R.color.background_light_start);
        }
        
        // 新布局中没有这些元素，暂时注释掉
        // 未来可以扩展为动态更新右侧卡片内容
    }
}