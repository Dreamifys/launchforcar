# MuMu模拟器APK安装脚本
# 用于将9.1.87悬浮共存.apk安装到MuMu模拟器

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "MuMu模拟器 - APK安装工具" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

$apkPath = "d:\LPOS\9.1.87悬浮共存.apk"

# 检查APK文件
if (-not (Test-Path $apkPath)) {
    Write-Host "❌ 未找到APK文件: $apkPath" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 找到APK: $apkPath" -ForegroundColor Green

# 尝试查找MuMu的adb
Write-Host ""
Write-Host "查找MuMu模拟器..." -ForegroundColor Yellow

$mumuPaths = @(
    "C:\Program Files\Netease\MuMuPlayer-12.0\shell",
    "C:\Program Files (x86)\Netease\MuMuPlayer-12.0\shell",
    "C:\Program Files\Netease\MuMu\shell",
    "C:\Program Files (x86)\Netease\MuMu\shell",
    "$env:USERPROFILE\AppData\Local\Netease\MuMuPlayer-12.0\shell"
)

$foundAdb = $null
foreach ($p in $mumuPaths) {
    $adbPath = Join-Path $p "adb.exe"
    if (Test-Path $adbPath) {
        Write-Host "✅ 找到MuMu adb: $adbPath" -ForegroundColor Green
        $foundAdb = $adbPath
        break
    }
}

if (-not $foundAdb) {
    Write-Host ""
    Write-Host "⚠️  未自动找到MuMu adb，请手动操作:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "步骤1: 启动MuMu模拟器" -ForegroundColor White
    Write-Host "步骤2: 打开MuMu设置 - 其他设置 - 开启adb调试" -ForegroundColor White
    Write-Host "步骤3: 在MuMu中点击 'APK安装' 按钮" -ForegroundColor White
    Write-Host "步骤4: 选择文件: $apkPath" -ForegroundColor White
    Write-Host ""
    Write-Host "或者使用命令:" -ForegroundColor Yellow
    Write-Host "  adb connect 127.0.0.1:7555" -ForegroundColor Gray
    Write-Host "  adb install `"$apkPath`"" -ForegroundColor Gray
    Write-Host ""
    pause
    exit 0
}

# 设置PATH
$env:PATH = "$(Split-Path $foundAdb);$env:PATH"

Write-Host ""
Write-Host "尝试连接MuMu模拟器..." -ForegroundColor Yellow

# 连接MuMu（常用端口：7555, 7505, 7525等）
$ports = @(7555, 7505, 7525, 7535, 7545, 7565)
$connected = $false

foreach ($port in $ports) {
    Write-Host "  尝试端口 $port..." -ForegroundColor Gray
    & $foundAdb connect "127.0.0.1:$port" 2>&1 | Out-Null
    $devices = & $foundAdb devices
    if ($devices -match "127\.0\.0\.1:$port") {
        Write-Host "  ✅ 连接成功: 127.0.0.1:$port" -ForegroundColor Green
        $connected = $true
        break
    }
}

if (-not $connected) {
    Write-Host ""
    Write-Host "❌ 无法连接到MuMu模拟器" -ForegroundColor Red
    Write-Host ""
    Write-Host "请确保:" -ForegroundColor Yellow
    Write-Host "  1. MuMu模拟器已启动" -ForegroundColor White
    Write-Host "  2. MuMu设置中已开启ADB调试" -ForegroundColor White
    Write-Host ""
    pause
    exit 1
}

Write-Host ""
Write-Host "开始安装APK..." -ForegroundColor Yellow
Write-Host "  $apkPath" -ForegroundColor Gray
Write-Host ""

# 安装APK
&amp; $foundAdb install -r $apkPath

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "====================================" -ForegroundColor Green
    Write-Host "✅ APK安装成功！" -ForegroundColor Green
    Write-Host "====================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "====================================" -ForegroundColor Red
    Write-Host "❌ APK安装失败" -ForegroundColor Red
    Write-Host "====================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "尝试通过MuMu界面手动安装APK" -ForegroundColor Yellow
}

Write-Host ""
pause
