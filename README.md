# Car AI Launcher

面向 Android 4.4+ 车机设备的 AI 桌面项目。

## 当前内容

1. 需求文档：[docs/car-ai-launcher-requirements.md](docs/car-ai-launcher-requirements.md)
2. 线框与页面结构：[docs/car-ai-launcher-wireframes.md](docs/car-ai-launcher-wireframes.md)
3. 视觉设计说明：[docs/car-ai-launcher-visual-spec.md](docs/car-ai-launcher-visual-spec.md)
4. 开发任务拆解：[docs/car-ai-launcher-dev-plan.md](docs/car-ai-launcher-dev-plan.md)
5. 第三方车机应用架构地图：[docs/car-headunit-app-architecture-map.md](docs/car-headunit-app-architecture-map.md)
6. 高保真静态原型：[prototype/index.html](prototype/index.html)
7. Android 工程骨架：[app/build.gradle](app/build.gradle)

## 项目结构

```text
.
├── app/
├── docs/
├── prototype/
├── build.gradle
├── gradle.properties
└── settings.gradle
```

## Android 工程说明

当前已搭建一个 Android 4.4+ 兼容的应用骨架：

1. `minSdk 19`
2. Java + ViewBinding
3. AppCompat + ConstraintLayout + CardView
4. 首屏为车机首页原型界面
5. 已新增应用抽屉页面与基础搜索过滤
6. 已新增 AI 面板输入与简单指令执行
7. 已新增设置页与卡片编辑页骨架
8. 已将首页状态渲染与 AI 指令处理拆分为独立类
9. 已接入 SharedPreferences，支持默认模式、卡片显隐和设置项持久化
10. 已新增左右双应用宿主位预备结构，普通 APK 走占位宿主，系统版预留 TaskView 接入点

## 当前限制

当前环境未安装可用的 Java Runtime 与 Gradle，因此本次未完成本地编译验证。

如需本地构建，请先准备：

1. JDK 11 或更高版本
2. Android SDK
3. Gradle 或 Gradle Wrapper

## 下一步建议

1. 给工程补 Gradle Wrapper
2. 将首页进一步拆成 include 级卡片布局或 RecyclerView 卡片容器
3. 接入更完整的数据模型与场景模式切换
4. 为设置页、编辑页补充更多可编辑配置项

## 系统宿主集成

当前工程已经包含系统级双应用宿主的预备结构，但要真正承载第三方应用，仍需把 APK 部署成模拟器或设备中的 priv-app，并结合系统宿主能力接入。

仓库内已提供：

1. privapp 白名单模板：[system/privapp-permissions-com.launchforcar.carlauncher.xml](system/privapp-permissions-com.launchforcar.carlauncher.xml)
2. 一键部署脚本：[scripts/deploy_privileged_launcher.sh](scripts/deploy_privileged_launcher.sh)

脚本会执行：

1. `adb root`
2. `adb remount`
3. 将 debug APK 推送到 `/system/priv-app/CarAILauncher/CarAILauncher.apk`
4. 将 privapp 权限白名单推送到 `/system/etc/permissions/`
5. 移除当前 `/data/app` 版本并重启模拟器
