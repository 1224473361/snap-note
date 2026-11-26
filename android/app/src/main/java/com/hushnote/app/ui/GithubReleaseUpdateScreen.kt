package com.hushnote.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.update.GithubReleaseConfig
import com.hushnote.app.update.GithubReleaseSummary

@Composable
fun GithubReleaseUpdateScreen(
    latestName: String?,
    latestDateLabel: String?,
    loadingLatest: Boolean,
    downloading: Boolean,
    confirmRelease: GithubReleaseSummary?,
    onBack: () -> Unit,
    onVisible: () -> Unit,
    onRequestDownload: () -> Unit,
    onConfirmDownload: () -> Unit,
    onDismissConfirm: () -> Unit
) {
    LaunchedEffect(Unit) {
        onVisible()
    }

    if (confirmRelease != null) {
        HushNoteConfirmDialog(
            onDismissRequest = onDismissConfirm,
            title = "下载最新版",
            message = "最新发行版：${confirmRelease.displayName}\n" +
                "版本：${confirmRelease.tagName}\n" +
                "发布日期：${confirmRelease.dateLabel}\n\n是否下载并安装？",
            confirmText = "下载",
            onConfirm = onConfirmDownload,
            confirmDestructive = false,
            confirmEnabled = !downloading
        )
    }

    SubScreenScaffold(title = "下载最新版", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "从 GitHub Releases 拉取最新版本（${GithubReleaseConfig.owner}/${GithubReleaseConfig.repo}）。公开仓库无需令牌。",
                color = AppColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "远端最新发行版",
                        color = AppColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    when {
                        loadingLatest -> Text(
                            "加载中…",
                            color = AppColors.TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        latestName != null -> {
                            Text(
                                "名称：$latestName",
                                color = AppColors.TextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                "日期：${latestDateLabel ?: "—"}",
                                color = AppColors.TextSecondary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        else -> Text(
                            "暂无数据，请检查网络或稍后重试",
                            color = AppColors.TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onRequestDownload,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                enabled = !loadingLatest && !downloading
            ) {
                Text(if (downloading) "下载中…" else "下载最新版")
            }
        }
    }
}
