package com.hushnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hushnote.app.util.DateUtils
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun appDatePickerColors() = DatePickerDefaults.colors(
    containerColor = AppColors.CardBackground,
    titleContentColor = AppColors.TextPrimary,
    headlineContentColor = AppColors.TextPrimary,
    weekdayContentColor = AppColors.TextSecondary,
    subheadContentColor = AppColors.TextSecondary,
    yearContentColor = AppColors.TextPrimary,
    currentYearContentColor = AppColors.AccentRed,
    selectedYearContainerColor = AppColors.AccentRed,
    selectedYearContentColor = AppColors.CardBackground,
    selectedDayContainerColor = AppColors.AccentRed,
    selectedDayContentColor = AppColors.CardBackground,
    todayContentColor = AppColors.AccentRed,
    dayContentColor = AppColors.TextPrimary,
    disabledDayContentColor = AppColors.TextSecondary.copy(alpha = 0.4f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun appTimePickerColors() = TimePickerDefaults.colors(
    clockDialColor = AppColors.SoftFill,
    clockDialSelectedContentColor = AppColors.CardBackground,
    clockDialUnselectedContentColor = AppColors.TextPrimary,
    selectorColor = AppColors.AccentRed,
    containerColor = AppColors.CardBackground,
    periodSelectorBorderColor = AppColors.Divider,
    periodSelectorSelectedContainerColor = AppColors.AccentRed.copy(alpha = 0.2f),
    periodSelectorUnselectedContainerColor = AppColors.SoftFill,
    periodSelectorSelectedContentColor = AppColors.AccentRed,
    periodSelectorUnselectedContentColor = AppColors.TextSecondary,
    timeSelectorSelectedContainerColor = AppColors.AccentRed.copy(alpha = 0.2f),
    timeSelectorUnselectedContainerColor = AppColors.SoftFill,
    timeSelectorSelectedContentColor = AppColors.AccentRed,
    timeSelectorUnselectedContentColor = AppColors.TextPrimary
)

@Composable
fun MealSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = AppColors.TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp
    )
}

@Composable
fun MealSoftField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    val softFill = AppColors.SoftFill
    val placeholderColor = AppColors.Placeholder
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .background(softFill, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        singleLine = singleLine,
        minLines = minLines,
        textStyle = TextStyle(
            color = AppColors.TextPrimary,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        cursorBrush = SolidColor(AppColors.AccentRed),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, color = placeholderColor, fontSize = 15.sp)
                }
                inner()
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealDishEditor(
    dishes: List<String>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onDishesChange: (List<String>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (dishes.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dishes.forEachIndexed { index, dish ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppColors.ChipBackground)
                            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = dish,
                            onValueChange = { value ->
                                onDishesChange(dishes.toMutableList().also { it[index] = value })
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = AppColors.TextPrimary,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(AppColors.AccentRed),
                            modifier = Modifier.width(
                                (dish.length * 14 + 24).coerceIn(48, 220).dp
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onDishesChange(dishes.filterIndexed { i, _ -> i != index })
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "删除",
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.SoftFill, RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                singleLine = true,
                textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 15.sp),
                cursorBrush = SolidColor(AppColors.AccentRed),
                decorationBox = { inner ->
                    Box {
                        if (draft.isEmpty()) {
                            Text("添加菜品", color = AppColors.Placeholder, fontSize = 15.sp)
                        }
                        inner()
                    }
                }
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppColors.AccentRed.copy(alpha = 0.1f))
                    .clickable {
                        val name = draft.trim()
                        if (name.isNotEmpty()) {
                            onDishesChange(dishes + name)
                            onDraftChange("")
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加",
                    tint = AppColors.AccentRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDateTimePickerRow(
    timestamp: Long,
    onTimestampChange: (Long) -> Unit
) {
    val overlay = rememberHushNoteOverlayTheme()
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    val calendar = remember(timestamp) {
        Calendar.getInstance().apply { timeInMillis = timestamp }
    }

    FeedingFormTimeRow(
        timeText = "${DateUtils.toDateKey(timestamp)}  ${DateUtils.formatTime(timestamp)}",
        onClick = { showDate = true },
        emphasizeTime = false
    )

    if (showDate) {
        val utc = TimeZone.getTimeZone("UTC")
        val initialUtc = Calendar.getInstance(utc).apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dateState = rememberDatePickerState(initialSelectedDateMillis = initialUtc.timeInMillis)
        HushNoteDatePickerDialog(
            state = dateState,
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = dateState.selectedDateMillis
                        if (selected != null) {
                            val picked = Calendar.getInstance(utc).apply { timeInMillis = selected }
                            val next = Calendar.getInstance().apply {
                                timeInMillis = timestamp
                                set(Calendar.YEAR, picked.get(Calendar.YEAR))
                                set(Calendar.MONTH, picked.get(Calendar.MONTH))
                                set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))
                            }
                            onTimestampChange(next.timeInMillis)
                        }
                        showDate = false
                        showTime = true
                    }
                ) { Text("下一步", color = AppColors.AccentRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) {
                    Text("取消", color = AppColors.TextSecondary)
                }
            }
        )
    }

    if (showTime) {
        val timeState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTime = false }) {
            ProvideHushNoteOverlayTheme(overlay) {
            Column(
                modifier = Modifier
                    .background(AppColors.CardBackground, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "选择时间",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                TimePicker(state = timeState, colors = appTimePickerColors())
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showTime = false }) {
                        Text("取消", color = AppColors.TextSecondary)
                    }
                    TextButton(
                        onClick = {
                            val next = Calendar.getInstance().apply {
                                timeInMillis = timestamp
                                set(Calendar.HOUR_OF_DAY, timeState.hour)
                                set(Calendar.MINUTE, timeState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onTimestampChange(next.timeInMillis)
                            showTime = false
                        }
                    ) {
                        Text("确定", color = AppColors.AccentRed)
                    }
                }
            }
            }
        }
    }
}

@Composable
fun MealHairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppColors.Divider)
    )
}
