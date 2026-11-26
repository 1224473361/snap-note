package com.hushnote.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Search
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hushnote.app.MainUiState
import com.hushnote.app.R
import com.hushnote.app.model.HomeMode
import com.hushnote.app.model.FeedingGroup
import com.hushnote.app.model.ChronicleFilter
import com.hushnote.app.model.ChronicleGroup
import com.hushnote.app.model.ChronicleItem
import com.hushnote.app.model.FeedingStatsCardStyle
import com.hushnote.app.model.FeedingItem
import com.hushnote.app.model.MealGroup
import com.hushnote.app.model.MealItem
import com.hushnote.app.model.MemoConstants
import com.hushnote.app.model.MemoGroup
import com.hushnote.app.model.MemoItem
import com.hushnote.app.util.DateUtils
import com.hushnote.app.util.HapticUtils

/** 中间悬浮按钮会压住列表底部，留白以便滚到最末一条 */
private val HomeListFabClearance = 64.dp

private fun homeListContentPadding(): PaddingValues = PaddingValues(
    start = 16.dp,
    end = 16.dp,
    top = 12.dp,
    bottom = 12.dp + HomeListFabClearance
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HushNoteScreen(
    state: MainUiState,
    onHomeVisible: () -> Unit,
    onSearch: () -> Unit,
    onMine: () -> Unit,
    onFeedingPullRefresh: () -> Unit,
    onRecordStart: () -> Boolean,
    onRecordEnd: (Boolean) -> Unit,
    onMealFabClick: () -> Unit,
    onFeedingFabClick: () -> Unit,
    onChronicleFabClick: () -> Unit,
    onChronicleFilterSelected: (ChronicleFilter) -> Unit,
    onHomeModeSelected: (HomeMode) -> Unit,
    onPlayToggle: (String, String) -> Unit,
    onDelete: (String, String) -> Unit,
    onEdit: (MemoItem) -> Unit,
    onMealClick: (MealItem) -> Unit,
    onFeedingClick: (FeedingItem) -> Unit,
    onDeleteFeeding: (String) -> Unit,
    onChronicleClick: (ChronicleItem) -> Unit,
    onDeleteChronicle: (String) -> Unit,
    onDismissMealSheet: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit
) {
    var showHomeModeSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onHomeVisible()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.PageBackground)
    ) {
        TopBar(
            homeMode = state.homeMode,
            onOpenHomeModePicker = { showHomeModeSheet = true }
        )

        AnimatedVisibility(
            visible = state.isRecording,
            enter = slideInVertically { -it } + fadeIn(tween(200)),
            exit = slideOutVertically { -it } + fadeOut(tween(160))
        ) {
            RecordingOverlay(durationText = state.recordingDuration)
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(AppColors.PageBackground)
        ) {
            AnimatedContent(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.PageBackground),
                targetState = state.homeMode,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    val offset: (Int) -> Int = { h -> if (forward) h / 8 else -h / 8 }
                    (fadeIn(tween(220)) + slideInVertically { offset(it) })
                        .togetherWith(fadeOut(tween(160)) + slideOutVertically { -offset(it) })
                },
                label = "homeMode"
            ) { mode ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.PageBackground)
                ) {
                when (mode) {
                    HomeMode.NOTE -> {
                        if (state.groups.isEmpty()) {
                            EmptyState(
                                icon = Icons.Outlined.MicNone,
                                title = stringResource(R.string.empty_title),
                                subtitle = stringResource(R.string.empty_subtitle)
                            )
                        } else {
                            MemoList(
                                groups = state.groups,
                                playingId = state.playingId,
                                pausedId = state.pausedId,
                                onPlayToggle = onPlayToggle,
                                onDelete = onDelete,
                                onEdit = onEdit
                            )
                        }
                    }
                    HomeMode.MEAL -> {
                        if (state.mealGroups.isEmpty()) {
                            EmptyState(
                                icon = Icons.Outlined.Restaurant,
                                title = stringResource(R.string.empty_meal_title),
                                subtitle = stringResource(R.string.empty_meal_subtitle)
                            )
                        } else {
                            MealGroupList(
                                groups = state.mealGroups,
                                onMealClick = onMealClick
                            )
                        }
                    }
                    HomeMode.FEEDING -> {
                        FeedingListWithPullRefresh(
                            groups = state.feedingGroups,
                            statsCardStyle = state.feedingStatsCardStyle,
                            isSyncing = state.feishuSyncing,
                            onRefresh = onFeedingPullRefresh,
                            onFeedingClick = onFeedingClick,
                            onDeleteFeeding = onDeleteFeeding
                        )
                    }
                    HomeMode.CHRONICLE -> {
                        val filteredGroups = remember(state.chronicleGroups, state.chronicleFilter) {
                            state.chronicleGroups.mapNotNull { group ->
                                val items = group.items.filter { state.chronicleFilter.matchesItem(it.type) }
                                if (items.isEmpty()) null else group.copy(items = items)
                            }
                        }
                        Column(modifier = Modifier.fillMaxSize()) {
                            ChronicleFilterChips(
                                selected = state.chronicleFilter,
                                onSelected = onChronicleFilterSelected
                            )
                            if (filteredGroups.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Outlined.Timeline,
                                    title = stringResource(R.string.empty_chronicle_title),
                                    subtitle = stringResource(R.string.empty_chronicle_subtitle)
                                )
                            } else {
                                ChronicleGroupList(
                                    groups = filteredGroups,
                                    onChronicleClick = onChronicleClick,
                                    onDeleteChronicle = onDeleteChronicle
                                )
                            }
                        }
                    }
                }
                }
            }
        }

        BottomBar(
            homeMode = state.homeMode,
            isRecording = state.isRecording,
            onSearch = onSearch,
            onMine = onMine,
            onRecordStart = onRecordStart,
            onRecordEnd = onRecordEnd,
            onMealFabClick = onMealFabClick,
            onFeedingFabClick = onFeedingFabClick,
            onChronicleFabClick = onChronicleFabClick
        )
    }

    if (state.showMealSourceSheet) {
        MealCameraBottomSheet(
            onDismiss = onDismissMealSheet,
            onTakePhoto = onTakePhoto,
            onPickGallery = onPickGallery
        )
    }

    HomeModePickerSheet(
        visible = showHomeModeSheet,
        homeMode = state.homeMode,
        onDismiss = { showHomeModeSheet = false },
        onHomeModeSelected = onHomeModeSelected
    )
}

@Composable
private fun TopBar(
    homeMode: HomeMode,
    onOpenHomeModePicker: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.CardBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenHomeModePicker) {
            Icon(
                imageVector = homeModeIcon(homeMode),
                contentDescription = stringResource(R.string.switch_home_module),
                tint = AppColors.AccentRed,
                modifier = Modifier.size(26.dp)
            )
        }
        AnimatedContent(
            targetState = homeMode,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(120))
            },
            label = "homeTitle",
            modifier = Modifier.weight(1f)
        ) { mode ->
            Text(
                text = homeModeTitle(mode),
                color = AppColors.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppColors.Divider)
    )
}

@Composable
private fun RecordingOverlay(durationText: String) {
    val pulseAlpha = rememberInfiniteTransition(label = "recordBarPulse").animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recordBarAlpha"
    ).value
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xCC000000).copy(alpha = pulseAlpha),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RecordingDots()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.recording_hint),
                color = Color.White,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = durationText,
                color = Color(0xFFEEEEEE),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun RecordingDots() {
    val transition = rememberInfiniteTransition(label = "recordDots")
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { index ->
            val scale = transition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(420, delayMillis = index * 120),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            ).value
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    val floatOffset = rememberInfiniteTransition(label = "emptyFloat").animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emptyOffsetY"
    ).value
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .size(72.dp)
                .background(AppColors.AccentRed.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.AccentRed,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
private fun MemoList(
    groups: List<MemoGroup>,
    playingId: String,
    pausedId: String,
    onPlayToggle: (String, String) -> Unit,
    onDelete: (String, String) -> Unit,
    onEdit: (MemoItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = homeListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groups.forEach { group ->
            item(key = "header-${group.dateKey}") {
                Text(
                    text = group.dateKey,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            items(group.items, key = { it.id }) { item ->
                SwipeDeleteMemoItem(
                    modifier = listItemMotionModifier(),
                    item = item,
                    isPlaying = playingId == item.id,
                    isPaused = pausedId == item.id,
                    onPlayToggle = onPlayToggle,
                    onDelete = onDelete,
                    onEdit = onEdit
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FeedingListWithPullRefresh(
    groups: List<FeedingGroup>,
    statsCardStyle: FeedingStatsCardStyle,
    isSyncing: Boolean,
    onRefresh: () -> Unit,
    onFeedingClick: (FeedingItem) -> Unit,
    onDeleteFeeding: (String) -> Unit
) {
    val pullState = rememberPullRefreshState(refreshing = isSyncing, onRefresh = onRefresh)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullState)
    ) {
        if (groups.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.ChildCare,
                title = stringResource(R.string.empty_title),
                subtitle = stringResource(R.string.empty_feeding_subtitle)
            )
        } else {
                        FeedingGroupList(
                            groups = groups,
                            statsCardStyle = statsCardStyle,
                            onFeedingClick = onFeedingClick,
                            onDeleteFeeding = onDeleteFeeding
                        )
        }
        PullRefreshIndicator(
            refreshing = isSyncing,
            state = pullState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun ChronicleGroupList(
    groups: List<ChronicleGroup>,
    onChronicleClick: (ChronicleItem) -> Unit,
    onDeleteChronicle: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = homeListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groups.forEach { group ->
            item(key = "chronicle-header-${group.dateKey}") {
                Text(
                    text = group.dateKey,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            items(group.items, key = { it.id }) { item ->
                SwipeDeleteChronicleItem(
                    modifier = listItemMotionModifier(),
                    item = item,
                    onDelete = onDeleteChronicle,
                    onClick = { onChronicleClick(item) }
                )
            }
        }
    }
}

@Composable
private fun FeedingGroupList(
    groups: List<FeedingGroup>,
    statsCardStyle: FeedingStatsCardStyle,
    onFeedingClick: (FeedingItem) -> Unit,
    onDeleteFeeding: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = homeListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groups.forEach { group ->
            item(key = "feeding-day-${group.dateKey}-${statsCardStyle.name}") {
                FeedingDaySection(
                    dateKey = group.dateKey,
                    items = group.items,
                    statsCardStyle = statsCardStyle
                )
            }
            items(group.items, key = { it.id }) { item ->
                SwipeDeleteFeedingItem(
                    modifier = listItemMotionModifier(),
                    item = item,
                    onDelete = onDeleteFeeding,
                    onClick = { onFeedingClick(item) }
                )
            }
        }
    }
}

@Composable
private fun MealGroupList(
    groups: List<MealGroup>,
    onMealClick: (MealItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = homeListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groups.forEach { group ->
            item(key = "meal-header-${group.dateKey}") {
                Text(
                    text = group.dateKey,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            items(group.items, key = { it.id }) { item ->
                MealListItem(
                    modifier = listItemMotionModifier(),
                    item = item,
                    onClick = { onMealClick(item) }
                )
            }
        }
    }
}

@Composable
private fun BottomBar(
    homeMode: HomeMode,
    isRecording: Boolean,
    onSearch: () -> Unit,
    onMine: () -> Unit,
    onRecordStart: () -> Boolean,
    onRecordEnd: (Boolean) -> Unit,
    onMealFabClick: () -> Unit,
    onFeedingFabClick: () -> Unit,
    onChronicleFabClick: () -> Unit
) {
    val pulseScale = if (isRecording) {
        val transition = rememberInfiniteTransition(label = "recordPulse")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        ).value
    } else {
        1f
    }

    val barHeight = 52.dp
    val fabSize = 80.dp
    val fabProtrusion = 28.dp
    val fabElevation by animateDpAsState(
        targetValue = if (isRecording) 16.dp else 12.dp,
        animationSpec = tween(220),
        label = "fabElevation"
    )
    val view = LocalView.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppColors.CardBackground,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppColors.Divider)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val showStatsEntry = homeMode == HomeMode.CHRONICLE || homeMode == HomeMode.FEEDING
                    TextButton(onClick = onSearch, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (showStatsEntry) Icons.Outlined.BarChart else Icons.Outlined.Search,
                            contentDescription = null,
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(if (showStatsEntry) R.string.home_tab_stats else R.string.home_tab_search),
                            color = AppColors.TextPrimary,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(fabSize))

                    TextButton(onClick = onMine, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.home_tab_mine), color = AppColors.TextPrimary, fontSize = 15.sp)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -fabProtrusion)
                .zIndex(1f)
                .requiredSize(fabSize)
                .then(
                    when (homeMode) {
                        HomeMode.NOTE -> Modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                var cancelled = false
                                val startY = down.position.y
                                if (!onRecordStart()) return@awaitEachGesture
                                HapticUtils.performRecordPressFeedback(context, view)
                                var ended = false
                                try {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Main)
                                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                        if (!change.pressed) {
                                            onRecordEnd(cancelled)
                                            ended = true
                                            break
                                        }
                                        if (change.position.y - startY < -MemoConstants.CANCEL_SLIDE_THRESHOLD_PX) {
                                            cancelled = true
                                        }
                                        change.consume()
                                    }
                                } finally {
                                    if (!ended) {
                                        onRecordEnd(cancelled)
                                    }
                                }
                            }
                        }
                        HomeMode.MEAL -> Modifier.clickableWithPressScale(onClick = onMealFabClick)
                        HomeMode.FEEDING -> Modifier.clickableWithPressScale(onClick = onFeedingFabClick)
                        HomeMode.CHRONICLE -> Modifier.clickableWithPressScale(onClick = onChronicleFabClick)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
                    .shadow(
                        elevation = fabElevation,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = AppColors.AccentRed.copy(alpha = 0.2f),
                        spotColor = AppColors.AccentRed.copy(alpha = 0.34f)
                    ),
                shape = CircleShape,
                color = if (isRecording) AppColors.AccentRed.copy(alpha = 0.92f) else AppColors.AccentRed,
                shadowElevation = fabElevation,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (homeMode) {
                            HomeMode.NOTE -> Icons.Default.Mic
                            HomeMode.MEAL -> Icons.Default.CameraAlt
                            HomeMode.FEEDING -> Icons.Default.Add
                            HomeMode.CHRONICLE -> Icons.Default.Add
                        },
                        contentDescription = when (homeMode) {
                            HomeMode.NOTE -> stringResource(R.string.fab_record)
                            HomeMode.MEAL -> stringResource(R.string.fab_photo)
                            HomeMode.FEEDING -> stringResource(R.string.fab_add)
                            HomeMode.CHRONICLE -> stringResource(R.string.fab_add)
                        },
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}
