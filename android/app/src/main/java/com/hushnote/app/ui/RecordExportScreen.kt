package com.hushnote.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.R
import com.hushnote.app.model.ChronicleItem
import com.hushnote.app.model.ChronicleType
import com.hushnote.app.model.FeedingItem
import com.hushnote.app.util.ChronicleFormat
import com.hushnote.app.model.HomeMode
import com.hushnote.app.model.MealItem
import com.hushnote.app.model.MemoItem
import com.hushnote.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordExportScreen(
    homeMode: HomeMode,
    selectedDateKey: String,
    memos: List<MemoItem>,
    meals: List<MealItem>,
    feedings: List<FeedingItem>,
    chronicles: List<ChronicleItem>,
    onBack: () -> Unit,
    onToggleMode: () -> Unit,
    onDateSelected: (String) -> Unit,
    onCopy: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.dateKeyToMillis(selectedDateKey)
    )

    val modeLabel = when (homeMode) {
        HomeMode.NOTE -> stringResource(R.string.home_mode_note)
        HomeMode.MEAL -> stringResource(R.string.home_mode_meal)
        HomeMode.FEEDING -> stringResource(R.string.home_mode_feeding)
        HomeMode.CHRONICLE -> stringResource(R.string.home_mode_chronicle)
    }
    val hasContent = when (homeMode) {
        HomeMode.NOTE -> memos.isNotEmpty()
        HomeMode.MEAL -> meals.isNotEmpty()
        HomeMode.FEEDING -> feedings.isNotEmpty()
        HomeMode.CHRONICLE -> chronicles.isNotEmpty()
    }
    val totalAmountMl = feedings.sumOf { it.amountMl ?: 0 }

    SubScreenScaffold(title = stringResource(R.string.export_title), onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = modeLabel,
                        color = AppColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                    IconButton(onClick = onToggleMode) {
                        Icon(
                            imageVector = when (homeMode) {
                                HomeMode.NOTE -> Icons.Outlined.Restaurant
                                HomeMode.MEAL -> Icons.Outlined.ChildCare
                                HomeMode.FEEDING -> Icons.Outlined.Timeline
                                HomeMode.CHRONICLE -> Icons.Default.Mic
                            },
                            contentDescription = stringResource(R.string.export_switch_type),
                            tint = AppColors.AccentRed
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.select_date), color = AppColors.TextSecondary, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = AppColors.AccentRed
                        )
                        Text(
                            text = selectedDateKey,
                            color = AppColors.TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        )
                        Text(stringResource(R.string.export_change), color = AppColors.AccentRed, fontSize = 14.sp)
                    }
                }
            }

            if (!hasContent) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.export_no_content, modeLabel), color = AppColors.TextSecondary, fontSize = 15.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedDateKey,
                                color = AppColors.TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            if (homeMode == HomeMode.FEEDING && totalAmountMl > 0) {
                                Text(
                                    text = stringResource(R.string.export_total_amount, totalAmountMl),
                                    color = AppColors.AccentRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    when (homeMode) {
                        HomeMode.NOTE -> items(memos, key = { it.id }) { memo ->
                            ExportPreviewLine(text = "${DateUtils.formatTimeWithSeconds(memo.timestamp)} ${memo.content}")
                        }
                        HomeMode.MEAL -> items(meals, key = { it.id }) { meal ->
                            val dishes = meal.dishes.joinToString("、")
                                .ifBlank { stringResource(R.string.export_dishes_blank) }
                            val line = buildString {
                                append(DateUtils.formatTime(meal.timestamp))
                                append(' ')
                                append(dishes)
                                if (meal.note.isNotBlank()) {
                                    append(' ')
                                    append(meal.note)
                                }
                            }
                            ExportPreviewLine(text = line)
                        }
                        HomeMode.FEEDING -> items(feedings, key = { it.id }) { feeding ->
                            val line = buildString {
                                append(DateUtils.formatTime(feeding.timestamp))
                                if (feeding.amountMl != null) {
                                    append(' ')
                                    append(feeding.amountMl)
                                    append("ml")
                                }
                                if (feeding.note.isNotBlank()) {
                                    append(' ')
                                    append(feeding.note)
                                }
                            }
                            ExportPreviewLine(text = line)
                        }
                        HomeMode.CHRONICLE -> items(chronicles, key = { it.id }) { item ->
                            val line = buildString {
                                append(DateUtils.formatTime(item.timestamp))
                                append(' ')
                                append(ChronicleFormat.typeLabel(item.type))
                                when (item.type) {
                                    ChronicleType.WEIGHT, ChronicleType.HEIGHT -> {
                                        ChronicleFormat.displayValue(item.type, item.value)?.let {
                                            append(' ')
                                            append(it)
                                        }
                                    }
                                    ChronicleType.MILESTONE -> {
                                        if (item.title.isNotBlank()) {
                                            append(' ')
                                            append(item.title)
                                        }
                                    }
                                }
                            }
                            ExportPreviewLine(text = line)
                        }
                    }
                }
            }

            Button(
                onClick = onCopy,
                enabled = hasContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Text(
                    text = stringResource(R.string.export_copy),
                    modifier = Modifier.padding(start = 8.dp),
                    fontSize = 16.sp
                )
            }
        }
    }

    if (showDatePicker) {
        HushNoteDatePickerDialog(
            state = datePickerState,
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(DateUtils.millisToDateKey(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.confirm), color = AppColors.AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel), color = AppColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ExportPreviewLine(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = text,
            color = AppColors.TextPrimary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}
