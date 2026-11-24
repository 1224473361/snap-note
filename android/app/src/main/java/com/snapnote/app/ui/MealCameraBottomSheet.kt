package com.snapnote.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MealCameraBottomSheet(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit
) {
    SnapNoteAppSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "添加饭记",
                color = AppColors.TextPrimary,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SheetAction(
                icon = { Icon(Icons.Outlined.CameraAlt, null, tint = AppColors.AccentRed) },
                title = "拍照",
                onClick = onTakePhoto
            )
            SheetAction(
                icon = { Icon(Icons.Outlined.PhotoLibrary, null, tint = AppColors.AccentRed) },
                title = "从相册选图",
                onClick = onPickGallery
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SheetAction(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = AppColors.TextPrimary, fontSize = 16.sp)
        Spacer(modifier = Modifier.size(0.dp))
    }
}
