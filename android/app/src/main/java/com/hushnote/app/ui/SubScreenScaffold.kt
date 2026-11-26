package com.hushnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.hushnote.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreenScaffold(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color? = null,
    topBarContainerColor: Color? = null,
    contentBackgroundBrush: Boolean = false,
    content: @Composable () -> Unit
) {
    val resolvedContainer = containerColor ?: AppColors.PageBackground
    val resolvedTopBar = topBarContainerColor ?: AppColors.CardBackground
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = resolvedTopBar
                )
            )
        },
        containerColor = resolvedContainer
    ) { padding ->
        val bgModifier = if (contentBackgroundBrush) {
            Modifier.background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.FeedingStatsGradientTop,
                        AppColors.FeedingStatsGradientBottom
                    )
                )
            )
        } else {
            Modifier.background(resolvedContainer)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(bgModifier)
        ) {
            content()
        }
    }
}
