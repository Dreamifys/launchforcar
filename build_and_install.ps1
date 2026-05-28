# Car Launcher 一键编译和安装脚本
# 使用方法: 右键 -> 使用PowerShell运行
# 
# 功能:
# 1. 自动设置Java环境
# 2. 自动确认SDK配置
# 3. 编译Debug APK
# 4. 查找APK文件
# 5. 自动安装到MuMu模拟器

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Car Launcher 编译 & 安装" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

# ============ 步骤 1: 设置环境 ============
Write-Host "[1/5] 配置环境..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\jdk17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
Write-Host "   Java: $env:JAVA_HOME" -ForegroundColor Green

# ============ 步骤 2: 确认SDK ============
Write-Host ""
Write-Host "[2/5] 配置SDK..." -ForegroundColor Yellow
$LocalProps = Join-Path $ProjectRoot "local.properties"
if (-not (Test-Path $LocalProps)) {
    "sdk.dir=C\:\\Users\\Administrator\\AppData\\Local\\Temp\\my-android-sdk" | Out-File -FilePath $LocalProps -Encoding ASCII
}
Write-Host "   SDK配置已就绪" -ForegroundColor Green

# ============ 步骤 3: 编译APK ============
Write-Host ""
Write-Host "[3/5] 正在编译..." -ForegroundColor Yellow
$GradleCmd = Join-Path $ProjectRoot "gradlew.bat"
& $GradleCmd assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ 编译失败！" -ForegroundColor Red
    pause
    exit 1
}
Write-Host "   ✅ 编译成功！" -ForegroundColor Green

# ============ 步骤 4: 查找APK ============
Write-Host ""
Write-Host "[4/5] 查找APK..." -ForegroundColor Yellow
$ApkPath = Get-ChildItem -Path (Join-Path $ProjectRoot "app\build\outputs\apk\debug") -Filter "*.apk" | Select-Object -First 1 -ExpandProperty FullName
if (-not $ApkPath) {
    Write-Host "❌ 未找到APK！" -ForegroundColor Red
    pause
    exit 1
}
Write-Host "   APK: $ApkPath" -ForegroundColor Green

# ============ 步骤 5: 安装到MuMu ============
Write-Host ""
Write-Host "[5/5] 安装到MuMu..." -ForegroundColor Yellow

# 查找MuMu adb
$MuMuPaths = @(
    "C:\Program Files\Netease\MuMu\nx_device\12.0\shell\adb.exe",
    "C:\Program Files\Netease\MuMu\nx_main\adb.exe",
    "C:\Program Files\Netease\MuMuPlayer-12.0\shell\adb.exe",
    "C:\Program Files (x86)\Netease\MuMuPlayer-12.0\shell\adb.exe"
)
$AdbPath = $null
foreach ($Path in $MuMuPaths) {
    if (Test-Path $Path) {
        $AdbPath = $Path
        break
    }
}

if (-not $AdbPath) {
    Write-Host "⚠️  未找到MuMu adb" -ForegroundColor Yellow
    Write-Host "   APK位置: $ApkPath" -ForegroundColor White
    pause
    exit 0
}
Write-Host "   MuMu adb: $AdbPath" -ForegroundColor Green

# 连接MuMu
$Ports = @(7555, 7505, 7525, 7535, 7545, 7565)
$TargetDevice = $null
foreach ($Port in $Ports) {
    & $AdbPath connect "127.0.0.1:$Port" | Out-Null
    $Devices = & $AdbPath devices
    if ($Devices -match "127\.0\.0\.1:$Port") {
        $TargetDevice = "127.0.0.1:$Port"
        Write-Host "   已连接: $TargetDevice" -ForegroundColor Green
        break
    }
}

if (-not $TargetDevice) {
    Write-Host "⚠️  无法连接MuMu，请确保模拟器已启动" -ForegroundColor Yellow
    Write-Host "   APK位置: $ApkPath" -ForegroundColor White
    pause
    exit 0
}

# 安装APK
Write-Host "   正在安装..." -ForegroundColor Gray
& $AdbPath -s $TargetDevice install -r $ApkPath

# ============ 完成 ============
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ 全部完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host " APK已安装到MuMu模拟器！" -ForegroundColor White
Write-Host ""
pause
