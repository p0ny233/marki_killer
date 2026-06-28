set "DEBUG=1"
chcp 65001
echo off
if defined DEBUG (
    echo "======================================================================="
    echo "调试阶段"
    @REM xposed模块
    adb shell pm uninstall com.hook
    adb push app/build/outputs/apk/debug/app-debug.apk /sdcard/marki_xp-debug.apk

    @REM clientformarki模块
    adb shell pm uninstall com.example.clientformarki
    adb push clientformarki/build/outputs/apk/debug/clientformarki-debug.apk /sdcard
)else (
    echo "======================================================================="
    echo "发布阶段"
    @REM xposed模块
    adb shell pm uninstall com.hook
    adb push app/build/outputs/apk/release/app-release-unsigned.apk /sdcard/marki_xp-unsigned.apk

    @REM clientformarki模块
    adb shell pm uninstall com.example.clientformarki
    adb push clientformarki/build/outputs/apk/release/clientformarki-release-unsigned.apk /sdcard/clientformarki-release-unsigned.apk

)

