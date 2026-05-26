package com.launchforcar.carlauncher.ui.ai;

import android.text.TextUtils;

import com.launchforcar.carlauncher.ui.main.HomeUiState;

public class AiCommandProcessor {

    public AiCommandResult process(String command, String currentRecentAction) {
        if (TextUtils.isEmpty(command)) {
            return new AiCommandResult(null, "请输入一句有效的桌面指令", currentRecentAction, null);
        }

        String normalizedCommand = command.trim();
        if (normalizedCommand.contains("停车") || normalizedCommand.contains("娱乐") || normalizedCommand.contains("屏保")) {
            return new AiCommandResult(
                    HomeUiState.park(),
                    "已根据指令切换到停车娱乐态",
                    normalizedCommand,
                    "default_mode:park"
            );
        }

        if (normalizedCommand.contains("驾驶") || normalizedCommand.contains("通勤")) {
            return new AiCommandResult(
                    HomeUiState.drive(
                            "去公司",
                            "预计 24 分钟 · 16.2 km · 今日略拥堵",
                            "系统已切换到通勤桌面，展示路线、音乐和最近联系人。"
                    ),
                    "已切换到驾驶通勤桌面",
                    normalizedCommand,
                    "default_mode:drive"
            );
        }

        if (normalizedCommand.contains("回家") || normalizedCommand.contains("导航")) {
            return new AiCommandResult(
                    HomeUiState.drive(
                            "回家",
                            "预计 28 分钟 · 18.4 km · 环路通畅",
                            "已将回家导航卡片置于首页主舞台，并保留音乐推荐。"
                    ),
                    "已将回家导航设为首页主卡片",
                    normalizedCommand,
                    "default_mode:drive"
            );
        }

        if (normalizedCommand.contains("音乐")) {
            return new AiCommandResult(
                    null,
                    "已记录音乐卡片调整意图，下一步可进入编辑页细化布局",
                    normalizedCommand,
                    "music_card_visible:true"
            );
        }

        if (normalizedCommand.contains("隐藏联系人")) {
            return new AiCommandResult(null, "已隐藏联系人卡片", normalizedCommand, "contact_card_visible:false");
        }

        if (normalizedCommand.contains("显示联系人")) {
            return new AiCommandResult(null, "已显示联系人卡片", normalizedCommand, "contact_card_visible:true");
        }

        return new AiCommandResult(
                null,
                "已记录指令，建议下一步进入卡片编辑或 AI 推荐流程",
                normalizedCommand,
                null
        );
    }
}