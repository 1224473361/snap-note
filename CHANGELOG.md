# 变更日志 / Changelog

本项目遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 约定。

## [Unreleased]

### Added
- 双用途授权协议：个人/家庭免费，商业使用需授权
- 中英双语切换基础设施（默认中文，可在设置页切换）
- GitHub Releases 更新检查，替代原 Gitee 私有仓库令牌机制
- 开源治理文档：贡献指南、行为公约、安全策略、CI 与 Issue 模板

### Changed
- 所有第三方密钥改为仅从本地配置注入，仓库不再包含任何真实凭据
- 文档拆分为 `docs/zh/` 与 `docs/en/` 中英双语结构

### Removed
- 签名私钥、明文密码与 AI 工具内部记忆文件（不再进入版本控制）

## 说明

首次开源发布前的准备版本。历史闭源开发记录不随仓库发布。
