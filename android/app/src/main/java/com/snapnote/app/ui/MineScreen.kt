package com.snapnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.snapnote.app.R
import com.snapnote.app.model.AppFontPreset
import com.snapnote.app.model.AppFontSize
import com.snapnote.app.model.HomeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(
    homeMode: HomeMode,
    onBack: () -> Unit,
    onHomeModeSelected: (HomeMode) -> Unit,
    onOpenFeishuHub: () -> Unit,
    onOpenDataHub: () -> Unit,
    onOpenIflytekHub: () -> Unit,
    onOpenGithubReleaseUpdate: () -> Unit,
    onOpenFeedingConfig: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    feishuSyncEnabled: Boolean,
    iflytekConfigured: Boolean,
    feedingPhraseCount: Int,
    appFontPreset: AppFontPreset = AppFontPreset.APP,
    appFontSize: AppFontSize = AppFontSize.MEDIUM
) {
    var showHomeModeSheet by remember { mutableStateOf(false) }

    SubScreenScaffold(
        title = stringResource(R.string.mine_title),
        onBack = onBack,
        actions = {
            IconButton(onClick = { showHomeModeSheet = true }) {
                Icon(
                    homeModeIcon(homeMode),
                    contentDescription = stringResource(R.string.switch_home_module),
                    tint = AppColors.AccentRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            if (homeMode == HomeMode.FEEDING || homeMode == HomeMode.CHRONICLE) {
                MineGroupEntry(
                    icon = Icons.Outlined.Search,
                    title = stringResource(R.string.mine_search_records),
                    subtitle = stringResource(R.string.mine_search_records_subtitle),
                    onClick = onOpenSearch
                )
                HorizontalDivider(color = AppColors.Divider)
            }
            if (homeMode == HomeMode.FEEDING) {
                MineGroupEntry(
                    icon = Icons.Outlined.Settings,
                    title = stringResource(R.string.mine_feeding_config),
                    subtitle = if (feedingPhraseCount > 0) {
                        stringResource(R.string.mine_feeding_config_configured, feedingPhraseCount)
                    } else {
                        stringResource(R.string.mine_feeding_config_unconfigured)
                    },
                    onClick = onOpenFeedingConfig
                )
                HorizontalDivider(color = AppColors.Divider)
            }
            MineGroupEntry(
                icon = Icons.Outlined.CloudSync,
                title = stringResource(R.string.mine_feishu),
                subtitle = if (feishuSyncEnabled) {
                    stringResource(R.string.mine_feishu_enabled)
                } else {
                    stringResource(R.string.mine_feishu_disabled)
                },
                onClick = onOpenFeishuHub
            )
            HorizontalDivider(color = AppColors.Divider)
            MineGroupEntry(
                icon = Icons.Outlined.Storage,
                title = stringResource(R.string.mine_data),
                subtitle = stringResource(R.string.mine_data_subtitle),
                onClick = onOpenDataHub
            )
            HorizontalDivider(color = AppColors.Divider)
            MineGroupEntry(
                icon = Icons.Default.Mic,
                title = stringResource(R.string.mine_iflytek),
                subtitle = if (iflytekConfigured) {
                    stringResource(R.string.mine_iflytek_enabled)
                } else {
                    stringResource(R.string.mine_iflytek_disabled)
                },
                onClick = onOpenIflytekHub
            )
            HorizontalDivider(color = AppColors.Divider)
            MineGroupEntry(
                icon = Icons.Outlined.SystemUpdate,
                title = stringResource(R.string.mine_update),
                subtitle = stringResource(R.string.mine_update_subtitle),
                onClick = onOpenGithubReleaseUpdate
            )
            HorizontalDivider(color = AppColors.Divider)
            MineGroupEntry(
                icon = Icons.Outlined.Tune,
                title = stringResource(R.string.settings_title),
                subtitle = stringResource(
                    R.string.mine_system_settings_subtitle,
                    appFontPreset.label,
                    appFontSize.label
                ),
                onClick = onOpenSystemSettings
            )
        }
    }

    HomeModePickerSheet(
        visible = showHomeModeSheet,
        homeMode = homeMode,
        onDismiss = { showHomeModeSheet = false },
        onHomeModeSelected = onHomeModeSelected
    )
}

@Composable
private fun MineGroupEntry(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    MineMenuItem(
        icon = { Icon(icon, contentDescription = null, tint = AppColors.AccentRed) },
        title = title,
        subtitle = subtitle,
        onClick = onClick
    )
}

@Composable
internal fun MineMenuItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardBackground)
            .clickableWithPressScale(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(title, color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                color = AppColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
