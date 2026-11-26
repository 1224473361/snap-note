package com.hushnote.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.model.DeletedChronicleItem
import com.hushnote.app.model.DeletedFeedingItem
import com.hushnote.app.model.ChronicleType
import com.hushnote.app.util.ChronicleFormat
import com.hushnote.app.model.DeletedMealItem
import com.hushnote.app.model.DeletedMemoItem
import com.hushnote.app.model.HomeMode
import com.hushnote.app.util.DateUtils

@Composable
fun RecycleBinScreen(
    homeMode: HomeMode,
    items: List<DeletedMemoItem>,
    mealItems: List<DeletedMealItem>,
    feedingItems: List<DeletedFeedingItem>,
    chronicleItems: List<DeletedChronicleItem>,
    onBack: () -> Unit,
    onRestore: (String) -> Unit,
    onDeletePermanently: (String) -> Unit
) {
    SubScreenScaffold(title = "回收站", onBack = onBack) {
        when (homeMode) {
            HomeMode.MEAL -> {
                if (mealItems.isEmpty()) {
                    EmptyTrash()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(mealItems, key = { index, deleted ->
                            trashRowKey(deleted.item.id, deleted.deletedAt, index)
                        }) { _, deleted ->
                            MealTrashListItem(
                                deleted = deleted,
                                onRestore = { onRestore(deleted.item.id) },
                                onDeletePermanently = { onDeletePermanently(deleted.item.id) }
                            )
                        }
                    }
                }
            }
            HomeMode.FEEDING -> {
                if (feedingItems.isEmpty()) {
                    EmptyTrash()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(feedingItems, key = { index, deleted ->
                            trashRowKey(deleted.item.id, deleted.deletedAt, index)
                        }) { _, deleted ->
                            FeedingTrashListItem(
                                deleted = deleted,
                                onRestore = { onRestore(deleted.item.id) },
                                onDeletePermanently = { onDeletePermanently(deleted.item.id) }
                            )
                        }
                    }
                }
            }
            HomeMode.CHRONICLE -> {
                if (chronicleItems.isEmpty()) {
                    EmptyTrash()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(chronicleItems, key = { index, deleted ->
                            trashRowKey(deleted.item.id, deleted.deletedAt, index)
                        }) { _, deleted ->
                            ChronicleTrashListItem(
                                deleted = deleted,
                                onRestore = { onRestore(deleted.item.id) },
                                onDeletePermanently = { onDeletePermanently(deleted.item.id) }
                            )
                        }
                    }
                }
            }
            HomeMode.NOTE -> {
                if (items.isEmpty()) {
                    EmptyTrash()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(items, key = { index, deleted ->
                            trashRowKey(deleted.item.id, deleted.deletedAt, index)
                        }) { _, deleted ->
                            TrashListItem(
                                deleted = deleted,
                                onRestore = { onRestore(deleted.item.id) },
                                onDeletePermanently = { onDeletePermanently(deleted.item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun trashRowKey(id: String, deletedAt: Long, index: Int): String =
    "$id-$deletedAt-$index"

@Composable
private fun EmptyTrash() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("回收站为空", color = AppColors.TextSecondary, fontSize = 15.sp)
    }
}

@Composable
private fun TrashListItem(
    deleted: DeletedMemoItem,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val item = deleted.item
    val duration = item.durationSeconds.takeIf { it > 0 }
        ?: DateUtils.audioDurationSeconds(item.audioPath)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = item.content,
                color = AppColors.TextPrimary,
                fontSize = 15.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "录音 ${DateUtils.formatTime(item.timestamp)} · ${DateUtils.formatDuration(duration)} · 删除于 ${DateUtils.formatTime(deleted.deletedAt)}",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRestore) {
                    Text("恢复", color = AppColors.AccentRed)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onDeletePermanently) {
                    Text("彻底删除", color = AppColors.ErrorRed)
                }
            }
        }
    }
}

@Composable
private fun FeedingTrashListItem(
    deleted: DeletedFeedingItem,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val item = deleted.item
    val summary = buildString {
        if (item.amountMl != null) append("${item.amountMl}ml ")
        append(item.note.ifBlank { "（无描述）" })
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = summary,
                color = AppColors.TextPrimary,
                fontSize = 15.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.dateKey} ${DateUtils.formatTime(item.timestamp)} · 删除于 ${DateUtils.formatTime(deleted.deletedAt)}",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRestore) {
                    Text("恢复", color = AppColors.AccentRed)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onDeletePermanently) {
                    Text("彻底删除", color = AppColors.ErrorRed)
                }
            }
        }
    }
}

@Composable
private fun ChronicleTrashListItem(
    deleted: DeletedChronicleItem,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val item = deleted.item
    val summary = when (item.type) {
        ChronicleType.MILESTONE -> item.title.ifBlank { item.body }.ifBlank { "（里程碑）" }
        ChronicleType.WEIGHT, ChronicleType.HEIGHT ->
            ChronicleFormat.displayValue(item.type, item.value) ?: ChronicleFormat.typeLabel(item.type)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${ChronicleFormat.typeLabel(item.type)} · $summary",
                color = AppColors.TextPrimary,
                fontSize = 15.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.dateKey} ${DateUtils.formatTime(item.timestamp)} · 删除于 ${DateUtils.formatTime(deleted.deletedAt)}",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRestore) {
                    Text("恢复", color = AppColors.AccentRed)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onDeletePermanently) {
                    Text("彻底删除", color = AppColors.ErrorRed)
                }
            }
        }
    }
}

@Composable
private fun MealTrashListItem(
    deleted: DeletedMealItem,
    onRestore: () -> Unit,
    onDeletePermanently: () -> Unit
) {
    val item = deleted.item
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = item.dishes.joinToString("、").ifBlank { "（无菜品）" },
                color = AppColors.TextPrimary,
                fontSize = 15.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.dateKey} ${DateUtils.formatTime(item.timestamp)} · 删除于 ${DateUtils.formatTime(deleted.deletedAt)}",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRestore) {
                    Text("恢复", color = AppColors.AccentRed)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onDeletePermanently) {
                    Text("彻底删除", color = AppColors.ErrorRed)
                }
            }
        }
    }
}
