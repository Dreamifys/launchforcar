package com.launchforcar.carlauncher.ui.host;

import android.content.Context;

public final class EmbeddedAppStageHostFactory {

    private EmbeddedAppStageHostFactory() {
    }

    public static EmbeddedAppStageHost create(Context context) {
        boolean taskViewPresent = hasClass("com.android.wm.shell.taskview.TaskView")
                || hasClass("android.app.ActivityView");
        boolean organizerPresent = hasClass("com.android.wm.shell.ShellTaskOrganizer")
                || hasClass("android.window.TaskOrganizer");

        if (taskViewPresent || organizerPresent) {
            return new ReflectionTaskViewEmbeddedAppStageHost(context, taskViewPresent, organizerPresent);
        }
        return new PlaceholderEmbeddedAppStageHost(context);
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}