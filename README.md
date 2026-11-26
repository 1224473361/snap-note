# HushNote · 轻声备忘录

> 按住说话、自动转写、本地列表回听。一个为家庭记录而生的极简 Android App。

[English](README.en.md) · [许可证](LICENSE-CN.md) · [文档](docs/zh/)

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-A97BFF?style=flat-square)
![License](https://img.shields.io/badge/License-Dual--Use-orange?style=flat-square)

## 功能

- 按住录音钮说话，松手保存并在线转写（讯飞语音听写）
- 记录按日期分组、左滑删除、单条播放
- 喂养记录、辅食/饭记、成长纪事等家庭模块
- 可选同步到飞书多维表格，PC 端只读查看
- 从 GitHub Releases 检查并下载最新版本
- 支持浅色/深色主题、字号与字体切换、**中英双语**（默认中文）

## 授权

本项目采用**双用途授权**：

| 使用场景 | 是否免费 |
|---------|---------|
| 个人及家庭自用 | ✅ 免费 |
| Fork、二次开发、公开修改版源码 | ✅ 免费（保留版权声明） |
| 免费分享 APK 给朋友、写教程引用代码 | ✅ 免费 |
| 公司/组织/法人实体使用（含内部工具） | ❌ 需商业授权 |
| 以盈利为目的的任何使用 | ❌ 需商业授权 |

这不是 OSI 开源许可证。完整条款见 [LICENSE](LICENSE) / [中文对照](LICENSE-CN.md)。商业授权请发邮件至 **1224473361@qq.com**，或通过仓库 Issue 联系维护者。

## 快速开始

### Android App

需要 JDK 17+ 与 Android Studio（或 Android SDK）。

```bash
cd android
cp local.properties.example local.properties
# 编辑 local.properties，设置 sdk.dir；讯飞密钥可选（也可在 App 内填写）
./gradlew assembleDebug
```

产物：`android/app/build/outputs/apk/debug/`。首次安装建议使用 debug 包；发布包需用你自己的签名密钥，见 [签名说明](android/keystore/README.md)。

### PC 只读页

```bash
cd pc
cp .env.example .env       # 如需飞书同步，填写配置
npm install
npm run dev
```

## 文档

| 文档 | 说明 |
|------|------|
| [方案说明](docs/zh/方案说明.md) | 产品与架构设计 |
| [讯飞 ASR 配置](docs/zh/讯飞ASR配置.md) | 语音转写密钥申请与配置 |
| [飞书同步配置](docs/zh/飞书同步配置.md) | 多维表格同步设置 |
| [APK 打包说明](docs/zh/APK打包说明.md) | 构建与签名 |
| [i18n 使用说明](docs/i18n.md) | 中英语言切换与翻译指引 |

## 目录结构

```text
hush-note/
├── android/     Android Compose 应用（主工程）
├── pc/          Vue3 + TypeScript 只读页
├── docs/        中英双语文档（zh/ 与 en/）
├── LICENSE      双用途授权协议（英文）
├── LICENSE-CN.md 协议中文对照
└── NOTICE       第三方组件声明
```

## 参与贡献

欢迎 Issue 与 Pull Request，详见 [CONTRIBUTING.md](CONTRIBUTING.md)。参与前请阅读 [行为公约](CODE_OF_CONDUCT.md)。

## 安全

请勿在仓库中提交任何密钥、签名文件或个人信息。发现安全漏洞请按 [SECURITY.md](SECURITY.md) 的方式私下报告。
