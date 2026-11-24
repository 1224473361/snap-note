package com.snapnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.snapnote.app.util.DateUtils
import java.util.Calendar
import java.util.TimeZone

/**
 * 育儿记专用：默认当前时间；点「更改」先调时分，日期用今天/昨天/近 7 天，极少情况才进月历。
 */
@Composable
fun FeedingDateTimeField(
    timestamp: Long,
    onTimestampChange: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    FeedingFormTimeRow(
        timeText = "${DateUtils.toDateKey(timestamp)}  ${DateUtils.formatTime(timestamp)}",
        onClick = { showPicker = true },
        emphasizeTime = false,
        showChangeButton = false,
        compact = true
    )

    if (showPicker) {
        FeedingDateTimePickerOverlay(
            timestamp = timestamp,
            onDismiss = { showPicker = false },
            onConfirm = {
                onTimestampChange(it)
                showPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedingDateTimePickerOverlay(
    timestamp: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val overlay = rememberSnapNoteOverlayTheme()
    val initialCal = remember(timestamp) {
        Calendar.getInstance().apply { timeInMillis = timestamp }
    }
    var selectedDateKey by remember(timestamp) {
        mutableStateOf(DateUtils.toDateKey(timestamp))
    }
    val timeState = rememberTimePickerState(
        initialHour = initialCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = initialCal.get(Calendar.MINUTE),
        is24Hour = true
    )
    var showCalendar by remember { mutableStateOf(false) }

    val recentDateKeys = remember {
        (0..6).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            DateUtils.toDateKey(cal.timeInMillis)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ProvideSnapNoteOverlayTheme(overlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Scrim)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        .background(AppColors.PageBackground)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {}
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 20.dp)
                ) {
                    Text(
                        text = "调整时间",
                        color = AppColors.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "先选时分，日期一般用今天或昨天即可",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    TimePicker(
                        state = timeState,
                        colors = appTimePickerColors(),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    FeedingFormLabel(text = "日期")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentDateKeys.forEachIndexed { index, key ->
                            FeedingDateChip(
                                label = when (index) {
                                    0 -> "今天"
                                    1 -> "昨天"
                                    else -> formatShortDateLabel(key)
                                },
                                selected = key == selectedDateKey,
                                onClick = { selectedDateKey = key }
                            )
                        }
                    }
                    TextButton(
                        onClick = { showCalendar = true },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("更多日期…", color = AppColors.AccentRed, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    FeedingPrimaryCapsuleButton(
                        text = "确定",
                        enabled = true,
                        onClick = {
                            onConfirm(
                                DateUtils.combineDateKeyAndTime(
                                    selectedDateKey,
                                    timeState.hour,
                                    timeState.minute
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showCalendar) {
        val utc = TimeZone.getTimeZone("UTC")
        val cal = Calendar.getInstance().apply { timeInMillis = DateUtils.dateKeyToMillis(selectedDateKey) }
        val initialUtc = Calendar.getInstance(utc).apply {
            set(Calendar.YEAR, cal.get(Calendar.YEAR))
            set(Calendar.MONTH, cal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateState = rememberDatePickerState(initialSelectedDateMillis = initialUtc.timeInMillis)
        SnapNoteDatePickerDialog(
            state = dateState,
            onDismissRequest = { showCalendar = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = dateState.selectedDateMillis
                        if (selected != null) {
                            val picked = Calendar.getInstance(utc).apply { timeInMillis = selected }
                            selectedDateKey = String.format(
                                "%04d-%02d-%02d",
                                picked.get(Calendar.YEAR),
                                picked.get(Calendar.MONTH) + 1,
                                picked.get(Calendar.DAY_OF_MONTH)
                            )
                        }
                        showCalendar = false
                    }
                ) {
                    Text("确定", color = AppColors.AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendar = false }) {
                    Text("取消", color = AppColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun FeedingDateChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val bg = if (selected) AppColors.FeedingStatsFooterBg else AppColors.CardBackground
    val borderColor = if (selected) AppColors.AccentRed else AppColors.ChipBorder
    Text(
        text = label,
        color = if (selected) AppColors.AccentRed else AppColors.TextPrimary,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}

private fun formatShortDateLabel(dateKey: String): String {
    val parts = dateKey.split("-")
    if (parts.size != 3) return dateKey
    return "${parts[1].toInt()}/${parts[2].toInt()}"
}
