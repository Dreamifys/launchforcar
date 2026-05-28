不用装 Android Studio，我给你一套极简、纯命令行、M1 原生可用的方案，10 分钟就能跑起来车机模拟器。

一、最终目标
只用 VS Code + Java
→ 运行一个 Android Automotive（车机）ARM64 模拟器
→ 直接安装、运行、调试你的车机桌面 APP

二、你只需要安装 1 个工具：命令行 SDK
不用装 Android Studio！
1. 下载「命令行 Android SDK」（M1 版）
官网直接下载，很小：
👉 https://developer.android.com/studio#command-line-tools-only
下载后解压，得到文件夹：
cmdline-tools
2. 放到一个固定目录（建议）
plaintext
~/Library/Android/
最终结构：
plaintext
~/Library/Android/cmdline-tools/

三、3 条命令安装车机模拟器（复制粘贴）
打开 终端，依次执行：
1. 进入 SDK 工具目录
bash
运行
cd ~/Library/Android/cmdline-tools/bin
2. 同意协议
bash
运行
./sdkmanager --sdk_root=~/Library/Android --licenses
一路输入 y 同意。
3. 安装车机系统 + 模拟器（核心！）
bash
运行
./sdkmanager --sdk_root=~/Library/Android \
"platform-tools" \
"emulator" \
"system-images/android-33/android-automotive-arm64-v8a"
这会安装：
ADB（装 APP 用）
车机模拟器（Android 13 ARM64，完美匹配 8155）

四、1 条命令创建车机模拟器
bash
运行
./avdmanager create avd \
-n Car8155 \
-k "system-images;android-33;android-automotive;arm64-v8a" \
-d "automotive_1080p"
名称：Car8155（你后面启动用这个名字）

五、启动车机模拟器（最关键）
bash
运行
cd ~/Library/Android/emulator
./emulator -avd Car8155
成功效果：
直接弹出 安卓车机桌面（AAOS）
和 8155 车机界面几乎一样！

六、你用 VS Code 怎么调试？
1. 把 APP 编译成 apk
你的 Java 项目最终会生成：
app.apk
2. 用 ADB 安装到模拟器
bash
运行
cd ~/Library/Android/platform-tools
./adb install 你的apk路径.apk
安装完 直接在车机模拟器里打开。
3. 看日志（调试用）
bash
运行
./adb logcat
VS Code 里能直接看到日志。

七、M1 特别注意（必看）
必须用 android-automotive-arm64-v8a（M1 原生，不卡）
不要下 x86 镜像（巨卡、无法用）
这个模拟器就是 8155 车机的标准 AAOS 系统
开发车机桌面 APP 100% 够用

八、我可以给你一键启动脚本
你以后想启动模拟器，只需要双击一个文件就行，不用敲命令。

九、APK安装失败的架构问题（重要经验）

9.1 错误信息
如果安装APK时遇到以下错误：
plaintext
INSTALL_FAILED_NO_MATCHING_ABIS: Failed to extract native libraries, res=-113
这说明APK中的本地库（Native Libraries）与模拟器的CPU架构不匹配。

9.2 架构对应关系
M1/M2 Mac的Android模拟器支持情况：
表格
模拟器架构 | M1 Mac能否运行 | 原因
ARM64 (arm64-v8a) | ✅ 可以 | 原生支持
ARM32 (armeabi-v7a) | ❌ 不行 | QEMU2不支持
x86_64 | ❌ 不行 | 架构不匹配

重要：M1 Mac只能运行ARM64架构的模拟器！

9.3 Android Automotive镜像限制
Google官方Android Automotive系统镜像只有两种架构：
arm64-v8a（ARM64位）- ✅ 可用
x86_64（x86_64位）- ❌ M1 Mac不支持

Google从未发布过armeabi-v7a（ARM32位）的Android Automotive镜像！

9.4 真实车机 vs 模拟器的区别
真实车机（如高通8155）可以运行32位ARM应用，因为：
系统配置了32位兼容层（libhoudini或类似ARM翻译层）
系统属性允许32位应用安装

但Android Automotive模拟器默认不包含这个兼容层！

9.5 APK架构检查方法
用以下命令检查APK支持的架构：
bash
unzip -l 你的apk路径.apk | grep "^lib/" | awk '{print $4}' | cut -d/ -f2 | sort -u

9.6 解决方案
如果遇到INSTALL_FAILED_NO_MATCHING_ABIS错误：
寻找支持ARM64-v8a架构的APK版本
联系应用官方请求提供Android Automotive或ARM64版本
使用真实车机设备测试
在Intel/AMD的Windows电脑上运行x86_64模拟器

9.7 关于高德地图车机版
高德地图车机版APK（9.1.87等版本）通常只包含armeabi-v7a（ARM32位）架构
无法在M1 Mac的Android Automotive ARM64模拟器上安装
需要寻找支持arm64-v8a的版本，或在真车上测试

总结（超级简化版）
下载命令行 SDK
3 条命令安装车机镜像
1 条命令启动车机模拟器
adb install 装上你的 APK
在 VS Code 里开发 + 调试
