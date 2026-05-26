#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APK_PATH="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
PRIVAPP_XML="$ROOT_DIR/system/privapp-permissions-com.launchforcar.carlauncher.xml"
DEVICE_APP_DIR="/system/priv-app/CarAILauncher"
DEVICE_APP_APK="$DEVICE_APP_DIR/CarAILauncher.apk"
DEVICE_PRIVAPP_XML="/system/etc/permissions/privapp-permissions-com.launchforcar.carlauncher.xml"
PKG_NAME="com.launchforcar.carlauncher"

if [[ ! -f "$APK_PATH" ]]; then
  echo "Missing APK at $APK_PATH"
  exit 1
fi

if [[ ! -f "$PRIVAPP_XML" ]]; then
  echo "Missing privapp permission file at $PRIVAPP_XML"
  exit 1
fi

adb root
adb wait-for-device
adb remount
adb shell mkdir -p "$DEVICE_APP_DIR"
adb push "$APK_PATH" "$DEVICE_APP_APK"
adb push "$PRIVAPP_XML" "$DEVICE_PRIVAPP_XML"
adb shell chmod 644 "$DEVICE_APP_APK" "$DEVICE_PRIVAPP_XML"
adb shell pm uninstall --user 0 "$PKG_NAME" >/dev/null 2>&1 || true
adb reboot

echo "Privileged launcher payload deployed. Wait for reboot, then verify with:"
echo "adb shell pm path $PKG_NAME"