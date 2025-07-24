# libopus-Android
基于官方 Opus 库封装，实现了在 Android 设备上的音频录制与 Opus 编解码功能，支持高效压缩与解压，适用于语音通信、语音存储等场景。

# 使用

参考 DemoActivity.java 示例使用方式，可编解码无头的 PCM 录音文件

# 编译

提供 Cmake 和 make 两种编译方式

1、直接使用 gradlew assembleDebug 编译libopus 模块，生成 aar 使用

2、找到 Android.mk 目录，使用 ndk-build 命令直接编译生成 so 使用

# 修改

libopus/src/main/java/com/book/studio/opus/OpusBridge.java

libopus/src/main/cpp/libopus/opus_jni.cpp

自定义编解码
