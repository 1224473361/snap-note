# HushNote · Whisper Notes

> Press to talk, automatic transcription, local playback. A minimal Android app built for family records.

[简体中文](README.md) · [License](LICENSE) · [Docs](docs/en/)

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-A97BFF?style=flat-square)
![License](https://img.shields.io/badge/License-Dual--Use-orange?style=flat-square)

## Features

- Hold the record button to speak; release to save and transcribe online (iFlytek ASR)
- Notes grouped by date, swipe to delete, tap to play
- Family modules: feeding records, meal journal, growth chronicle
- Optional sync to Feishu (Lark) Bitable with a read-only PC web page
- Check and download the latest version from GitHub Releases
- Light/dark themes, font and size options, **English/Chinese UI** (Chinese by default)

## Licensing

HushNote uses a **dual-use license**:

| Use case | Free? |
|----------|-------|
| Personal and family use | Yes |
| Fork, modify, publish modified source | Yes (keep the copyright notice) |
| Share APKs with friends for free, quote code in tutorials | Yes |
| Use by a company, organization or legal entity (including internal tools) | Requires a commercial license |
| Any revenue-generating use | Requires a commercial license |

This is **not** an OSI open-source license. See [LICENSE](LICENSE) for full terms. For a commercial license, email **1224473361@qq.com** or contact the maintainer via a repository issue.

## Quick start

### Android app

Requires JDK 17+ and Android Studio (or Android SDK).

```bash
cd android
cp local.properties.example local.properties
# Edit local.properties: set sdk.dir. iFlytek keys are optional (can be entered in-app)
./gradlew assembleDebug
```

Output: `android/app/build/outputs/apk/debug/`. For release builds, generate your own signing key — see [signing notes](android/keystore/README.md).

### PC read-only page

```bash
cd pc
cp .env.example .env       # fill in Feishu config if you need sync
npm install
npm run dev
```

## Documentation

| Doc | Description |
|-----|-------------|
| [Architecture](docs/en/方案说明.md) | Product and architecture design |
| [iFlytek ASR setup](docs/en/讯飞ASR配置.md) | Speech recognition key setup |
| [Feishu sync](docs/en/飞书同步配置.md) | Bitable sync configuration |
| [APK build](docs/en/APK打包说明.md) | Build and signing |
| [i18n guide](docs/i18n.md) | Language switching and translation guide |

## Project layout

```text
hush-note/
├── android/     Android Compose app (main project)
├── pc/          Vue3 + TypeScript read-only page
├── docs/        Bilingual docs (zh/ and en/)
├── LICENSE      Dual-use license (English)
├── LICENSE-CN.md Chinese license reference
└── NOTICE       Third-party component notices
```

## Contributing

Issues and pull requests are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md). Please read the [Code of Conduct](CODE_OF_CONDUCT.md) first.

## Security

Never commit keys, signing files or personal information. Report vulnerabilities privately as described in [SECURITY.md](SECURITY.md).
