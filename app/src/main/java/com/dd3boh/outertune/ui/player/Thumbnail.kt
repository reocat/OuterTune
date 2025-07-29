/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dd3boh.outertune.LocalImageCache
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.constants.PlayerHorizontalPadding
import com.dd3boh.outertune.constants.ShowLyricsKey
import com.dd3boh.outertune.constants.SwipeSensitivityKey
import com.dd3boh.outertune.constants.SwipeToSkip
import com.dd3boh.outertune.constants.ThumbnailCornerRadius
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.ui.component.AsyncImageLocal
import com.dd3boh.outertune.ui.component.Lyrics
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Thumbnail(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
    showLyricsOnClick: Boolean = false,
    contentScale: ContentScale = ContentScale.Fit,
    customMediaMetadata: MediaMetadata? = null
) {
    val haptic = LocalHapticFeedback.current
    val imageCache = LocalImageCache.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val currentView = LocalView.current
    val playerMediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val error by playerConnection.error.collectAsState()

    val mediaMetadata = customMediaMetadata ?: playerMediaMetadata

    var showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)
    val swipeToSkip by rememberPreference(SwipeToSkip, defaultValue = true)
    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)

    val coroutineScope = rememberCoroutineScope()
    val offsetXAnimatable = remember { Animatable(0f) }

    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    DisposableEffect(showLyrics) {
        currentView.keepScreenOn = showLyrics
        onDispose {
            currentView.keepScreenOn = false
        }
    }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var isPreviewingNextSong by remember { mutableStateOf(false) }
    var previewImage by remember { mutableStateOf<String?>(null) }
    var dragStartTime by remember { mutableStateOf(0L) }
    var totalDragDistance by remember { mutableFloatStateOf(0f) }

    var displayedThumbnailUrl by remember { mutableStateOf(mediaMetadata?.thumbnailUrl) }
    var nextThumbnailUrl by remember { mutableStateOf<String?>(null) }
    var isAnimatingTransition by remember { mutableStateOf(false) }
    var lastMediaId by remember { mutableStateOf(mediaMetadata?.id) }
    var lastQueueIndex by remember { mutableIntStateOf(playerConnection.player.currentMediaItemIndex) }
    var swipeTriggeredChange by remember { mutableStateOf(false) }
    var animationDirection by remember { mutableStateOf(true) }

    LaunchedEffect(mediaMetadata?.id) {
        val currentMediaId = mediaMetadata?.id
        val currentQueueIndex = playerConnection.player.currentMediaItemIndex

        if (lastMediaId != null && currentMediaId != null && lastMediaId != currentMediaId) {
            if (!swipeTriggeredChange && !isAnimatingTransition) {
                isAnimatingTransition = true
                nextThumbnailUrl = mediaMetadata.thumbnailUrl
                animationDirection = if (currentQueueIndex > lastQueueIndex) {
                    false
                } else if (currentQueueIndex < lastQueueIndex) {
                    true
                } else {
                    true
                }

                val targetX = if (animationDirection) currentView.width.toFloat() else -currentView.width.toFloat()

                coroutineScope.launch {
                    offsetXAnimatable.snapTo(0f)
                    offsetX = 0f
                    offsetXAnimatable.animateTo(
                        targetValue = targetX,
                        animationSpec = animationSpec
                    )
                    displayedThumbnailUrl = nextThumbnailUrl
                    nextThumbnailUrl = null
                    offsetXAnimatable.snapTo(0f)
                    offsetX = 0f
                    isAnimatingTransition = false
                }
            } else if (swipeTriggeredChange) {
                swipeTriggeredChange = false
            } else if (isAnimatingTransition) {
                nextThumbnailUrl = mediaMetadata.thumbnailUrl
            }
        } else if (lastMediaId == null) {
            displayedThumbnailUrl = mediaMetadata?.thumbnailUrl
        }
        lastMediaId = currentMediaId
        lastQueueIndex = currentQueueIndex
    }

    val layoutDirection = LocalLayoutDirection.current

    fun updateImagePreview(offsetX: Float) {
        val threshold = 100f
        when {
            offsetX > threshold -> {
                isPreviewingNextSong = true
                previewImage =
                    playerConnection.player.previousMediaItemIndex.takeIf { it != -1 }?.let {
                        playerConnection.player.getMediaItemAt(it).mediaMetadata.artworkUri?.toString()
                    }
            }
            offsetX < -threshold -> {
                isPreviewingNextSong = true
                previewImage = playerConnection.player.nextMediaItemIndex.takeIf { it != -1 }?.let {
                    playerConnection.player.getMediaItemAt(it).mediaMetadata.artworkUri?.toString()
                }
            }
            else -> {
                isPreviewingNextSong = false
                previewImage = null
            }
        }
    }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = !showLyrics && error == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
        ) {
            var isRectangularImage by remember { mutableStateOf(false) }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                // Expressive: Larger, more rounded, shadowed album art
                val shape = RoundedCornerShape(32.dp)
                val elevation = 12.dp
                val borderColor = Color.Black.copy(alpha = 0.08f)
                val borderWidth = 2.dp
                val albumArtModifier = Modifier
                    .aspectRatio(1f)
                    .shadow(elevation, shape)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .then(Modifier)
                if (mediaMetadata?.thumbnailUrl != null) {
                    AsyncImage(
                        model = mediaMetadata.thumbnailUrl,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = albumArtModifier
                    )
                } else {
                    Box(
                        modifier = albumArtModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.OndemandVideo,
                            contentDescription = "No Album Art",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showLyrics && error == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Lyrics(sliderPositionProvider = sliderPositionProvider)
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .padding(32.dp)
                .align(Alignment.Center)
                .fillMaxSize()
        ) {
            error?.let { error ->
                PlaybackError(
                    error = error,
                    retry = playerConnection.player::prepare
                )
            }
        }
    }
}