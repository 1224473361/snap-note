package com.snapnote.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import com.snapnote.app.model.ChronicleConstants
import com.snapnote.app.model.ChronicleType
import com.snapnote.app.util.ChronicleFormat
import java.io.File

@Composable
fun ChronicleRecordFormFields(
    type: ChronicleType,
    valueText: String,
    onValueChange: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    body: String,
    onBodyChange: (String) -> Unit,
    imagePaths: List<String>,
    onRemoveImage: (String) -> Unit,
    onAddImages: () -> Unit,
    timeSection: @Composable () -> Unit
) {
    timeSection()
    when (type) {
        ChronicleType.WEIGHT, ChronicleType.HEIGHT -> {
            val unit = if (type == ChronicleType.WEIGHT) "kg" else "cm"
            OutlinedTextField(
                value = valueText,
                onValueChange = { onValueChange(ChronicleFormat.filterDecimalInput(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                label = { Text("数值（$unit）") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = outlinedFieldColors()
            )
            Text(
                text = "保留两位小数",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        ChronicleType.MILESTONE -> {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                label = { Text("标题") },
                singleLine = true,
                colors = outlinedFieldColors()
            )
            OutlinedTextField(
                value = body,
                onValueChange = onBodyChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                label = { Text("正文") },
                minLines = 3,
                colors = outlinedFieldColors()
            )
            MilestoneImageRow(
                imagePaths = imagePaths,
                onRemoveImage = onRemoveImage,
                onAddImages = onAddImages,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun MilestoneImageRow(
    imagePaths: List<String>,
    onRemoveImage: (String) -> Unit,
    onAddImages: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "图片（最多 ${ChronicleConstants.MAX_MILESTONE_IMAGES} 张）",
            color = AppColors.TextSecondary,
            fontSize = 13.sp
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(imagePaths, key = { it }) { path ->
                Box {
                    val bitmap = remember(path) {
                        runCatching {
                            BitmapFactory.decodeFile(path)?.asImageBitmap()
                        }.getOrNull()
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.Divider)
                        )
                    }
                    IconButton(
                        onClick = { onRemoveImage(path) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "移除",
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (imagePaths.size < ChronicleConstants.MAX_MILESTONE_IMAGES) {
                item(key = "add") {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.CardBackground)
                            .clickable(onClick = onAddImages),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加图片", tint = AppColors.AccentRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppColors.AccentRed,
    cursorColor = AppColors.AccentRed,
    focusedLabelColor = AppColors.AccentRed
)
