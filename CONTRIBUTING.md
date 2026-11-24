# 贡献指南 / Contributing

感谢你考虑为 SnapNote 做贡献。

## 贡献前须知

- 本项目采用**双用途授权**：个人使用免费，商业使用需授权。贡献代码即表示你同意以该协议发布你的贡献。
- 请勿在 Issue、PR 或任何提交中包含密钥、令牌、签名文件、真实个人数据或本机绝对路径。
- 所有交流请使用中文或英文，保持友善与尊重。

## 报告问题

用 Issue 模板提交。一个好问题包含：

1. 复现步骤
2. 期望行为 vs 实际行为
3. 设备系统版本、App 版本
4. 相关日志（**请先脱敏**）

## 提交代码

1. Fork 仓库并创建功能分支：`git checkout -b feat/your-feature`
2. 保持改动聚焦；不要顺手重构无关代码或升级依赖
3. 提交信息遵循 Conventional Commits，例如 `feat(asr): 支持自定义采样率`
4. 本地验证后再开 PR：

```bash
cd android && ./gradlew testDebugUnitTest compileDebugKotlin --no-daemon
cd ../pc && npm ci && npm run build
```

5. PR 描述说明改了什么、为什么、如何验证

## 开发约定

- **UI 配色**：以 `android/app/src/main/java/com/snapnote/app/ui/AppColors.kt` 为唯一来源，同时适配深色模式，不要自定义十六进制色值。
- **新增界面文案**：必须使用字符串资源（`res/values/strings.xml` + `values-en/strings.xml`），不要硬编码中文，见 [i18n 说明](docs/i18n.md)。
- **密钥**：一律通过 `local.properties`（Android）或 `.env`（PC）注入，对应 `.example` 文件只放占位符。

## 署名

贡献者将被列入 `NOTICE` 或发布说明（如你希望署名，请在 PR 中说明）。
