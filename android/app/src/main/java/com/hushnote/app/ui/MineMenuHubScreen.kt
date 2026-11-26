package com.hushnote.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class MineHubMenuItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
fun MineMenuHubScreen(
    title: String,
    onBack: () -> Unit,
    items: List<MineHubMenuItem>
) {
    SubScreenScaffold(title = title, onBack = onBack) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                MineMenuItem(
                    icon = { HubIcon(item.icon) },
                    title = item.title,
                    subtitle = item.subtitle,
                    onClick = item.onClick
                )
                if (index < items.lastIndex) {
                    HorizontalDivider(color = AppColors.Divider)
                }
            }
        }
    }
}

@Composable
internal fun HubIcon(icon: ImageVector) {
    androidx.compose.material3.Icon(icon, contentDescription = null, tint = AppColors.AccentRed)
}
