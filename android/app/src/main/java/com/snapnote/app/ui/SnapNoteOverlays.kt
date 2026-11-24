package com.snapnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.snapnote.app.R

private val SnapNoteDialogShape = RoundedCornerShape(20.dp)

@Composable
fun snapNoteDialogGradientBrush(): Brush = Brush.verticalGradient(
    colors = listOf(
        AppColors.FeedingStatsGradientTop,
        AppColors.FeedingStatsGradientBottom
    )
)

@Composable
fun SnapNoteDialogContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SnapNoteDialogShape)
            .background(snapNoteDialogGradientBrush())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        content = content
    )
}

@Composable
fun SnapNoteConfirmDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String = stringResource(R.string.cancel),
    confirmDestructive: Boolean = true,
    confirmEnabled: Boolean = true
) {
    SnapNoteAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = {
            Text(
                text = message,
                color = AppColors.TextSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            SnapNoteDialogActionButton(
                text = confirmText,
                accent = confirmDestructive,
                onClick = onConfirm,
                enabled = confirmEnabled
            )
        },
        dismissButton = {
            SnapNoteDialogActionButton(
                text = dismissText,
                accent = false,
                onClick = onDismissRequest
            )
        }
    )
}

@Composable
fun SnapNoteAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    text: (@Composable () -> Unit)? = null
) {
    val overlay = rememberSnapNoteOverlayTheme()
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ProvideSnapNoteOverlayTheme(overlay) {
            SnapNoteDialogContainer(
                modifier = Modifier.padding(horizontal = 28.dp)
            ) {
                Text(
                    text = title,
                    color = AppColors.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (text != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    text()
                }
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}

@Composable
fun SnapNoteDialogActionButton(
    text: String,
    accent: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    TextButton(onClick = onClick, enabled = enabled) {
        Text(
            text = text,
            color = if (accent) AppColors.AccentRed else AppColors.TextSecondary,
            fontSize = 16.sp,
            fontWeight = if (accent) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun SnapNoteAppSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LocalSnapNoteColors.current
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.scrim)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismissRequest
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(palette.pageBackground)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(palette.textSecondary.copy(alpha = 0.45f))
                )
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapNoteDatePickerDialog(
    state: DatePickerState,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit
) {
    val overlay = rememberSnapNoteOverlayTheme()
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ProvideSnapNoteOverlayTheme(overlay) {
            SnapNoteDialogContainer(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                    DatePicker(
                        state = state,
                        colors = appDatePickerColors(),
                        title = {
                            Text(
                                text = stringResource(R.string.select_date),
                                color = AppColors.TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                            )
                        }
                    )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    confirmButton()
                }
            }
        }
    }
}
