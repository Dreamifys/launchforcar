# Windows车机模拟器设置脚本 - 原生armeabi-v7a镜像
# 适用于需要完美兼容ARM32应用的场景

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "车机模拟器设置 - 原生ARM32支持" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否有Android SDK
$sdkPath = "$env:LOCALAPPDATA\Android\Sdk"
if (-not (Test-Path $sdkPath)) {
    Write-Host "未找到Android SDK，请先安装Android Studio或命令行工具" -ForegroundColor Red
    Write-Host "下载地址: https://developer.android.com/studio#command-tools" -ForegroundColor Yellow
    exit 1
}

Write-Host "找到Android SDK: $sdkPath" -ForegroundColor Green

# 设置环境变量
$env:ANDROID_HOME = $sdkPath
$env:PATH = "$sdkPath\cmdline-tools\latest\bin;$sdkPath\platform-tools;$sdkPath\emulator;$env:PATH"

Write-Host ""
Write-Host "步骤1: 安装必要的SDK组件" -ForegroundColor Yellow
Write-Host "--------------------------------" -ForegroundColor Yellow

# 安装platform-tools
Write-Host "正在安装 platform-tools..." -ForegroundColor Gray
$sdkmanager --sdk_root="$sdkPath" "platform-tools"

# 安装emulator
Write-Host "正在安装 emulator..." -ForegroundColor Gray
$sdkmanager --sdk_root="$sdkPath" "emulator"

# 安装armeabi-v7a系统镜像（原生32位支持）
Write-Host "正在安装系统镜像 (Android 13, armeabi-v7a)..." -ForegroundColor Gray
$sdkmanager --sdk_root="$sdkPath" "system-images;android-33;default;armeabi-v7a"

Write-Host ""
Write-Host "步骤2: 创建AVD设备" -ForegroundColor Yellow
Write-Host "--------------------------------" -ForegroundColor Yellow

# 创建车机AVD
Write-Host "正在创建车机模拟器 (CarArmV7)..." -ForegroundColor Gray
echo no | avdmanager create avd `
    -n CarArmV7 `
    -k "system-images;android-33;default;armeabi-v7a" `
    -d "pixel_xl"

Write-Host ""
Write-Host "步骤3: 配置车机分辨率" -ForegroundColor Yellow
Write-Host "--------------------------------" -ForegroundColor Yellow

# 修改AVD配置为车机分辨率
$avdPath = "$env:USERPROFILE\.android\avd\CarArmV7.avd\config.ini"
if (Test-Path $avdPath) {
    (Get-Content $avdPath) -replace "hw.lcd.density=.*", "hw.lcd.density=240" | Set-Content $avdPath
    (Get-Content $avdPath) -replace "hw.lcd.height=.*", "hw.lcd.height=1080" | Set-Content $avdPath
    (Get-Content $avdPath) -replace "hw.lcd.width=.*", "hw.lcd.width=1920" | Set-Content $avdPath
    Write-Host "已配置为车机分辨率: 1920x1080, 240dpi" -ForegroundColor Green
}

Write-Host ""
Write-Host "====================================" -ForegroundColor Cyan
Write-Host "设置完成！" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "启动模拟器命令:" -ForegroundColor Yellow
Write-Host "  emulator -avd CarArmV7" -ForegroundColor White
Write-Host ""
Write-Host "安装APK命令:" -ForegroundColor Yellow
Write-Host "  adb install d:\LPOS\9.1.87悬浮共存.apk" -ForegroundColor White
Write-Host ""
Write-Host "注意: armeabi-v7a镜像在Windows上运行速度较慢，" -ForegroundColor Yellow
Write-Host "      建议优先使用setup_windows_armv7_emulator.ps1方案" -ForegroundColor Gray
Write-Host ""
