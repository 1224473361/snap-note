package com.hushnote.app.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.hushnote.app.R
import com.hushnote.app.model.AppFontPreset

/**
 * 打包字体：`res/font/zihun_langyuanti.ttf`（字魂 144 号朗圆体）
 */
private val bundledAppFont = Font(R.font.zihun_langyuanti, FontWeight.Normal)

val BundledAppFontFamily: FontFamily = FontFamily(
    bundledAppFont,
    Font(R.font.zihun_langyuanti, FontWeight.Medium),
    Font(R.font.zihun_langyuanti, FontWeight.SemiBold),
    Font(R.font.zihun_langyuanti, FontWeight.Bold)
)

/** @deprecated 使用 [fontFamilyForPreset]；保留别名避免遗漏引用。 */
val CuteFontFamily: FontFamily = BundledAppFontFamily

fun fontFamilyForPreset(preset: AppFontPreset): FontFamily = when (preset) {
    AppFontPreset.SYSTEM -> FontFamily.Default
    AppFontPreset.APP -> BundledAppFontFamily
}
