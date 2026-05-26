package com.launchforcar.carlauncher.ui.ai;

import com.launchforcar.carlauncher.ui.main.HomeUiState;

public class AiCommandResult {

    private final HomeUiState homeUiState;
    private final String latestAction;
    private final String previousAction;
    private final String preferenceAction;

    public AiCommandResult(HomeUiState homeUiState, String latestAction, String previousAction, String preferenceAction) {
        this.homeUiState = homeUiState;
        this.latestAction = latestAction;
        this.previousAction = previousAction;
        this.preferenceAction = preferenceAction;
    }

    public HomeUiState getHomeUiState() {
        return homeUiState;
    }

    public String getLatestAction() {
        return latestAction;
    }

    public String getPreviousAction() {
        return previousAction;
    }

    public String getPreferenceAction() {
        return preferenceAction;
    }
}