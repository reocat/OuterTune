package com.dd3boh.outertune.ui.component

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.SNACKBAR_VERY_SHORT
import com.dd3boh.outertune.constants.SwipeToQueueKey
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Swipe to perform an action. This supports one or two actions
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeActionBox(
    firstAction: Pair<ImageVector, () -> Unit>,
    modifier: Modifier = Modifier,
    secondAction: Pair<ImageVector, () -> Unit>? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val swipeOffset = remember { mutableFloatStateOf(0f) }
    val progress = remember { mutableIntStateOf(0) } // swipeOffset but to track haptics and opacity
    
    val actionQueueNext = with(density) { 80.dp.toPx() }
    val actionQueueEnd = with(density) { 160.dp.toPx() }
    
    val firstThreshold = actionQueueNext
    val secondThreshold = actionQueueEnd
    
    val currentAction = remember {
        derivedStateOf {
            when {
                swipeOffset.floatValue >= secondThreshold -> 2
                swipeOffset.floatValue >= firstThreshold -> 1
                else -> 0
            }
        }
    }
    
    LaunchedEffect(currentAction.value) {
        if (currentAction.value != 0) {
            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
        }
        progress.intValue = currentAction.value
    }

    val backgroundAlpha by remember {
        derivedStateOf {
            when (currentAction.value) {
                0 -> 0f
                1 -> (swipeOffset.floatValue / firstThreshold).coerceIn(0f, 0.6f)
                else -> (swipeOffset.floatValue / secondThreshold).coerceIn(0f, 0.7f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    swipeOffset.floatValue = (swipeOffset.floatValue + delta).coerceAtLeast(0f)
                },
                onDragStopped = {
                    when {
                        swipeOffset.floatValue >= secondThreshold -> {
                            if (secondAction == null) {
                                firstAction.second.invoke()
                            } else {
                                secondAction.second.invoke()
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            resetDrag(coroutineScope, swipeOffset)
                        }
                        swipeOffset.floatValue >= firstThreshold -> {
                            firstAction.second.invoke()
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            resetDrag(coroutineScope, swipeOffset)
                        }
                        else -> resetDrag(coroutineScope, swipeOffset)
                    }
                }
            )
    ) {
        // Background with action indicators
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = when (currentAction.value) {
                        1 -> MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha)
                        2 -> MaterialTheme.colorScheme.secondary.copy(alpha = backgroundAlpha)
                        else -> Color.Transparent
                    }
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val iconScale by animateFloatAsState(
                targetValue = if (currentAction.value != 0) 1.2f else 1f,
                label = "iconPop"
            )

            if (currentAction.value > 0) {
                Icon(
                    imageVector = if (currentAction.value == 2 && secondAction != null) secondAction.first else firstAction.first,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.scale(iconScale)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(swipeOffset.floatValue.roundToInt(), 0) }
                .fillMaxWidth(),
            content = content
        )
    }
}

@Composable
fun SwipeToQueueBox(
    modifier: Modifier = Modifier,
    item: MediaItem,
    content: @Composable BoxScope.() -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val playerConnection = LocalPlayerConnection.current
    val coroutineScope = rememberCoroutineScope()
    val swipeToQueueEnabled by rememberPreference(SwipeToQueueKey, true)

    if (!enabled || !swipeToQueueEnabled) {
        Box(modifier = modifier) { content() }
        return
    }

    SwipeActionBox(
        firstAction = Pair(Icons.AutoMirrored.Outlined.PlaylistPlay, {
            playerConnection?.enqueueNext(item)
            coroutineScope.launch {
                snackbarHostState?.showSnackbar(
                    message = context.getString(R.string.song_added_to_queue, item.mediaMetadata.title),
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
            }
        }),
        secondAction = Pair(Icons.AutoMirrored.Outlined.PlaylistAdd, {
            playerConnection?.enqueueEnd(item)
            coroutineScope.launch {
                val job = launch {
                    snackbarHostState?.showSnackbar(
                        message = context.getString(
                            R.string.song_added_to_queue_end,
                            item.mediaMetadata.title
                        ),
                        withDismissAction = true,
                        duration = SnackbarDuration.Indefinite
                    )
                }
                delay(SNACKBAR_VERY_SHORT)
                job.cancel()
            }
        }),
        enabled = enabled,
        modifier = modifier,
        content = content
    )
}


private fun resetDrag(scope: CoroutineScope, offset: MutableState<Float>) {
    scope.launch {
        animate(
            initialValue = offset.value,
            targetValue = 0f,
            animationSpec = tween(durationMillis = 300)
        ) { value, _ ->
            offset.value = value
        }
    }
}