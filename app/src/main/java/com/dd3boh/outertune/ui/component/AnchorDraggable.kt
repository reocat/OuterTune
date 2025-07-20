package com.dd3boh.outertune.ui.component

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private sealed class SwipeAction(val threshold: Float, val color: Color, val icon: ImageVector) {
    data class QueueNext(val t: Float, val c: Color, val i: ImageVector) : SwipeAction(t, c, i)
    data class QueueEnd(val t: Float, val c: Color, val i: ImageVector) : SwipeAction(t, c, i)
    object None : SwipeAction(0f, Color.Transparent, Icons.AutoMirrored.Outlined.PlaylistPlay)
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
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val swipeOffset = remember { mutableFloatStateOf(0f) }
    val swipeToQueueEnabled by rememberPreference(SwipeToQueueKey, true)

    if (!enabled || !swipeToQueueEnabled) {
        Box(modifier = modifier) { content() }
        return
    }

    val actionQueueNext = SwipeAction.QueueNext(
        t = with(density) { 80.dp.toPx() },
        c = MaterialTheme.colorScheme.primary.copy(
            alpha = 1f,
            red = (MaterialTheme.colorScheme.primary.red + 0.3f).coerceAtMost(1f),
            green = (MaterialTheme.colorScheme.primary.green + 0.3f).coerceAtMost(1f),
            blue = (MaterialTheme.colorScheme.primary.blue + 0.3f).coerceAtMost(1f)
        ),
        i = Icons.AutoMirrored.Outlined.PlaylistPlay
    )
    val actionQueueEnd = SwipeAction.QueueEnd(
        t = with(density) { 160.dp.toPx() },
        c = MaterialTheme.colorScheme.secondary.copy(
            alpha = 1f,
            red = (MaterialTheme.colorScheme.secondary.red + 0.3f).coerceAtMost(1f),
            green = (MaterialTheme.colorScheme.secondary.green + 0.3f).coerceAtMost(1f),
            blue = (MaterialTheme.colorScheme.secondary.blue + 0.3f).coerceAtMost(1f)
        ),
        i = Icons.AutoMirrored.Outlined.PlaylistAdd
    )

    val currentAction by remember {
        derivedStateOf {
            when {
                swipeOffset.floatValue >= actionQueueEnd.threshold -> actionQueueEnd
                swipeOffset.floatValue >= actionQueueNext.threshold -> actionQueueNext
                else -> SwipeAction.None
            }
        }
    }

    val draggableState = rememberDraggableState { delta ->
        swipeOffset.floatValue = (swipeOffset.floatValue + delta).coerceAtLeast(0f)
    }

    LaunchedEffect(currentAction) {
        if (currentAction != SwipeAction.None) {
            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
        }
    }

    val backgroundAlpha by remember {
        derivedStateOf {
            when (currentAction) {
                SwipeAction.None -> 0f
                is SwipeAction.QueueNext -> (swipeOffset.floatValue / actionQueueNext.threshold).coerceIn(0f, 0.6f)
                is SwipeAction.QueueEnd -> (swipeOffset.floatValue / actionQueueEnd.threshold).coerceIn(0f, 0.7f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .draggable(
                orientation = Orientation.Horizontal,
                state = draggableState,
                onDragStopped = {
                    when (currentAction) {
                        is SwipeAction.QueueEnd -> {
                            playerConnection?.enqueueEnd(item)
                            coroutineScope.launch {
                                snackbarHostState?.showSnackbar(
                                    message = context.getString(R.string.song_added_to_queue_end, item.mediaMetadata.title),
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Indefinite
                                )
                                delay(SNACKBAR_VERY_SHORT)
                                this.coroutineContext[Job]?.cancel()
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        }
                        is SwipeAction.QueueNext -> {
                            playerConnection?.enqueueNext(item)
                            coroutineScope.launch {
                                snackbarHostState?.showSnackbar(
                                    message = context.getString(R.string.song_added_to_queue, item.mediaMetadata.title),
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        }
                        SwipeAction.None -> {}
                    }
                    resetDrag(coroutineScope, swipeOffset)
                }
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = currentAction.color.copy(alpha = backgroundAlpha)
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val iconScale by animateFloatAsState(
                targetValue = if (currentAction != SwipeAction.None) 1.2f else 1f,
                label = "iconPop"
            )

            Icon(
                imageVector = currentAction.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.scale(iconScale)
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(swipeOffset.floatValue.roundToInt(), 0) }
                .fillMaxWidth(),
            content = content
        )
    }
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