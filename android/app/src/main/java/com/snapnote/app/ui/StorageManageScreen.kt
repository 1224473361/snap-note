package com.snapnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapnote.app.model.StorageInfo
import com.snapnote.app.util.DateUtils

@Composable
fun StorageManageScreen(
    info: StorageInfo,
    onBack: () -> Unit,
    onExportBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onEmptyTrash: () -> Unit,
    onClearAsrLogs: () -> Unit,
    onAddShortcut: () -> Unit
) {
    SubScreenScaffold(title = "存储管理", onBack = onBack) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Storage, contentDescription = null, tint = AppColors.AccentRed)
                        Text(
                            "存储概览",
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                    StorageRow("记录条数", "${info.memoCount} 条")
                    StorageRow("音频占用", DateUtils.formatFileSize(info.audioBytes))
                    StorageRow("回收站", "${info.trashCount} 条 / ${DateUtils.formatFileSize(info.trashAudioBytes)}")
                    StorageRow("讯飞日志", "${info.asrLogCount} 条")
                }
            }

            Text(
                "备份与恢复",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                OutlinedButton(
                    onClick = onExportBackup,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.Backup, contentDescription = null)
                    Text("导出 ZIP", modifier = Modifier.padding(start = 6.dp))
                }
                OutlinedButton(
                    onClick = onRestoreBackup,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.Restore, contentDescription = null)
                    Text("恢复 ZIP", modifier = Modifier.padding(start = 6.dp))
                }
            }

            HorizontalDivider(
                color = AppColors.Divider,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            OutlinedButton(
                onClick = onEmptyTrash,
                enabled = info.trashCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("清空回收站")
            }
            OutlinedButton(
                onClick = onClearAsrLogs,
                enabled = info.asrLogCount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("清空讯飞日志")
            }
            Button(
                onClick = onAddShortcut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("添加「快速录音」到桌面")
            }
        }
    }
}

@Composable
private fun StorageRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AppColors.TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = AppColors.TextPrimary, fontSize = 14.sp)
    }
}
