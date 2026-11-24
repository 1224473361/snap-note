# 发布签名（Release signing）

本目录**不存放任何真实签名文件**。私钥（`.jks`）与 `signing.properties` 含明文密码，一旦提交到 Git 就无法撤回，请不要放入本目录后提交。

## 为什么需要自己的签名

Android 要求每个安装包都用同一个密钥签名才能升级安装。开源仓库无法公开你的发布私钥，因此**你需要自己生成并保管**，Instructions 如下。

## 1. 生成密钥库

```bash
keytool -genkeypair -v \
  -keystore snap-note-release.jks \
  -alias snap-note \
  -keyalg RSA -keysize 2048 -validity 10000
```

按提示设置**两次密码**（建议不同），并记住 alias（示例为 `snap-note`）。

## 2. 创建本地配置

在本目录创建 `signing.properties`（**已被 .gitignore 忽略**）：

```properties
storeFile=snap-note-release.jks
storePassword=你的store密码
keyAlias=snap-note
keyPassword=你的key密码
```

## 3. 构建

`app/build.gradle.kts` 会读取 `signing.properties` 进行 release 签名。没有该文件时，debug 构建仍可正常进行。

> 签名密钥丢失后，用户将无法通过更新覆盖安装旧版本。请离线备份密钥与密码。
