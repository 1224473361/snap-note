package com.hushnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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

@Composable
fun FeedingQuickPhraseScreen(
    phrases: List<String>,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    onUpdate: (index: Int, text: String) -> Unit,
    onDelete: (index: Int) -> Unit
) {
    var dialogMode by remember { mutableStateOf<PhraseDialogMode?>(null) }

    SubScreenScaffold(title = "育儿描述短语", onBack = onBack) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (phrases.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("暂无短语", color = AppColors.TextSecondary, fontSize = 15.sp)
                    Text(
                        "点击右下角 + 添加",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(phrases, key = { index, text -> "$index-$text" }) { index, text ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AppColors.CardBackground, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = text,
                                color = AppColors.TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { dialogMode = PhraseDialogMode.Edit(index, text) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "编辑", tint = AppColors.TextSecondary)
                            }
                            IconButton(onClick = { onDelete(index) }) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = "删除", tint = AppColors.AccentRed)
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { dialogMode = PhraseDialogMode.Add },
                containerColor = AppColors.AccentRed,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加短语", tint = AppColors.CardBackground)
            }
        }
    }

    dialogMode?.let { mode ->
        PhraseEditDialog(
            mode = mode,
            onDismiss = { dialogMode = null },
            onConfirm = { value ->
                when (mode) {
                    PhraseDialogMode.Add -> onAdd(value)
                    is PhraseDialogMode.Edit -> onUpdate(mode.index, value)
                }
                dialogMode = null
            }
        )
    }
}

private sealed class PhraseDialogMode {
    data object Add : PhraseDialogMode()
    data class Edit(val index: Int, val initial: String) : PhraseDialogMode()
}

@Composable
private fun PhraseEditDialog(
    mode: PhraseDialogMode,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initial = when (mode) {
        PhraseDialogMode.Add -> ""
        is PhraseDialogMode.Edit -> mode.initial
    }
    var text by remember(mode) { mutableStateOf(initial) }
    HushNoteAlertDialog(
        onDismissRequest = onDismiss,
        title = if (mode is PhraseDialogMode.Add) "添加短语" else "编辑短语",
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如：左母乳") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.AccentRed,
                    cursorColor = AppColors.AccentRed
                )
            )
        },
        confirmButton = {
            HushNoteDialogActionButton(
                text = "保存",
                accent = true,
                enabled = text.trim().isNotEmpty(),
                onClick = { onConfirm(text.trim()) }
            )
        },
        dismissButton = {
            HushNoteDialogActionButton(text = "取消", accent = false, onClick = onDismiss)
        }
    )
}
