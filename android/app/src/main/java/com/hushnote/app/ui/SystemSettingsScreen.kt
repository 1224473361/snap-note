package com.hushnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.R
import com.hushnote.app.model.AppFontPreset
import com.hushnote.app.model.AppFontSize
import com.hushnote.app.model.AppLanguage
import com.hushnote.app.model.AppThemeMode
import com.hushnote.app.model.FeedingStatsCardStyle

private enum class SettingsPicker {
    FONT_PRESET,
    FONT_SIZE,
    THEME_MODE,
    LANGUAGE,
    STATS_CARD
}

@Composable
fun SystemSettingsScreen(
    fontPreset: AppFontPreset,
    fontSize: AppFontSize,
    themeMode: AppThemeMode,
    appLanguage: AppLanguage,
    reduceMotion: Boolean,
    showSecondsInListTime: Boolean,
    hapticFeedbackEnabled: Boolean,
    feedingStatsCardStyle: FeedingStatsCardStyle,
    onBack: () -> Unit,
    onFontPresetSelected: (AppFontPreset) -> Unit,
    onFontSizeSelected: (AppFontSize) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onShowSecondsInListTimeChange: (Boolean) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onFeedingStatsCardStyleSelected: (FeedingStatsCardStyle) -> Unit
) {
    var openPicker by remember { mutableStateOf<SettingsPicker?>(null) }

    SubScreenScaffold(title = stringResource(R.string.settings_title), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionTitle(stringResource(R.string.settings_section_display))
            SettingsNavigateRow(
                title = stringResource(R.string.settings_font_source),
                value = fontPreset.label,
                subtitle = fontPreset.subtitle,
                onClick = { openPicker = SettingsPicker.FONT_PRESET }
            )
            HorizontalDivider(color = AppColors.Divider)
            SettingsNavigateRow(
                title = stringResource(R.string.settings_font_size),
                value = fontSize.label,
                subtitle = stringResource(R.string.settings_font_size_approx, fontSize.scaleLabel),
                onClick = { openPicker = SettingsPicker.FONT_SIZE }
            )
            HorizontalDivider(color = AppColors.Divider)
            SettingsNavigateRow(
                title = stringResource(R.string.settings_appearance),
                value = themeMode.label,
                subtitle = stringResource(R.string.settings_appearance_subtitle),
                onClick = { openPicker = SettingsPicker.THEME_MODE }
            )
            HorizontalDivider(color = AppColors.Divider)
            SettingsNavigateRow(
                title = stringResource(R.string.settings_language),
                value = languageLabel(appLanguage),
                subtitle = stringResource(R.string.settings_language_subtitle),
                onClick = { openPicker = SettingsPicker.LANGUAGE }
            )

            HorizontalDivider(color = AppColors.Divider, modifier = Modifier.padding(top = 8.dp))
            SettingsSectionTitle(stringResource(R.string.settings_section_interaction))
            SettingsSwitchRow(
                icon = Icons.Outlined.Vibration,
                title = stringResource(R.string.settings_haptic),
                subtitle = stringResource(R.string.settings_haptic_subtitle),
                checked = hapticFeedbackEnabled,
                onCheckedChange = onHapticFeedbackChange
            )
            HorizontalDivider(color = AppColors.Divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Animation,
                title = stringResource(R.string.settings_reduce_motion),
                subtitle = stringResource(R.string.settings_reduce_motion_subtitle),
                checked = reduceMotion,
                onCheckedChange = onReduceMotionChange
            )
            HorizontalDivider(color = AppColors.Divider)
            SettingsSwitchRow(
                icon = Icons.Outlined.Schedule,
                title = stringResource(R.string.settings_show_seconds),
                subtitle = stringResource(R.string.settings_show_seconds_subtitle),
                checked = showSecondsInListTime,
                onCheckedChange = onShowSecondsInListTimeChange
            )

            HorizontalDivider(color = AppColors.Divider, modifier = Modifier.padding(top = 8.dp))
            SettingsSectionTitle(stringResource(R.string.home_mode_feeding))
            SettingsNavigateRow(
                title = stringResource(R.string.settings_stats_card_style),
                value = feedingStatsCardStyle.label,
                subtitle = feedingStatsCardStyle.subtitle,
                onClick = { openPicker = SettingsPicker.STATS_CARD }
            )
        }
    }

    when (openPicker) {
        SettingsPicker.FONT_PRESET -> SettingsSingleChoiceDialog(
            title = stringResource(R.string.settings_font_source),
            options = AppFontPreset.entries,
            selected = fontPreset,
            optionTitle = { it.label },
            optionSubtitle = { it.subtitle },
            onSelect = onFontPresetSelected,
            onDismiss = { openPicker = null }
        )
        SettingsPicker.FONT_SIZE -> SettingsSingleChoiceDialog(
            title = stringResource(R.string.settings_font_size),
            options = AppFontSize.entries,
            selected = fontSize,
            optionTitle = { it.label },
            optionSubtitle = { stringResource(R.string.settings_font_size_approx, it.scaleLabel) },
            onSelect = onFontSizeSelected,
            onDismiss = { openPicker = null }
        )
        SettingsPicker.THEME_MODE -> SettingsSingleChoiceDialog(
            title = stringResource(R.string.settings_appearance),
            options = AppThemeMode.entries,
            selected = themeMode,
            optionTitle = { it.label },
            onDismiss = { openPicker = null },
            onSelect = onThemeModeSelected
        )
        SettingsPicker.LANGUAGE -> SettingsSingleChoiceDialog(
            title = stringResource(R.string.settings_language),
            options = listOf(AppLanguage.CHINESE, AppLanguage.ENGLISH, AppLanguage.SYSTEM),
            selected = appLanguage,
            optionTitle = { languageLabel(it) },
            onSelect = onLanguageSelected,
            onDismiss = { openPicker = null }
        )
        SettingsPicker.STATS_CARD -> SettingsSingleChoiceDialog(
            title = stringResource(R.string.settings_stats_card_style),
            options = FeedingStatsCardStyle.entries,
            selected = feedingStatsCardStyle,
            optionTitle = { it.label },
            optionSubtitle = { it.subtitle },
            onSelect = onFeedingStatsCardStyleSelected,
            onDismiss = { openPicker = null }
        )
        null -> Unit
    }
}

@Composable
private fun languageLabel(language: AppLanguage): String = when (language) {
    AppLanguage.CHINESE -> stringResource(R.string.language_chinese)
    AppLanguage.ENGLISH -> stringResource(R.string.language_english)
    AppLanguage.SYSTEM -> stringResource(R.string.language_system)
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        color = AppColors.TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun SettingsNavigateRow(
    title: String,
    value: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Text(
            text = value,
            color = AppColors.AccentRed,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(end = 4.dp)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextSecondary
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.AccentRed)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Text(title, color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                color = AppColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = AppColors.CardBackground,
                checkedTrackColor = AppColors.AccentRed,
                uncheckedThumbColor = AppColors.CardBackground,
                uncheckedTrackColor = AppColors.Divider
            )
        )
    }
}
