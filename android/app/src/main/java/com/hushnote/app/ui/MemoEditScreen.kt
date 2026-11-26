package com.hushnote.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.model.MemoItem
import com.hushnote.app.util.DateUtils

@Composable
fun MemoEditScreen(
    item: MemoItem,
    onBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var content by rememberSaveable(item.id) { mutableStateOf(item.content) }

    SubScreenScaffold(title = "编辑文字", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "${item.dateKey} ${DateUtils.formatTimeWithSeconds(item.timestamp)}",
                color = AppColors.TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("输入转写文字", color = AppColors.Placeholder) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.TextPrimary,
                    unfocusedTextColor = AppColors.TextPrimary,
                    focusedContainerColor = AppColors.CardBackground,
                    unfocusedContainerColor = AppColors.CardBackground,
                    focusedBorderColor = AppColors.AccentRed,
                    unfocusedBorderColor = AppColors.Divider,
                    cursorColor = AppColors.AccentRed,
                    focusedPlaceholderColor = AppColors.Placeholder,
                    unfocusedPlaceholderColor = AppColors.Placeholder
                )
            )
            Button(
                onClick = { onSave(content.trim()) },
                enabled = content.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存", fontSize = 16.sp)
            }
        }
    }
}
