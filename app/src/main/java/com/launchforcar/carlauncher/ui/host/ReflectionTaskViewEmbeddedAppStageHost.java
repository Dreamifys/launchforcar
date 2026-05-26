package com.launchforcar.carlauncher.ui.host;

import android.content.Context;

public class ReflectionTaskViewEmbeddedAppStageHost extends PlaceholderEmbeddedAppStageHost {

    private final boolean taskViewClassPresent;
    private final boolean shellTaskOrganizerPresent;

    public ReflectionTaskViewEmbeddedAppStageHost(Context context,
                                                  boolean taskViewClassPresent,
                                                  boolean shellTaskOrganizerPresent) {
        super(context);
        this.taskViewClassPresent = taskViewClassPresent;
        this.shellTaskOrganizerPresent = shellTaskOrganizerPresent;
    }

    @Override
    public boolean isSystemEmbeddingSupported() {
        return taskViewClassPresent && shellTaskOrganizerPresent;
    }

    @Override
    public String getBackendName() {
        return "ReflectionTaskViewHost";
    }

    @Override
    public String getCapabilitySummary() {
        if (isSystemEmbeddingSupported()) {
            return "已探测到 TaskView 相关系统类，后续只差系统权限和实际宿主接线。";
        }
        return "探测到部分系统宿主类，但当前环境还不完整，先保持占位宿主模式。";
    }
}