package com.hushnote.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.MealConfirmState
import com.hushnote.app.util.DateUtils

@Composable
fun MealConfirmScreen(
    state: MealConfirmState,
    onBack: () -> Unit,
    onDishesChange: (List<String>) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var draftDish by remember { mutableStateOf("") }
    val bitmap = remember(state.imagePath) {
        runCatching { BitmapFactory.decodeFile(state.imagePath) }.getOrNull()
    }

    SubScreenScaffold(title = "确认饭记", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.SoftFill)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = DateUtils.formatTimeWithSeconds(state.timestamp),
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(28.dp))
            MealSectionLabel("菜品")
            Spacer(modifier = Modifier.height(12.dp))

            if (state.recognizing) {
                Row(
                    modifier = Modifier.padding(vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppColors.AccentRed,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("正在识别菜品…", color = AppColors.TextSecondary, fontSize = 14.sp)
                }
            } else {
                if (state.recognizeFailed) {
                    Text(
                        text = state.errorMessage ?: "识别失败，可手动填写",
                        color = AppColors.AccentRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                MealDishEditor(
                    dishes = state.dishes,
                    draft = draftDish,
                    onDraftChange = { draftDish = it },
                    onDishesChange = onDishesChange
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            MealSectionLabel("备注")
            Spacer(modifier = Modifier.height(12.dp))
            MealSoftField(
                value = state.note,
                onValueChange = onNoteChange,
                placeholder = "写点什么…",
                singleLine = false,
                minLines = 3
            )

            Spacer(modifier = Modifier.height(36.dp))
            MealHairline()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消", color = AppColors.TextSecondary)
                }
                Button(
                    onClick = onSave,
                    enabled = !state.recognizing,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentRed)
                ) {
                    Text("确认保存", fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
