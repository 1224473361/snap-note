package com.hushnote.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.model.ChronicleGroup
import com.hushnote.app.model.ChronicleItem
import com.hushnote.app.model.FeedingGroup
import com.hushnote.app.model.FeedingItem
import com.hushnote.app.model.HomeMode
import com.hushnote.app.model.MealGroup
import com.hushnote.app.model.MealItem
import com.hushnote.app.model.MemoGroup
import com.hushnote.app.model.MemoItem
import com.hushnote.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    homeMode: HomeMode,
    selectedDateKey: String?,
    keyword: String,
    results: List<MemoGroup>,
    mealResults: List<MealGroup>,
    feedingResults: List<FeedingGroup>,
    chronicleResults: List<ChronicleGroup>,
    playingId: String,
    pausedId: String,
    onBack: () -> Unit,
    onDateSelected: (String?) -> Unit,
    onKeywordChange: (String) -> Unit,
    onPlayToggle: (String, String) -> Unit,
    onDelete: (String, String) -> Unit,
    onEdit: (MemoItem) -> Unit,
    onMealClick: (MealItem) -> Unit,
    onFeedingClick: (FeedingItem) -> Unit,
    onChronicleClick: (ChronicleItem) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateKey?.let { DateUtils.dateKeyToMillis(it) }
    )
    val title = when (homeMode) {
        HomeMode.MEAL -> "搜索饭记"
        HomeMode.FEEDING -> "搜索育儿记"
        HomeMode.CHRONICLE -> "搜索大事记"
        HomeMode.NOTE -> "搜索"
    }
    val placeholder = when (homeMode) {
        HomeMode.MEAL -> "菜品名或备注关键词"
        HomeMode.FEEDING -> "描述或奶量"
        HomeMode.CHRONICLE -> "标题、正文或数值"
        HomeMode.NOTE -> "输入关键词，模糊匹配"
    }

    SubScreenScaffold(title = title, onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("选择日期", color = AppColors.TextSecondary, fontSize = 13.sp)
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
                            text = selectedDateKey ?: "不限",
                            color = AppColors.TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                                .clickable { showDatePicker = true }
                        )
                        if (selectedDateKey != null) {
                            TextButton(onClick = { onDateSelected(null) }) {
                                Text("不限", color = AppColors.TextSecondary, fontSize = 14.sp)
                            }
                        }
                        Text(
                            text = "更换",
                            color = AppColors.AccentRed,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clickable { showDatePicker = true }
                        )
                    }

                    HorizontalDivider(
                        color = AppColors.Divider,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    Text("搜索文字", color = AppColors.TextSecondary, fontSize = 13.sp)
                    OutlinedTextField(
                        value = keyword,
                        onValueChange = onKeywordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        placeholder = {
                            Text(placeholder, color = AppColors.TextSecondary)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.TextSecondary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            when (homeMode) {
                HomeMode.MEAL -> {
                    if (mealResults.isEmpty()) {
                        Text(
                            text = if (keyword.isBlank() && selectedDateKey == null) "暂无饭记" else "无匹配饭记",
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            mealResults.forEach { group ->
                                item(key = "mh-${group.dateKey}") {
                                    Text(
                                        text = group.dateKey,
                                        color = AppColors.TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(group.items, key = { it.id }) { meal ->
                                    MealListItem(item = meal, onClick = { onMealClick(meal) })
                                }
                            }
                        }
                    }
                }
                HomeMode.CHRONICLE -> {
                    if (chronicleResults.isEmpty()) {
                        Text(
                            text = if (keyword.isBlank() && selectedDateKey == null) "暂无记录" else "无匹配记录",
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chronicleResults.forEach { group ->
                                item(key = "ch-${group.dateKey}") {
                                    Text(
                                        text = group.dateKey,
                                        color = AppColors.TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(group.items, key = { it.id }) { item ->
                                    ChronicleListItem(item = item, onClick = { onChronicleClick(item) })
                                }
                            }
                        }
                    }
                }
                HomeMode.FEEDING -> {
                    if (feedingResults.isEmpty()) {
                        Text(
                            text = if (keyword.isBlank() && selectedDateKey == null) "暂无记录" else "无匹配记录",
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            feedingResults.forEach { group ->
                                item(key = "fh-${group.dateKey}") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = group.dateKey,
                                            color = AppColors.TextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (group.totalAmountMl > 0) {
                                            Text(
                                                text = "共 ${group.totalAmountMl} ml",
                                                color = AppColors.AccentRed,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                                items(group.items, key = { it.id }) { feeding ->
                                    FeedingListItem(item = feeding, onClick = { onFeedingClick(feeding) })
                                }
                            }
                        }
                    }
                }
                HomeMode.NOTE -> {
                    MemoGroupList(
                        groups = results,
                        playingId = playingId,
                        pausedId = pausedId,
                        onPlayToggle = onPlayToggle,
                        modifier = Modifier.weight(1f),
                        emptyText = if (keyword.isBlank() && selectedDateKey == null) {
                            "暂无记录"
                        } else {
                            "无匹配记录"
                        },
                        onDelete = onDelete,
                        onEdit = onEdit
                    )
                }
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
                    Text("确定", color = AppColors.AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消", color = AppColors.TextSecondary)
                }
            }
        )
    }
}
