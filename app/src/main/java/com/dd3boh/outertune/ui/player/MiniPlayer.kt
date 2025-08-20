/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.player

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.dd3boh.outertune.LocalImageCache
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.MiniPlayerHeight
import com.dd3boh.outertune.constants.SwipeSensitivityKey
import com.dd3boh.outertune.constants.SwipeToSkip
import com.dd3boh.outertune.constants.ThumbnailCornerRadius
import com.dd3boh.outertune.extensions.togglePlayPause
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.ui.component.AsyncImageLocal
import com.dd3boh.outertune.ui.component.button.IconButton
import com.dd3boh.outertune.ui.theme.isPureBlackEnabled
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun MiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
) {
    val pureBlack = isPureBlackEnabled()
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val swipeToSkip by rememberPreference(SwipeToSkip, defaultValue = true)
    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val layoutDirection = LocalLayoutDirection.current

    val offsetXAnimatable = remember { Animatable(0f) }
    var dragStartTime by remember { mutableLongStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy, 
        stiffness = Spring.StiffnessMedium
    )
    
    fun calculateAutoSwipeThreshold(swipeSensitivity: Float): Int {
        return (500 / (1f + kotlin.math.exp(-(-10.0 * swipeSensitivity + 8.0)))).roundToInt()
    }
    val autoSwipeThreshold = calculateAutoSwipeThreshold(swipeSensitivity)

    val swipeIndicatorScale by animateFloatAsState(
        targetValue = when {
            offsetXAnimatable.value.absoluteValue > autoSwipeThreshold * 0.8f -> 1.3f
            offsetXAnimatable.value.absoluteValue > 80f -> 1.2f
            offsetXAnimatable.value.absoluteValue > 50f -> 1.1f
            else -> 0.9f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "swipe_indicator_scale"
    )

    val swipeIndicatorAlpha by animateFloatAsState(
        targetValue = (offsetXAnimatable.value.absoluteValue / 120f).coerceIn(0f, 1f),
        animationSpec = tween(100),
        label = "swipe_indicator_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pureBlack)
                Color.Black.copy(alpha = 0.95f)
            else
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .let { baseModifier ->
                    if (swipeToSkip) {
                        baseModifier.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    dragStartTime = System.currentTimeMillis()
                                    totalDragDistance = 0f
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        offsetXAnimatable.animateTo(
                                            targetValue = 0f,
                                            animationSpec = animationSpec
                                        )
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    val adjustedDragAmount =
                                        if (layoutDirection == LayoutDirection.Rtl) -dragAmount else dragAmount
                                    val canSkipPreviousCheck = playerConnection.player.previousMediaItemIndex != -1
                                    val canSkipNextCheck = playerConnection.player.nextMediaItemIndex != -1
                                    val currentOffset = offsetXAnimatable.value
                                    
                                    val isRetractingLeft = currentOffset < 0 && adjustedDragAmount > 0
                                    val isRetractingRight = currentOffset > 0 && adjustedDragAmount < 0
                                    val isMovingLeft = adjustedDragAmount < 0 && canSkipNextCheck
                                    val isMovingRight = adjustedDragAmount > 0 && canSkipPreviousCheck

                                    val allowMovement = isRetractingLeft || isRetractingRight || isMovingLeft || isMovingRight

                                    if (allowMovement) {
                                        totalDragDistance += kotlin.math.abs(adjustedDragAmount)
                                        coroutineScope.launch {
                                            val newOffset = currentOffset + adjustedDragAmount

                                            val resistance = when {
                                                (currentOffset < 0 && adjustedDragAmount > 0) -> 1f
                                                (currentOffset > 0 && adjustedDragAmount < 0) -> 1f
                                                kotlin.math.abs(newOffset) > 150 -> 0.4f
                                                kotlin.math.abs(newOffset) > 100 -> 0.7f
                                                else -> 1f
                                            }
                                            offsetXAnimatable.snapTo(currentOffset + (adjustedDragAmount * resistance))
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val dragDuration = System.currentTimeMillis() - dragStartTime
                                    val velocity = if (dragDuration > 0) totalDragDistance / dragDuration else 0f
                                    val currentOffset = offsetXAnimatable.value

                                    val minDistanceThreshold = 80f
                                    val velocityThreshold = (swipeSensitivity * -8.25f) + 8.5f
                                    
                                    val reachedAutoThreshold = kotlin.math.abs(currentOffset) > autoSwipeThreshold
                                    val hasCommittedSwipe = kotlin.math.abs(currentOffset) > minDistanceThreshold &&
                                            velocity > velocityThreshold

                                    val shouldChangeSong = reachedAutoThreshold || hasCommittedSwipe

                                    if (shouldChangeSong) {
                                        val isRightSwipe = currentOffset > 0

                                        if (isRightSwipe && canSkipPrevious) {
                                            playerConnection.player.seekToPreviousMediaItem()
                                        } else if (!isRightSwipe && canSkipNext) {
                                            playerConnection.player.seekToNext()
                                        }
                                    }

                                    coroutineScope.launch {
                                        offsetXAnimatable.animateTo(
                                            targetValue = 0f,
                                            animationSpec = animationSpec
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        baseModifier
                    }
                }
        ) {
            LinearProgressIndicator(
                progress = { (position.toFloat() / duration).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round,
                drawStopIndicator = { }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.systemBars
                            .only(WindowInsetsSides.Horizontal)
                            .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                    )
                    .fillMaxSize()
                    .offset { IntOffset(offsetXAnimatable.value.roundToInt(), 0) }
                    .padding(horizontal = 12.dp, vertical = 8.dp), // Enhanced padding
            ) {
                Box(Modifier.weight(1f)) {
                    mediaMetadata?.let {
                        MiniMediaInfo(
                            mediaMetadata = it,
                            error = error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                ControlsButton(
                    onClick = {
                        if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.player.togglePlayPause()
                        }
                    },
                    isPlaying = isPlaying,
                    playbackState = playbackState
                )

                ControlsButton(
                    onClick = playerConnection.player::seekToNext,
                    enabled = canSkipNext,
                    icon = Icons.Outlined.SkipNext,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = offsetXAnimatable.value.absoluteValue > 50f,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(
                    if (offsetXAnimatable.value > 0) Alignment.CenterStart else Alignment.CenterEnd
                )
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            shape = CircleShape
                        )
                        .scale(swipeIndicatorScale)
                        .alpha(swipeIndicatorAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            if (offsetXAnimatable.value > 0) R.drawable.skip_previous else R.drawable.skip_next
                        ),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPlaying: Boolean = false,
    playbackState: Int = Player.STATE_IDLE,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isPlaying && icon == null) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .scale(buttonScale)
            .size(44.dp) // Slightly larger for better touch targets
    ) {
        Icon(
            imageVector = icon ?: run {
                when {
                    playbackState == Player.STATE_ENDED -> Icons.Outlined.Replay
                    isPlaying -> Icons.Outlined.Pause
                    else -> Icons.Outlined.PlayArrow
                }
            },
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MiniMediaInfo(
    mediaMetadata: MediaMetadata,
    error: PlaybackException?,
    modifier: Modifier = Modifier,
) {
    val imageCache = LocalImageCache.current
    val playerConnection = LocalPlayerConnection.current
    val isWaitingForNetwork by playerConnection?.waitingForNetworkConnection?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .padding(end = 12.dp)
                .size(52.dp),
            shape = RoundedCornerShape(ThumbnailCornerRadius * 1.2f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                var isRectangularImage by remember { mutableStateOf(false) }

                if (mediaMetadata.isLocal) {
                    AsyncImageLocal(
                        image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, true) },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(ThumbnailCornerRadius))
                    )
                } else {
                    AsyncImage(
                        model = mediaMetadata.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onSuccess = { success ->
                            val width = success.result.image.width
                            val height = success.result.image.height
                            isRectangularImage = width.toFloat() / height != 1f
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(ThumbnailCornerRadius))
                    )
                }

                if (isRectangularImage) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.OndemandVideo,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = error != null || isWaitingForNetwork,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                shape = RoundedCornerShape(ThumbnailCornerRadius)
                            )
                            .blur(radius = if (error != null) 0.dp else 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isWaitingForNetwork) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                strokeCap = StrokeCap.Round
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = mediaMetadata.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = mediaMetadata.artists.joinToString { it.name },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}