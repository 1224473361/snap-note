package com.snapnote.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapnote.app.model.IflytekCredentials

@Composable
fun IflytekSettingsScreen(
    credentials: IflytekCredentials,
    onBack: () -> Unit,
    onSave: (IflytekCredentials) -> Unit
) {
    var appId by rememberSaveable { mutableStateOf(credentials.appId) }
    var apiKey by rememberSaveable { mutableStateOf(credentials.apiKey) }
    var apiSecret by rememberSaveable { mutableStateOf(credentials.apiSecret) }
    var showSecret by rememberSaveable { mutableStateOf(false) }

    SubScreenScaffold(title = "讯飞语音", onBack = onBack) {
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
                        "用于语音转写、饭记识图与 AI 短评。开发时也可写在 android/local.properties 后重新编译安装。",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Text(
                        "控制台：console.xfyun.cn，开通「语音听写（流式版）」等服务。",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            IflytekField(
                value = appId,
                onValueChange = { appId = it },
                label = "APPID",
                modifier = Modifier.padding(top = 16.dp)
            )
            IflytekField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = "APIKey",
                modifier = Modifier.padding(top = 12.dp)
            )
            IflytekField(
                value = apiSecret,
                onValueChange = { apiSecret = it },
                label = "APISecret",
                isSecret = true,
                showSecret = showSecret,
                onToggleSecret = { showSecret = !showSecret },
                modifier = Modifier.padding(top = 12.dp)
            )

            Button(
                onClick = {
                    onSave(
                        IflytekCredentials(
                            appId = appId.trim(),
                            apiKey = apiKey.trim(),
                            apiSecret = apiSecret.trim()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("保存", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun IflytekField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isSecret: Boolean = false,
    showSecret: Boolean = false,
    onToggleSecret: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable(onClick = onToggleSecret)
                )
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(autoCorrect = false)
    )
}
