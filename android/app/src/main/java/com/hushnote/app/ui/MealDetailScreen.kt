package com.hushnote.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.MealAiPhase
import com.hushnote.app.model.MealItem
import com.hushnote.app.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MealDetailScreen(
    item: MealItem,
    aiPhase: MealAiPhase,
    aiPreview: String,
    aiError: String,
    onBack: () -> Unit,
    onSave: (dishes: List<String>, note: String, timestamp: Long, aiComment: String) -> Unit,
    onDelete: () -> Unit,
    onAnalyze: () -> Unit,
    onDiscardPreview: () -> Unit,
    onAdopt: () -> Unit,
    onCancelAnalyze: () -> Unit
) {
    var dishes by remember(item.id) { mutableStateOf(item.dishes) }
    var note by remember(item.id) { mutableStateOf(item.note) }
    var aiComment by remember(item.id, item.aiComment) { mutableStateOf(item.aiComment) }
    var timestamp by remember(item.id) { mutableLongStateOf(item.timestamp) }
    var draftDish by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(false) }
    var previewOpen by remember { mutableStateOf(false) }

    val bitmap = remember(item.imagePath) {
        runCatching { BitmapFactory.decodeFile(item.imagePath) }.getOrNull()
    }
    val canAnalyze = item.dishes.any { it.isNotBlank() } && aiPhase != MealAiPhase.Streaming

    DisposableEffect(item.id) {
        onDispose { onCancelAnalyze() }
    }

    SubScreenScaffold(
        title = if (editing) "编辑饭记" else "饭记详情",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "查看大图",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { previewOpen = true },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.SoftFill)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${item.dateKey} · ${DateUtils.formatTime(timestamp)}",
                color = AppColors.TextSecondary,
                fontSize = 13.sp,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.height(28.dp))
            MealSectionLabel("菜品")
            Spacer(modifier = Modifier.height(12.dp))

            if (editing) {
                MealDishEditor(
                    dishes = dishes,
                    draft = draftDish,
                    onDraftChange = { draftDish = it },
                    onDishesChange = { dishes = it }
                )

                Spacer(modifier = Modifier.height(28.dp))
                MealSectionLabel("备注")
                Spacer(modifier = Modifier.height(12.dp))
                MealSoftField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "写点什么…",
                    singleLine = false,
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(28.dp))
                MealSectionLabel("AI 短评")
                Spacer(modifier = Modifier.height(12.dp))
                MealSoftField(
                    value = aiComment,
                    onValueChange = { aiComment = it },
                    placeholder = "可清空或手改…",
                    singleLine = false,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(28.dp))
                MealSectionLabel("时间")
                Spacer(modifier = Modifier.height(12.dp))
                MealDateTimePickerRow(
                    timestamp = timestamp,
                    onTimestampChange = { timestamp = it }
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dishes.forEach { dish ->
                        Text(
                            text = dish,
                            color = AppColors.TextPrimary,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppColors.ChipBackground)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
                if (note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(28.dp))
                    MealSectionLabel("备注")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = note,
                        color = AppColors.TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )
                }
                if (item.aiComment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(28.dp))
                    MealSectionLabel("AI 短评")
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.aiComment,
                        color = AppColors.TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 24.sp
                    )
                }

                if (aiPhase == MealAiPhase.Streaming ||
                    aiPhase == MealAiPhase.Ready ||
                    aiPhase == MealAiPhase.Error
                ) {
                    Spacer(modifier = Modifier.height(28.dp))
                    MealSectionLabel("预览")
                    Spacer(modifier = Modifier.height(10.dp))
                    when (aiPhase) {
                        MealAiPhase.Streaming -> {
                            Text(
                                text = aiPreview.ifBlank { "生成中…" },
                                color = AppColors.TextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "生成中…",
                                color = AppColors.TextSecondary,
                                fontSize = 13.sp
                            )
                            TextButton(onClick = onDiscardPreview) {
                                Text("丢掉", color = AppColors.TextSecondary)
                            }
                        }
                        MealAiPhase.Ready -> {
                            Text(
                                text = aiPreview,
                                color = AppColors.TextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = onAdopt) {
                                    Text("采用", color = AppColors.AccentRed)
                                }
                                TextButton(onClick = onDiscardPreview) {
                                    Text("丢掉", color = AppColors.TextSecondary)
                                }
                                TextButton(onClick = onAnalyze) {
                                    Text("再生成", color = AppColors.TextSecondary)
                                }
                            }
                        }
                        MealAiPhase.Error -> {
                            Text(
                                text = aiError.ifBlank { "生成失败" },
                                color = AppColors.ErrorRed,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            )
                            TextButton(onClick = onDiscardPreview) {
                                Text("丢掉", color = AppColors.TextSecondary)
                            }
                        }
                        MealAiPhase.Idle -> Unit
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                TextButton(
                    onClick = onAnalyze,
                    enabled = canAnalyze
                ) {
                    Text(
                        text = "AI 分析",
                        color = if (canAnalyze) AppColors.AccentRed else AppColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
            MealHairline()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (editing) {
                    TextButton(
                        onClick = {
                            dishes = item.dishes
                            note = item.note
                            aiComment = item.aiComment
                            timestamp = item.timestamp
                            draftDish = ""
                            editing = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消", color = AppColors.TextSecondary)
                    }
                    Button(
                        onClick = { onSave(dishes, note, timestamp, aiComment) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentRed)
                    ) {
                        Text("保存", fontWeight = FontWeight.Medium)
                    }
                } else {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("删除", color = AppColors.ErrorRed)
                    }
                    Button(
                        onClick = {
                            onCancelAnalyze()
                            editing = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentRed)
                    ) {
                        Text("编辑", fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (previewOpen && bitmap != null) {
        val fileName = "meal-${item.dateKey}-${DateUtils.formatTime(item.timestamp).replace(":", "")}.jpg"
        MealImageViewer(
            imagePath = item.imagePath,
            suggestedFileName = fileName,
            onDismiss = { previewOpen = false }
        )
    }
}
