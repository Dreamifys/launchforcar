package com.launchforcar.carlauncher.ui.host;

import android.widget.FrameLayout;

public interface EmbeddedAppStageHost {

    boolean isSystemEmbeddingSupported();

    String getBackendName();

    String getCapabilitySummary();

    void attach(FrameLayout container, String stageName, String appLabel, String packageName);
}