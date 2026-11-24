package com.snapnote.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.snapnote.app.model.AppLanguage
import com.snapnote.app.model.MemoConstants
import java.util.Locale

/**
 * 应用内语言切换辅助类。
 *
 * AppCompat 提供 [androidx.appcompat.app.AppCompatDelegate.setApplicationLocales]，
 * 但 MainActivity 是 ComponentActivity（不是 AppCompatActivity），在 API 33 以下
 * AppCompat 不会自动替换 Activity 的 Context。因此这里额外用 attachBaseContext
 * 包装 Context，保证 Compose 的 stringResource 读取到正确语言。
 */
object LocaleManager {

    /** 依据保存的语言设置包装 Context；SYSTEM 时保持系统默认。 */
    fun wrapContext(context: Context): Context {
        val raw = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)
            .getString(MemoConstants.PREF_KEY_APP_LANGUAGE, null)
        val language = AppLanguage.fromStored(raw)
        if (language == AppLanguage.SYSTEM || language.localeTag.isEmpty()) return context

        val locale = Locale.forLanguageTag(language.localeTag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList(locale))
        return context.createConfigurationContext(config)
    }
}
