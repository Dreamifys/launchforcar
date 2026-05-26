package com.launchforcar.carlauncher.ui.host;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.launchforcar.carlauncher.R;

public class PlaceholderEmbeddedAppStageHost implements EmbeddedAppStageHost {

    private final Context context;

    public PlaceholderEmbeddedAppStageHost(Context context) {
        this.context = context;
    }

    @Override
    public boolean isSystemEmbeddingSupported() {
        return false;
    }

    @Override
    public String getBackendName() {
        return "PlaceholderHost";
    }

    @Override
    public String getCapabilitySummary() {
        return "当前是普通 APK 宿主位占位模式，未接入系统级 TaskView。";
    }

    @Override
    public void attach(FrameLayout container, String stageName, String appLabel, String packageName) {
        container.removeAllViews();

        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setGravity(Gravity.CENTER);
        wrapper.setBackgroundResource(R.drawable.bg_embedded_stage_placeholder);

        FrameLayout.LayoutParams wrapperParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        int padding = dpToPx(18);
        wrapper.setPadding(padding, padding, padding, padding);
        wrapper.setLayoutParams(wrapperParams);

        TextView titleView = new TextView(context);
        titleView.setText(TextUtils.isEmpty(appLabel) ? stageName : appLabel);
        titleView.setTextColor(context.getResources().getColor(R.color.text_primary));
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleView.setGravity(Gravity.CENTER);

        TextView detailView = new TextView(context);
        detailView.setText(buildStatusText(stageName, packageName));
        detailView.setTextColor(context.getResources().getColor(R.color.text_secondary));
        detailView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        detailView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        detailParams.topMargin = dpToPx(10);
        detailView.setLayoutParams(detailParams);

        wrapper.addView(titleView);
        wrapper.addView(detailView);
        container.addView(wrapper);
    }

    private String buildStatusText(String stageName, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return stageName + " 暂未选应用\n点击下方按钮选择一个已安装应用\n系统版将用 TaskView 在这里真正承载外部任务";
        }
        return "已配置包名：" + packageName + "\n当前普通 APK 仅显示宿主位\n系统版将用 TaskView 在这里真正承载外部任务";
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        ));
    }
}