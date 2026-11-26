package com.hushnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.R
import com.hushnote.app.model.HomeMode

fun homeModeIcon(mode: HomeMode): ImageVector = when (mode) {
    HomeMode.FEEDING -> Icons.Outlined.ChildCare
    HomeMode.CHRONICLE -> Icons.Outlined.Timeline
    HomeMode.MEAL -> Icons.Outlined.Restaurant
    HomeMode.NOTE -> Icons.Default.Mic
}

@Composable
fun homeModeTitle(mode: HomeMode): String = when (mode) {
    HomeMode.NOTE -> stringResource(R.string.home_mode_note)
    HomeMode.MEAL -> stringResource(R.string.home_mode_meal)
    HomeMode.FEEDING -> stringResource(R.string.home_mode_feeding)
    HomeMode.CHRONICLE -> stringResource(R.string.home_mode_chronicle)
}

@Composable
fun HomeModePickerSheet(
    visible: Boolean,
    homeMode: HomeMode,
    onDismiss: () -> Unit,
    onHomeModeSelected: (HomeMode) -> Unit
) {
    if (!visible) return
    HushNoteAppSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.home_module_picker),
            color = AppColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        HomeModeSheetOption(
            icon = Icons.Outlined.ChildCare,
            title = stringResource(R.string.home_mode_feeding),
            selected = homeMode == HomeMode.FEEDING,
            onClick = {
                onHomeModeSelected(HomeMode.FEEDING)
                onDismiss()
            }
        )
        HorizontalDivider(color = AppColors.Divider)
        HomeModeSheetOption(
            icon = Icons.Outlined.Timeline,
            title = stringResource(R.string.home_mode_chronicle),
            selected = homeMode == HomeMode.CHRONICLE,
            onClick = {
                onHomeModeSelected(HomeMode.CHRONICLE)
                onDismiss()
            }
        )
        HorizontalDivider(color = AppColors.Divider)
        HomeModeSheetOption(
            icon = Icons.Outlined.Restaurant,
            title = stringResource(R.string.home_mode_meal),
            selected = homeMode == HomeMode.MEAL,
            onClick = {
                onHomeModeSelected(HomeMode.MEAL)
                onDismiss()
            }
        )
        HorizontalDivider(color = AppColors.Divider)
        HomeModeSheetOption(
            icon = Icons.Default.Mic,
            title = stringResource(R.string.home_mode_note),
            selected = homeMode == HomeMode.NOTE,
            onClick = {
                onHomeModeSelected(HomeMode.NOTE)
                onDismiss()
            }
        )
        HorizontalDivider(
            color = AppColors.Divider,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun HomeModeSheetOption(
    icon: ImageVector,
    title: String,
    selected: Boolean,
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
        Icon(icon, contentDescription = null, tint = AppColors.AccentRed)
        Text(
            text = title,
            color = AppColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = stringResource(R.string.current_module),
                tint = AppColors.AccentRed,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
