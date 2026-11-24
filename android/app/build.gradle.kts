import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        f.inputStream().use { load(it) }
    }
}

fun prop(name: String): String = localProps.getProperty(name, "")

val signingProps = Properties().apply {
    val f = rootProject.file("keystore/signing.properties")
    if (f.exists()) {
        f.inputStream().use { load(it) }
    }
}

fun signingProp(name: String): String = signingProps.getProperty(name, "")

android {
    namespace = "com.snapnote.app"
    compileSdk = 34

    // 仅当本地存在 keystore/signing.properties 时才配置 release 签名。
    // 公开仓库不含签名私钥，fork 者需自行生成（见 android/keystore/README.md）。
    val hasReleaseSigning = rootProject.file("keystore/signing.properties").exists() &&
        signingProp("storeFile").isNotBlank()
    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file("keystore/${signingProp("storeFile")}")
                storePassword = signingProp("storePassword")
                keyAlias = signingProp("keyAlias")
                keyPassword = signingProp("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.snapnote.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField("String", "IFLYTEK_APP_ID", "\"${prop("IFLYTEK_APP_ID")}\"")
        buildConfigField("String", "IFLYTEK_API_KEY", "\"${prop("IFLYTEK_API_KEY")}\"")
        buildConfigField("String", "IFLYTEK_API_SECRET", "\"${prop("IFLYTEK_API_SECRET")}\"")

        // GitHub Releases 更新源（公开仓库，无需令牌；fork 者可在 local.properties 覆盖）
        buildConfigField("String", "GITHUB_RELEASE_OWNER", "\"${prop("GITHUB_RELEASE_OWNER")}\"")
        buildConfigField("String", "GITHUB_RELEASE_REPO", "\"${prop("GITHUB_RELEASE_REPO")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    applicationVariants.configureEach {
        val variant = this
        variant.preBuildProvider.configure {
            doFirst {
                val missing = listOf(
                    "IFLYTEK_APP_ID" to prop("IFLYTEK_APP_ID"),
                    "IFLYTEK_API_KEY" to prop("IFLYTEK_API_KEY"),
                    "IFLYTEK_API_SECRET" to prop("IFLYTEK_API_SECRET")
                ).filter { it.second.isBlank() }.map { it.first }
                if (missing.isNotEmpty()) {
                    logger.warn(
                        "local.properties 未配置 ${missing.joinToString()}，" +
                            "本变体 APK 内不会带讯飞密钥；可在 App「我的 → 讯飞语音」中填写。"
                    )
                }
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

@Suppress("DEPRECATION")
android.applicationVariants.configureEach {
    val variant = this
    outputs.configureEach {
        val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
        val stamp = SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.getDefault()).format(Date())
        // 文件名不要含空格，否则 Windows 上 adb install 常报 Broken pipe / UNKNOWN
        output.outputFileName = if (variant.buildType.name == "release") {
            "snap-note-$stamp.apk"
        } else {
            "snap-note-$stamp-${variant.buildType.name}.apk"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
}
