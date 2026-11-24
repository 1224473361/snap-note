package com.snapnote.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapnote.app.model.MemoGroup
import com.snapnote.app.model.MemoItem

@Composable
fun MemoGroupList(
    groups: List<MemoGroup>,
    playingId: String,
    pausedId: String,
    onPlayToggle: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    emptyText: String = "暂无记录",
    onDelete: ((String, String) -> Unit)? = null,
    onEdit: ((MemoItem) -> Unit)? = null
) {
    if (groups.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyText, color = AppColors.TextSecondary, fontSize = 15.sp)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groups.forEach { group ->
            item(key = "header-${group.dateKey}") {
                Text(
                    text = group.dateKey,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            items(group.items, key = { it.id }) { item ->
                if (onDelete != null) {
                    SwipeDeleteMemoItem(
                        item = item,
                        isPlaying = playingId == item.id,
                        isPaused = pausedId == item.id,
                        onPlayToggle = onPlayToggle,
                        onDelete = onDelete,
                        onEdit = onEdit
                    )
                } else {
                    MemoListItem(
                        item = item,
                        isPlaying = playingId == item.id,
                        isPaused = pausedId == item.id,
                        onPlayToggle = onPlayToggle,
                        onEdit = onEdit
                    )
                }
            }
        }
    }
}
