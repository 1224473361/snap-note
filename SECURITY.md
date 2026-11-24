# 安全策略 / Security Policy

## 支持的版本

目前仅对 GitHub Releases 的最新发行版提供安全修复支持。

## 报告漏洞

**请勿通过公开 Issue 报告安全漏洞。**

请通过私有渠道提交报告：使用 GitHub Security Advisory，或发邮件至 **1224473361@qq.com**。**请勿在公开 Issue 中披露漏洞细节。**报告请包含：

- 漏洞类型与影响范围
- 复现步骤或概念验证
- 受影响版本
- 建议的修复方案（如有）

我们会在确认后尽快回应并协调修复与披露时间线。

## 已知敏感面（贡献者须知）

本项目的以下配置涉及第三方密钥或凭据，**绝不允许提交到 Git**：

| 文件 | 内容 | 正确做法 |
|------|------|---------|
| `android/local.properties` | 讯飞 APPID / APIKey / APISecret、SDK 路径 | 从 `local.properties.example` 复制，本地填写，已被 `.gitignore` 忽略 |
| `android/keystore/*.jks`、`signing.properties` | 发布签名私钥与明文密码 | 自行生成，仅本地保存，见 [签名说明](android/keystore/README.md) |
| `pc/.env` | 飞书 App ID / Secret / Bitable Token | 从 `.env.example` 复制，本地填写，已被 `.gitignore` 忽略 |
| 应用内飞书令牌 | 用户运行时输入 | 仅存于设备的加密 SharedPreferences，不会写入仓库 |

**如果你的密钥曾经进入过 Git 历史**：立即在对应服务商控制台轮换/作废该密钥（讯飞控制台重新生成 Secret、飞书重建应用凭据、重新生成签名 keystore），仅从工作区删除文件是不够的。参见 [密钥轮换清单](docs/zh/安全与密钥轮换.md)。

GitHub Releases 更新功能使用公开 API，不需要任何令牌。
