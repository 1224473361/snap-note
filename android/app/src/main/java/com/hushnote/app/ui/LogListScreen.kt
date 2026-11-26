package com.hushnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.model.AsrLogEntry
import com.hushnote.app.util.DateUtils
import java.io.File

@Composable
fun LogListScreen(
    logs: List<AsrLogEntry>,
    onBack: () -> Unit
) {
    SubScreenScaffold(title = "讯飞调用日志", onBack = onBack) {
        if (logs.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text("暂无日志", color = AppColors.TextSecondary, fontSize = 15.sp)
                Text(
                    "录音转写后会在此记录",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs, key = { "${it.id}-${it.timestamp}-${it.status}" }) { log ->
                    LogListItem(log)
                }
            }
        }
    }
}

@Composable
private fun LogListItem(log: AsrLogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            RowStatusLine(log)
            Text(
                text = DateUtils.formatTime(log.timestamp),
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "音频：${File(log.audioPath).name} · ${log.pcmBytes / 1024}KB · ${log.elapsedMs}ms",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (log.isSuccess && log.resultText.isNotBlank()) {
                Text(
                    text = "结果：${log.resultText}",
                    color = AppColors.TextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (!log.isSuccess && log.errorMessage.isNotBlank()) {
                Text(
                    text = "错误：${log.errorMessage}",
                    color = AppColors.ErrorRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RowStatusLine(log: AsrLogEntry) {
    val (label, color) = when (log.status) {
        "success" -> "成功" to AppColors.SuccessGreen
        "error" -> "失败" to AppColors.ErrorRed
        else -> log.status to AppColors.TextSecondary
    }
    Text(
        text = label,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold
    )
}
