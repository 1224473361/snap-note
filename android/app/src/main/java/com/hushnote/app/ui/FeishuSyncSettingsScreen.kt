package com.hushnote.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.model.FeishuConfig
import com.hushnote.app.util.DateUtils

@Composable
fun FeishuSyncSettingsScreen(
    config: FeishuConfig,
    syncing: Boolean,
    onBack: () -> Unit,
    onSave: (FeishuConfig) -> Unit,
    onSyncLast7Days: () -> Unit,
    onSyncLast30Days: () -> Unit,
    onSyncFull: () -> Unit
) {
    var enabled by rememberSaveable { mutableStateOf(config.enabled) }
    var appId by rememberSaveable { mutableStateOf(config.appId) }
    var appSecret by rememberSaveable { mutableStateOf(config.appSecret) }
    var bitableAppToken by rememberSaveable { mutableStateOf(config.bitableAppToken) }
    var memoTableId by rememberSaveable { mutableStateOf(config.memoTableId) }
    var mealTableId by rememberSaveable { mutableStateOf(config.mealTableId) }
    var feedingTableId by rememberSaveable { mutableStateOf(config.feedingTableId) }
    var chronicleTableId by rememberSaveable { mutableStateOf(config.chronicleTableId) }
    var showSecret by rememberSaveable { mutableStateOf(false) }

    SubScreenScaffold(title = "飞书同步", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "两人共用一个飞书账号时，两台手机填写相同配置即可共享文字。图片和语音仍保留在各自手机本地。",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    if (config.lastSyncAt > 0L) {
                        Text(
                            "上次同步：${DateUtils.formatTimeWithSeconds(config.lastSyncAt)}",
                            color = AppColors.TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("启用飞书同步", color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "同步近7天 / 近30天 / 全量仅处理当前首页模块；更早的数据请用全量同步",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            Text(
                "应用凭证",
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
            ConfigField(
                value = appId,
                onValueChange = { appId = it },
                label = "App ID",
                placeholder = "飞书开放平台应用 App ID"
            )
            ConfigField(
                value = appSecret,
                onValueChange = { appSecret = it },
                label = "App Secret",
                placeholder = "飞书开放平台应用 App Secret",
                isSecret = true,
                showSecret = showSecret,
                onToggleSecret = { showSecret = !showSecret }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "多维表格",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        bitableAppToken = ""
                        memoTableId = ""
                        mealTableId = ""
                        feedingTableId = ""
                        chronicleTableId = ""
                    }
                ) {
                    Text("一键清除", color = AppColors.AccentRed, fontSize = 13.sp)
                }
            }
            ConfigField(
                value = bitableAppToken,
                onValueChange = { bitableAppToken = it },
                label = "Base App Token",
                placeholder = "多维表格 URL 中的 base 标识"
            )
            ConfigField(
                value = memoTableId,
                onValueChange = { memoTableId = it },
                label = "语音笔记表 ID",
                placeholder = "tblxxxxxxxx"
            )
            ConfigField(
                value = mealTableId,
                onValueChange = { mealTableId = it },
                label = "饭记表 ID",
                placeholder = "tblxxxxxxxx"
            )
            ConfigField(
                value = feedingTableId,
                onValueChange = { feedingTableId = it },
                label = "育儿记表 ID",
                placeholder = "tblxxxxxxxx"
            )
            ConfigField(
                value = chronicleTableId,
                onValueChange = { chronicleTableId = it },
                label = "大事记表 ID",
                placeholder = "tblxxxxxxxx"
            )

            Text(
                "在飞书多维表格中，点击表名旁的「…」→「复制表 ID」即可获取。",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                "语音笔记表：id、content、timestamp、date_key、updated_at\n" +
                    "饭记表：id、dishes、note、ai_comment、timestamp、date_key、updated_at\n" +
                    "育儿记表：id、note、amount_ml、timestamp、date_key、updated_at\n" +
                    "大事记表：id、type、value、title、body、timestamp、date_key、updated_at",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 12.dp)
            )

            val saveInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = {
                    onSave(
                        FeishuConfig(
                            enabled = enabled,
                            appId = appId.trim(),
                            appSecret = appSecret.trim(),
                            bitableAppToken = bitableAppToken.trim(),
                            memoTableId = memoTableId.trim(),
                            mealTableId = mealTableId.trim(),
                            feedingTableId = feedingTableId.trim(),
                            chronicleTableId = chronicleTableId.trim(),
                            lastSyncAt = config.lastSyncAt
                        )
                    )
                },
                interactionSource = saveInteraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .pressScaleEffect(saveInteraction),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存配置", fontSize = 16.sp)
            }

            val draftConfigured = FeishuConfig(
                enabled = enabled,
                appId = appId.trim(),
                appSecret = appSecret.trim(),
                bitableAppToken = bitableAppToken.trim(),
                memoTableId = memoTableId.trim(),
                mealTableId = mealTableId.trim(),
                feedingTableId = feedingTableId.trim(),
                chronicleTableId = chronicleTableId.trim()
            ).isConfigured

            val sync7Interaction = remember { MutableInteractionSource() }
            OutlinedButton(
                onClick = onSyncLast7Days,
                enabled = !syncing && enabled && draftConfigured,
                interactionSource = sync7Interaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .pressScaleEffect(sync7Interaction, enabled = !syncing && enabled && draftConfigured),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (syncing) "同步中…" else "同步近7天", fontSize = 16.sp)
            }

            val sync30Interaction = remember { MutableInteractionSource() }
            OutlinedButton(
                onClick = onSyncLast30Days,
                enabled = !syncing && enabled && draftConfigured,
                interactionSource = sync30Interaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .pressScaleEffect(sync30Interaction, enabled = !syncing && enabled && draftConfigured),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (syncing) "同步中…" else "同步近30天", fontSize = 16.sp)
            }

            val syncFullInteraction = remember { MutableInteractionSource() }
            OutlinedButton(
                onClick = onSyncFull,
                enabled = !syncing && enabled && draftConfigured,
                interactionSource = syncFullInteraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
                    .pressScaleEffect(syncFullInteraction, enabled = !syncing && enabled && draftConfigured),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (syncing) "同步中…" else "全量同步", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ConfigField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isSecret: Boolean = false,
    showSecret: Boolean = false,
    onToggleSecret: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isSecret && !showSecret) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = if (isSecret && onToggleSecret != null) {
            {
                Text(
                    text = if (showSecret) "隐藏" else "显示",
                    color = AppColors.AccentRed,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable(onClick = onToggleSecret)
                )
            }
        } else {
            null
        },
        keyboardOptions = if (isSecret) {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        } else {
            KeyboardOptions.Default
        }
    )
}
