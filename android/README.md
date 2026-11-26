# HushNote Android 客户端

HushNote 的 Android 端应用，使用 Kotlin 与 Jetpack Compose 构建。面向家庭场景提供语音备忘、喂养记录、辅食/饭记与成长纪事等功能，并支持可选的飞书多维表格同步与 PC 只读页查看。

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 1.9.24 |
| UI | Jetpack Compose（Compose BOM 2024.06.00、Material 3） |
| 构建 | Gradle 8.7、Android Gradle Plugin 8.5.2 |
| SDK | compileSdk / targetSdk 34，minSdk 24 |
| 网络与数据 | OkHttp 4.12、Gson 2.11、Kotlin Coroutines |
| 本地安全 | AndroidX Security Crypto（凭据加密存储） |
| 外部服务 | 讯飞语音听写与星火大模型、飞书多维表格、GitHub Releases |

## 目录结构

```text
android/
├── app/
│   ├── src/main/java/com/hushnote/app/    # 主源码（Kotlin）
│   ├── src/main/res/                      # 资源（字符串、主题、图标等）
│   ├── src/test/java/com/hushnote/app/    # 单元测试
│   └── build.gradle.kts                   # 应用级构建配置
├── docs/                                  # 工程文档（如 APK 打包说明）
├── gradle/wrapper/                        # Gradle Wrapper
├── keystore/                              # 签名说明（不含私钥）
├── build.gradle.kts                       # 项目级插件版本
├── gradle.properties                      # Gradle 运行参数
├── local.properties.example               # 本地配置示例
├── gradlew / gradlew.bat                  # 构建入口
└── settings.gradle.kts                    # 项目设置
```

主源码按职责划分：

| 目录 | 职责 |
|------|------|
| `asr` | 语音识别（讯飞听写） |
| `audio` | 录音、编码与播放 |
| `data` | 数据仓库、备份与同步元数据 |
| `domain` | 业务处理服务 |
| `feishu` | 飞书多维表格同步 |
| `llm` | 星火大模型转写与整理 |
| `model` | 数据模型与常量 |
| `ui` | Compose 界面与主题 |
| `update` | GitHub Releases 版本检查与安装 |
| `util` | 日期、导出、图片等工具类 |
| `vision` | 识图能力 |
| `widget` | 桌面录音小组件 |

## 环境要求

| 配置项 | 要求 |
|--------|------|
| JDK | 17 或更高版本 |
| Android Studio | Ladybug 或更高版本 |
| Android SDK | API 34 |

## 快速开始

1. 配置本地属性（在 `android/` 目录下执行）：

```powershell
copy local.properties.example local.properties
```

编辑 `local.properties` 设置 `sdk.dir`；讯飞密钥为可选项，也可在应用「我的 → 讯飞语音」中填写。该文件已被 Git 忽略，请勿提交。

2. 构建 Debug 包：

```powershell
.\gradlew.bat assembleDebug
```

产物位于 `android/app/build/outputs/apk/debug/`。Release 包需先配置签名密钥，详见 [APK 打包说明](docs/APK打包说明.md)。

3. 运行单元测试：

```powershell
.\gradlew.bat testDebugUnitTest
```

## 相关文档

- [APK 打包说明](docs/APK打包说明.md) — 构建、产物路径、签名与安装排查
- [发布签名说明](keystore/README.md) — 密钥库生成与保管要求
- [仓库根目录 README](../README.md) — 项目总览与快速开始
