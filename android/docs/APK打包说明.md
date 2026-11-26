# HushNote Android APK 打包说明

| 项目 | 说明 |
|------|------|
| 适用工程 | `android/`（HushNote Android 客户端） |
| 构建方式 | Android Studio 或 Gradle Wrapper |
| 编译环境 | JDK 17 及以上、Android SDK API 34 |
| 产物目录 | `android/app/build/outputs/apk/`（详见第 4 节） |

## 1. 概述

本文档说明 HushNote Android 工程的 APK 构建方法、产物位置、签名配置与安装步骤。

工程根目录为 `android/`，请在 Android Studio 中打开该目录，而非仓库根目录。除特别说明，本文命令行示例均以 `android/` 为当前工作目录。

构建产物的默认输出目录为：

```text
android/app/build/outputs/apk/
```

其中 Debug 与 Release 变体分别存放于 `debug/` 与 `release/` 子目录。完整文件名规则见第 4 节。

---

## 2. 环境要求

| 配置项 | 要求 |
|--------|------|
| Android Studio | Ladybug 或更高版本（支持 AGP 8.5 及以上） |
| JDK | 17 或更高版本；不建议使用 JDK 8 执行 Gradle |
| Android SDK | API 34 |
| 本地配置文件 | `android/local.properties` |

### 2.1 配置 local.properties

```powershell
cd android
copy local.properties.example local.properties
```

配置示例：

```properties
sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk

IFLYTEK_APP_ID=你的APPID
IFLYTEK_API_KEY=你的APIKey
IFLYTEK_API_SECRET=你的APISecret
```

| 配置项 | 说明 |
|--------|------|
| `sdk.dir` | Android SDK 安装路径。Android Studio 首次 Sync 时通常自动写入。 |
| `java.home` | 可选。仅命令行 `gradlew.bat` 使用，见 2.2 节。 |
| `IFLYTEK_APP_ID` 等三项 | 讯飞语音转写密钥，构建时写入 APK。未配置时应用可安装运行，但在线转写不可用。 |

> **安全提示**：`local.properties` 包含本机路径与密钥，已由 `.gitignore` 忽略，不得提交至 Git。

### 2.2 指定 Gradle 使用的 JDK

若系统 `JAVA_HOME` 指向 JDK 8，可在 `android/local.properties` 中追加 JDK 17/21 路径，无需修改系统环境变量：

```properties
java.home=D\:\\install\\java\\jdk-21.0.9
```

该配置由 `android/gradlew.bat` 读取并仅对当前 Gradle 进程生效，不会修改系统 `JAVA_HOME`。Unix 平台的 `gradlew` 脚本不支持此覆盖方式，需通过系统 `JAVA_HOME` 指定 JDK。

Android Studio 用户可在 **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** 中选择 JDK 17 或 21。

---

## 3. 构建执行

### 3.1 Android Studio

1. 打开 `android/` 工程，等待 Gradle Sync 完成。
2. 选择菜单 **Build → Build Bundle(s) / APK(s) → Build APK(s)**。
3. 在构建变体中选择 `debug` 或 `release`。
4. 构建完成后，可通过通知中的 **Locate** 直接定位产物，或按第 4 节给出的目录查找。

### 3.2 命令行

构建 Debug 包：

```powershell
cd android
.\gradlew.bat assembleDebug
```

构建 Release 包（需先完成第 6 节的签名配置）：

```powershell
cd android
.\gradlew.bat assembleRelease
```

构建成功后，终端输出 `BUILD SUCCESSFUL`。产物不会自动复制到其他目录，需从 4.1 节所列路径获取。

---

## 4. 构建产物

### 4.1 产物目录与文件名

产物统一输出至 `android/app/build/outputs/apk/`，按构建变体分目录存放：

| 构建变体 | 产物目录（相对仓库根目录） | 文件名格式 | 签名要求 |
|----------|---------------------------|-----------|----------|
| Debug | `android/app/build/outputs/apk/debug/` | `hush-note-<yyyy-MM-dd-HHmm>-debug.apk` | 无需签名配置 |
| Release | `android/app/build/outputs/apk/release/` | `hush-note-<yyyy-MM-dd-HHmm>.apk` | 需配置 `signing.properties` |

其中 `<yyyy-MM-dd-HHmm>` 为构建日期与时刻，24 小时制，例如 `2026-11-26-1930`。

示例：

```text
android/app/build/outputs/apk/debug/hush-note-2026-11-26-1930-debug.apk
android/app/build/outputs/apk/release/hush-note-2026-11-26-1930.apk
```

> 文件名由 `android/app/build.gradle.kts` 中的 `output.outputFileName` 显式指定；若修改了该配置，实际文件名以构建输出为准。未配置签名时 Release 产物为未签名 APK，文件名可能与上表存在差异，且无法直接安装。

### 4.2 定位与清理产物

列出已生成的 APK：

```powershell
cd android
Get-ChildItem app\build\outputs\apk -Recurse -Filter *.apk
```

执行清理会删除 `build/` 下的全部构建产物，包括已生成的 APK：

```powershell
cd android
.\gradlew.bat clean
```

---

## 5. 安装与验证

1. 将 APK 传输至设备（数据线、即时通信工具或网盘）。
2. 在设备的文件管理器中打开 APK 并按提示安装。
3. 若系统提示禁止安装未知来源应用，请在设置中为当前文件管理器或浏览器授予相应权限。
4. 首次启动时授予**麦克风**与**网络**权限，否则无法录音与转写。

HarmonyOS 4.2 等 Android 兼容设备可直接安装 APK，无需额外适配。

安装后如转写结果显示「（未识别）」，请核对讯飞密钥配置及设备网络连接。

---

## 6. 发布签名

### 6.1 当前状态

本仓库**不包含**签名私钥与签名配置。`android/keystore/` 目录仅提供说明文档，`*.jks` 与 `signing.properties` 均由 `.gitignore` 忽略。

`android/app/build.gradle.kts` 仅在 `android/keystore/signing.properties` 存在且 `storeFile` 非空时启用 Release 签名。因此，首次构建 Release 包前必须自行生成密钥库并完成配置，否则产物为未签名 APK，无法安装或升级。

### 6.2 生成密钥库

```powershell
cd android\keystore
keytool -genkeypair -v   -keystore hush-note-release.jks     -alias hush-note   -keyalg RSA -keysize 2048 -validity 100000   -dname "CN=snap-note, OU=Dev, O=YourName, L=Tangshan, ST=Hebei, C=CN"
```

按提示设置两次密码，并记录别名（示例为 `hush-note`）。

### 6.3 创建签名配置

在 `android/keystore/` 下创建 `signing.properties`（已被 Git 忽略）：

```properties
storeFile=hush-note-release.jks
storePassword=你的store密码
keyAlias=hush-note
keyPassword=你的key密码
```

配置完成后执行 `.\gradlew.bat assembleRelease` 即可产出已签名 APK，路径见 4.1 节。

> 签名密钥丢失后，用户无法通过覆盖安装升级应用。请离线备份 `.jks` 文件与密码，不要将其提交至 Git。

---

## 7. 版本管理

版本号定义在 `android/app/build.gradle.kts` 的 `defaultConfig` 中：

| 字段 | 含义 | 示例 |
|------|------|------|
| `versionCode` | 整数，每次发布必须递增 | `1`、`2` |
| `versionName` | 展示给用户的版本号 | `1.0.0` |

发布新版本前须同步更新上述两项。

---

## 8. 故障排查

### 8.1 安装提示「该安装包未包含任何证书」

设备在安装时提示：

```text
安装失败
失败原因：该安装包未包含任何证书。
处理建议：请使用官方版本进行安装。
```

**原因**：所安装的 APK 未经签名。本仓库不含签名密钥，未配置 `android/keystore/signing.properties` 时执行 `assembleRelease` 会产出未签名 APK，Android 与 HarmonyOS 均会拒绝安装。提示中的「请使用官方版本」为系统通用文案，不适用于自行构建的包；此问题与设备兼容性无关。

**处理方式**：

| 场景 | 操作 |
|------|------|
| 本机自测 | 改用 Debug 包：`cd android` 后执行 `.\gradlew.bat assembleDebug`，从 `android/app/build/outputs/apk/debug/` 取包（见 4.1 节） |
| 对外分发或长期使用 | 按第 6 节生成密钥库并创建 `signing.properties`，重新执行 `.\gradlew.bat assembleRelease`，从 `android/app/build/outputs/apk/release/` 取已签名包 |

如需确认某个 APK 是否已签名，使用 Android SDK 的 `apksigner`：

```powershell
cd android
& "$env:ANDROID_SDK_ROOT\build-tools\<版本>\apksigner.bat" verify --verbose app\build\outputs\apk\release\hush-note-xxx.apk
```

输出 `Verifies` 且 `Verified using v2 scheme (APK Signature Scheme v2): true` 表示签名有效。`<版本>` 按本机 `build-tools` 目录下的实际版本填写。

> 本工程 Release 包默认使用 v2 签名。若使用 `keytool -printcert -jarfile` 验证，可能显示「不是已签名的 jar 文件」，该结果不能作为未签名的判断依据；`keytool` 仅能识别 v1（JAR）签名。

### 8.2 Gradle 内存不足

典型报错：

```text
Could not reserve enough space for 2097152KB object heap
```

`android/gradle.properties` 已配置 `org.gradle.jvmargs=-Xmx1024m`。若仍失败，可将该值调整为 `-Xmx512m` 后重试。

### 8.3 Gradle 使用 JDK 8

典型报错：

```text
compatible with Java 8
```

或 Gradle 仍调用 `jdk1.8.0_xxx\bin\java.exe`。处理方式见 2.2 节。配置修改后建议先停止旧 Daemon 再构建：

```powershell
cd android
.\gradlew.bat --stop
.\gradlew.bat assembleRelease
```

### 8.4 首次构建耗时较长

首次构建需下载 Gradle 依赖，可能耗时数分钟；后续构建会使用缓存。可先在 Android Studio 中完成一次 Sync，确保 SDK 与依赖齐备。

---

## 9. 相关文档

- [仓库根目录 README](../../README.md) — 项目总览与快速开始
- [发布签名说明](../keystore/README.md) — 密钥生成与保管要求
