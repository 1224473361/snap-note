package com.snapnote.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.snapnote.app.model.AppScreen

private const val ScreenMotionDurationMs = 280

fun AppScreen.motionDepth(): Int = when (this) {
    AppScreen.Home -> 0
    AppScreen.Search,
    AppScreen.ChronicleStats,
    AppScreen.FeedingStats,
    AppScreen.Mine,
    AppScreen.MealConfirm -> 1
    AppScreen.MineFeishuHub,
    AppScreen.MineDataHub,
    AppScreen.MineIflytekHub,
    AppScreen.MemoEdit,
    AppScreen.MealDetail,
    AppScreen.SystemSettings -> 2
    else -> 3
}

fun AnimatedContentTransitionScope<AppScreen>.appScreenTransition(): ContentTransform {
    val forward = targetState.motionDepth() > initialState.motionDepth()
    val enterOffset: (Int) -> Int = { full -> if (forward) full / 5 else -full / 5 }
    val exitOffset: (Int) -> Int = { full -> if (forward) -full / 5 else full / 5 }
    return (
        slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) { enterOffset(it) } +
            fadeIn(animationSpec = tween(ScreenMotionDurationMs))
        ).togetherWith(
        slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) { exitOffset(it) } +
            fadeOut(animationSpec = tween(ScreenMotionDurationMs))
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.listItemMotionModifier(): Modifier =
    if (LocalReduceMotion.current) {
        Modifier
    } else {
        Modifier.animateItemPlacement(animationSpec = tween(220))
    }

private val HomeListCardShape = RoundedCornerShape(12.dp)

fun Modifier.floatingListCard(): Modifier = shadow(
    elevation = 3.dp,
    shape = HomeListCardShape,
    clip = false,
    ambientColor = Color(0x14000000),
    spotColor = Color(0x26000000)
)

private const val SheetStaggerStepMs = 55
private const val SheetSectionEnterMs = 300

@Composable
fun SheetStaggerSection(
    order: Int,
    visible: Boolean = true,
    slide: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((order * SheetStaggerStepMs).toLong())
        entered = true
    }
    val show = visible && entered
    val enterFade = fadeIn(
        animationSpec = tween(SheetSectionEnterMs, easing = FastOutSlowInEasing)
    )
    val enterMotion = if (slide) {
        enterFade + slideInVertically(
            animationSpec = tween(SheetSectionEnterMs, easing = FastOutSlowInEasing)
        ) { height -> height / 5 }
    } else {
        enterFade
    }
    val exitMotion = if (slide) {
        fadeOut(tween(140)) + slideOutVertically(tween(140)) { height -> height / 6 }
    } else {
        fadeOut(tween(140))
    }
    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = show,
        enter = enterMotion,
        exit = exitMotion
    ) {
        content()
    }
}

/** 按压缩放系数：轻微即可，过度会显得"塌陷"，与首页/记一笔保持一致。 */
private const val PressScaleDown = 0.97f

/**
 * 纯视觉的按压缩放效果，供 Material Button / OutlinedButton 等自带 onClick 的组件叠加使用。
 * 与 [clickableWithPressScale] 共用同一套参数，保证全局反馈一致。
 */
@Composable
fun Modifier.pressScaleEffect(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = LocalReduceMotion.current
    val scale by animateFloatAsState(
        targetValue = if (!reduceMotion && enabled && pressed) PressScaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScaleEffect"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun Modifier.clickableWithPressScale(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val clickHaptic = rememberClickHapticAction()
    val scaled = pressScaleEffect(interactionSource, enabled)
    scaled.clickable(
        enabled = enabled,
        onClick = {
            clickHaptic()
            onClick()
        },
        interactionSource = interactionSource,
        indication = null
    )
}
