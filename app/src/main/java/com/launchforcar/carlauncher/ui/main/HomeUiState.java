package com.launchforcar.carlauncher.ui.main;

public class HomeUiState {

    private final boolean parkMode;
    private final String heroTag;
    private final String heroTitle;
    private final String heroMeta;
    private final String heroDescription;
    private final int primaryActionRes;
    private final int secondaryActionRes;

    public HomeUiState(boolean parkMode,
                       String heroTag,
                       String heroTitle,
                       String heroMeta,
                       String heroDescription,
                       int primaryActionRes,
                       int secondaryActionRes) {
        this.parkMode = parkMode;
        this.heroTag = heroTag;
        this.heroTitle = heroTitle;
        this.heroMeta = heroMeta;
        this.heroDescription = heroDescription;
        this.primaryActionRes = primaryActionRes;
        this.secondaryActionRes = secondaryActionRes;
    }

    public static HomeUiState drive(String heroTitle, String heroMeta, String heroDescription) {
        return new HomeUiState(
                false,
                "主导航卡片",
                heroTitle,
                heroMeta,
                heroDescription,
                com.launchforcar.carlauncher.R.string.start_navigation,
                com.launchforcar.carlauncher.R.string.view_alternative_routes
        );
    }

    public static HomeUiState park() {
        return new HomeUiState(
                true,
                "停车娱乐卡片",
                "停车放松",
                "艺术相框 · 音乐 · 视频 · 最近继续",
                "已切换到停车娱乐态，主舞台可展示艺术屏保、视频推荐或本地媒体内容。",
                com.launchforcar.carlauncher.R.string.open_art_frame,
                com.launchforcar.carlauncher.R.string.open_media_center
        );
    }

    public boolean isParkMode() {
        return parkMode;
    }

    public String getHeroTag() {
        return heroTag;
    }

    public String getHeroTitle() {
        return heroTitle;
    }

    public String getHeroMeta() {
        return heroMeta;
    }

    public String getHeroDescription() {
        return heroDescription;
    }

    public int getPrimaryActionRes() {
        return primaryActionRes;
    }

    public int getSecondaryActionRes() {
        return secondaryActionRes;
    }
}