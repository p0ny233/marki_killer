
模拟器

```shell
adb connect 模拟器桥接IP

adb shell pm uninstall com.example.clientformarki


```


真机

```shell
adb connect 192.168.5.9

adb push app/build/outputs/apk/release/app-release-unsigned.apk /sdcard/marki_xp.apk

adb push clientformarki/build/outputs/apk/release/clientformarki-release-unsigned.apk /sdcard/
```

# Marki Killer
一个用于修改「水印相机」App 时间信息的 Android 工具

## 📖 项目简介
Marki Killer 是一个基于 Xposed 框架 的 Android 模块，通过 Hook 技术拦截并修改「水印相机」App 中与时间相关的系统调用，从而实现自定义水印时间显示的功能。

项目主要包含两个模块：

| 模块             |  说明  |
|----------------| -- |
| app            |   Xposed Hook 模块，包含核心 Hook 逻辑  |
| clientformarki |   配套客户端应用，提供交互界面和配置管理  |

📦 构建与安装
环境要求
Android Studio Panda 2 | 2025.3.2

JDK 11+

Android SDK

## 构建步骤

克隆仓库

```shell
git -c http.proxy=http://192.168.5.9:7890 -c https.proxy=http://192.168.5.9:7890 clone https://github.com/p0ny233/marki_killer.git
```

使用 Android Studio 打开项目，同步 Gradle 依赖。

构建 Debug APK：

```shell
gradlew.bat assembleDebug
```

```shell
adb connect 192.168.5.9

adb push app/build/outputs/apk/release/app-release-unsigned.apk /sdcard/marki_xp.apk

adb push clientformarki/build/outputs/apk/release/clientformarki-release-unsigned.apk /sdcard/
```

⚠️ 免责声明
本项目仅供学习和研究使用，请勿用于非法用途。

使用本项目修改第三方 App 可能违反其服务条款，请自行承担风险。

开发者不对因使用本项目造成的任何损失负责。