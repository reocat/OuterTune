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
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.os.PowerManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.C
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.navigation.NavController
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap
import com.dd3boh.outertune.LocalImageCache
import com.dd3boh.outertune.LocalMenuState
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.constants.DEFAULT_PLAYER_BACKGROUND
import com.dd3boh.outertune.constants.DarkMode
import com.dd3boh.outertune.constants.DarkModeKey
import com.dd3boh.outertune.constants.PlayerBackgroundStyle
import com.dd3boh.outertune.constants.PlayerBackgroundStyleKey
import com.dd3boh.outertune.constants.PlayerButtonsStyle
import com.dd3boh.outertune.constants.PlayerButtonsStyleKey
import com.dd3boh.outertune.constants.PlayerHorizontalPadding
import com.dd3boh.outertune.constants.QueuePeekHeight
import com.dd3boh.outertune.constants.ShowLyricsKey
import com.dd3boh.outertune.constants.SliderStyle
import com.dd3boh.outertune.constants.SliderStyleKey
import com.dd3boh.outertune.constants.SwipeToSkip
import com.dd3boh.outertune.extensions.metadata
import com.dd3boh.outertune.extensions.tabMode
import com.dd3boh.outertune.extensions.togglePlayPause
import com.dd3boh.outertune.extensions.toggleRepeatMode
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.ui.component.AsyncImageLocal
import com.dd3boh.outertune.ui.component.BottomSheet
import com.dd3boh.outertune.ui.component.BottomSheetState
import com.dd3boh.outertune.ui.component.PlayerSliderTrack
import com.dd3boh.outertune.ui.component.button.ResizableIconButton
import com.dd3boh.outertune.ui.component.rememberBottomSheetState
import com.dd3boh.outertune.ui.menu.PlayerMenu
import com.dd3boh.outertune.ui.utils.SnapLayoutInfoProvider
import com.dd3boh.outertune.utils.makeTimeString
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.max

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val imageCache = LocalImageCache.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val context = LocalContext.current

    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.DEFAULT)

    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    val thumbnailLazyGridState = rememberLazyGridState()

    val swipeToSkip by rememberPreference(SwipeToSkip, defaultValue = true)
    val previousMediaMetadata = if (swipeToSkip && playerConnection.player.hasPreviousMediaItem()) {
        val previousIndex = playerConnection.player.currentMediaItemIndex - 1
        if (previousIndex >= 0) {
            playerConnection.player.getMediaItemAt(previousIndex).metadata
        } else null
    } else null

    val nextMediaMetadata = if (swipeToSkip && playerConnection.player.hasNextMediaItem()) {
        val nextIndex = playerConnection.player.currentMediaItemIndex + 1
        if (nextIndex < playerConnection.player.mediaItemCount) {
            playerConnection.player.getMediaItemAt(nextIndex).metadata
        } else null
    } else null

    val mediaItems = listOfNotNull(previousMediaMetadata, mediaMetadata, nextMediaMetadata)
    val currentMediaIndex = mediaItems.indexOf(mediaMetadata)

    val currentItem by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemIndex } }
    val itemScrollOffset by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemScrollOffset } }

    LaunchedEffect(itemScrollOffset) {
        if (!thumbnailLazyGridState.isScrollInProgress || !swipeToSkip || itemScrollOffset != 0 || currentMediaIndex < 0) return@LaunchedEffect

        if (currentItem > currentMediaIndex && canSkipNext) {
            playerConnection.player.seekToNext()
        } else if (currentItem < currentMediaIndex && canSkipPrevious) {
            playerConnection.player.seekToPreviousMediaItem()
        }
    }

    LaunchedEffect(mediaMetadata, canSkipPrevious, canSkipNext) {
        val index = max(0, currentMediaIndex)
        if (index >= 0) {
            try {
                if (state.isExpanded)
                    thumbnailLazyGridState.animateScrollToItem(index)
                else
                    thumbnailLazyGridState.scrollToItem(index)
            } catch (_: Exception) {
                thumbnailLazyGridState.scrollToItem(index)
            }
        }
    }

    LaunchedEffect(playerConnection.player.currentMediaItemIndex) {
        val index = mediaItems.indexOf(mediaMetadata)
        if (index >= 0 && index != currentItem) {
            thumbnailLazyGridState.scrollToItem(index)
        }
    }

    val horizontalLazyGridItemWidthFactor = 1f
    val thumbnailSnapLayoutInfoProvider = remember(thumbnailLazyGridState) {
        SnapLayoutInfoProvider(
            lazyGridState = thumbnailLazyGridState,
            positionInLayout = { layoutSize, itemSize ->
                (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
            }
        )
    }

    val playerBackground by rememberEnumPreference(
        key = PlayerBackgroundStyleKey,
        defaultValue = DEFAULT_PLAYER_BACKGROUND
    )

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val playerButtonsStyle by rememberEnumPreference(PlayerButtonsStyleKey, PlayerButtonsStyle.DEFAULT)

    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary

    val (textColor, controlIconColor, accentStyledColor, iconOnAccentColor) = remember(
        playerButtonsStyle,
        playerBackground,
        useDarkTheme,
        primaryContainerColor,
        primaryColor,
        onBackgroundColor,
        surfaceColor,
        onPrimaryColor
    ) {
        val accentValue = if (useDarkTheme) primaryContainerColor else primaryColor

        if (playerBackground == PlayerBackgroundStyle.FOLLOW_THEME && !useDarkTheme) {
            if (playerButtonsStyle == PlayerButtonsStyle.DEFAULT) {
                listOf(Color.Black, Color.Black, Color.Black, Color.White)
            } else {
                listOf(accentValue, accentValue, accentValue, onPrimaryColor)
            }
        } else if (playerButtonsStyle == PlayerButtonsStyle.SECONDARY) {
            listOf(Color.White, Color.White, accentValue, Color.White)
        } else {
            val defaultText = when (playerBackground) {
                PlayerBackgroundStyle.FOLLOW_THEME -> onBackgroundColor
                else -> Color.White
            }
            val defaultIconOnButton = when (playerBackground) {
                PlayerBackgroundStyle.FOLLOW_THEME -> surfaceColor
                else -> Color.Black
            }
            listOf(defaultText, defaultText, defaultText, defaultIconOnButton)
        }
    }

    val showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)

    var position by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.currentPosition)
    }
    var duration by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.duration)
    }
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }

    var gradientColors by remember {
        mutableStateOf<List<Color>>(emptyList())
    }

    val gradientColorsCache = remember { mutableMapOf<String, List<Color>>() }
    val defaultGradientColors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    LaunchedEffect(mediaMetadata?.id, playerBackground) {
        val currentMetadata = mediaMetadata
        gradientColors = when {
            playerBackground != PlayerBackgroundStyle.GRADIENT -> emptyList()
            currentMetadata?.thumbnailUrl == null -> emptyList()
            else -> fetchAndExtractGradientColors(
                context = context,
                metadata = currentMetadata,
                cache = gradientColorsCache,
                defaultColors = defaultGradientColors
            )
        }
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(500)
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
            }
        }
    }

    val dismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val queueSheetState = rememberBottomSheetState(
        dismissedBound = dismissedBound,
        expandedBound = state.expandedBound,
        collapsedBound = dismissedBound + 1.dp,
        initialAnchor = 1
    )

    BottomSheet(
        state = state,
        modifier = modifier,
        backgroundColor = if (useDarkTheme || playerBackground == PlayerBackgroundStyle.FOLLOW_THEME) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
        } else MaterialTheme.colorScheme.onSurfaceVariant,
        collapsedBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        onDismiss = {
            playerConnection.player.stop()
            playerConnection.player.clearMediaItems()
            playerConnection.service.deInitQueue()
        },
        collapsedContent = {
            MiniPlayer(
                position = position,
                duration = duration
            )
        }
    ) {
        val tabMode = context.tabMode()
        val controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit = { mediaMetadata ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = mediaMetadata.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .basicMarquee(iterations = 1, initialDelayMillis = 3000)
                            .clickable(enabled = mediaMetadata.album != null) {
                                navController.navigate("album/${mediaMetadata.album!!.id}")
                                state.collapseSoft()
                            }
                    )
                    Row {
                        mediaMetadata.artists.fastForEachIndexed { index, artist ->
                            Text(
                                text = artist.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .basicMarquee(iterations = 1, initialDelayMillis = 5000)
                                    .clickable(enabled = artist.id != null) {
                                        navController.navigate("artist/${artist.id}")
                                        state.collapseSoft()
                                    }
                            )
                            if (index != mediaMetadata.artists.lastIndex) {
                                Text(
                                    text = ", ",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    ResizableIconButton(
                        icon = if (currentSong?.song?.liked == true) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        color = if (currentSong?.song?.liked == true) MaterialTheme.colorScheme.error else controlIconColor,
                        modifier = Modifier.size(28.dp),
                        onClick = playerConnection::toggleLike
                    )
                    ResizableIconButton(
                        icon = Icons.Outlined.MoreVert,
                        color = controlIconColor,
                        modifier = Modifier.size(28.dp),
                        onClick = {
                            menuState.show {
                                PlayerMenu(
                                    mediaMetadata = mediaMetadata,
                                    navController = navController,
                                    playerBottomSheetState = state,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)) {
                when (sliderStyle) {
                    SliderStyle.DEFAULT -> Slider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = { sliderPosition = it.toLong() },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                playerConnection.player.seekTo(it)
                                position = it
                            }
                            sliderPosition = null
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = accentStyledColor,
                            activeTrackColor = accentStyledColor,
                            inactiveTrackColor = accentStyledColor.copy(alpha = 0.4f)
                        )
                    )
                    SliderStyle.SQUIGGLY -> SquigglySlider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = { sliderPosition = it.toLong() },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                playerConnection.player.seekTo(it)
                                position = it
                            }
                            sliderPosition = null
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        },
                        colors = SquigglySliderDefaults.colors(
                            thumbColor = accentStyledColor,
                            activeTrackColor = accentStyledColor,
                            inactiveTrackColor = accentStyledColor.copy(alpha = 0.4f)
                        ),
                        squigglesSpec = SquigglySlider.SquigglesSpec(
                            amplitude = if (isPlaying) 2.dp else 0.dp,
                            strokeWidth = 4.dp,
                        )
                    )
                    SliderStyle.SLIM -> Slider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = { sliderPosition = it.toLong() },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                playerConnection.player.seekTo(it)
                                position = it
                            }
                            sliderPosition = null
                        },
                        thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                        track = { sliderState ->
                            PlayerSliderTrack(
                                sliderState = sliderState,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = accentStyledColor,
                                    inactiveTrackColor = accentStyledColor.copy(alpha = 0.4f)
                                )
                            )
                        }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = makeTimeString(sliderPosition ?: position),
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }


            Spacer(Modifier.height(24.dp))

            val playPauseRoundness by animateDpAsState(
                targetValue = if (isPlaying) 32.dp else 24.dp,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "playPauseRoundness"
            )
            val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ResizableIconButton(
                        icon = Icons.Outlined.Shuffle,
                        modifier = Modifier
                            .size(32.dp)
                            .alpha(if (shuffleModeEnabled) 1f else 0.5f),
                        color = controlIconColor,
                        onClick = {
                            playerConnection.triggerShuffle()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ResizableIconButton(
                        icon = Icons.Outlined.SkipPrevious,
                        enabled = canSkipPrevious,
                        modifier = Modifier.size(40.dp),
                        color = controlIconColor,
                        onClick = {
                            playerConnection.player.seekToPrevious()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        }
                    )
                }
                Spacer(Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(playPauseRoundness))
                        .background(accentStyledColor)
                        .clickable {
                            if (playbackState == STATE_ENDED) {
                                playerConnection.player.seekTo(0, 0)
                                playerConnection.player.playWhenReady = true
                            } else {
                                playerConnection.player.togglePlayPause()
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = if (playbackState == STATE_ENDED) Icons.Outlined.Replay else if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = "Play/Pause",
                        colorFilter = ColorFilter.tint(iconOnAccentColor),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ResizableIconButton(
                        icon = Icons.Outlined.SkipNext,
                        enabled = canSkipNext,
                        modifier = Modifier.size(40.dp),
                        color = controlIconColor,
                        onClick = {
                            playerConnection.player.seekToNext()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ResizableIconButton(
                        icon = when (repeatMode) {
                            REPEAT_MODE_OFF, REPEAT_MODE_ALL -> Icons.Outlined.Repeat
                            REPEAT_MODE_ONE -> Icons.Outlined.RepeatOne
                            else -> Icons.Outlined.Repeat
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .alpha(if (repeatMode == REPEAT_MODE_OFF) 0.5f else 1f),
                        color = controlIconColor,
                        onClick = {
                            playerConnection.player.toggleRepeatMode()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        }
                    )
                }
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = !powerManager.isPowerSaveMode && state.isExpanded,
                enter = fadeIn(tween(500)),
                exit = fadeOut(tween(500))
            ) {
                AnimatedContent(
                    targetState = mediaMetadata,
                    transitionSpec = { fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000))) },
                    label = "playerBackground"
                ) { metadata ->
                    if (playerBackground == PlayerBackgroundStyle.BLUR) {
                        val scrimColorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.3f), BlendMode.SrcOver)
                        if (metadata?.isLocal == true) {
                            AsyncImageLocal(
                                image = { imageCache.getLocalThumbnail(metadata.localPath, false) },
                                contentScale = ContentScale.FillBounds,
                                colorFilter = scrimColorFilter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(200.dp)
                            )
                        } else {
                            AsyncImage(
                                model = metadata?.thumbnailUrl,
                                contentDescription = "Blurred Album Art",
                                contentScale = ContentScale.FillBounds,
                                colorFilter = scrimColorFilter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(200.dp)
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = gradientColors,
                    transitionSpec = { fadeIn(tween(1000)) togetherWith fadeOut(tween(1000)) },
                    label = "playerGradient"
                ) { colors ->
                    if (playerBackground == PlayerBackgroundStyle.GRADIENT && colors.size >= 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(colors))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !powerManager.isPowerSaveMode && state.isExpanded && isPlaying && playerBackground == PlayerBackgroundStyle.GRADIENT && gradientColors.isNotEmpty(),
                    enter = fadeIn(tween(1000)),
                    exit = fadeOut(tween(1000))
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
                    val shimmerTranslate by infiniteTransition.animateFloat(
                        initialValue = -1f,
                        targetValue = 2f,
                        animationSpec = infiniteRepeatable(animation = tween(4000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
                        label = "shimmerTranslate"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.1f), Color.Transparent),
                                    start = Offset(0f, (shimmerTranslate - 0.5f) * 2000),
                                    end = Offset(2000f, shimmerTranslate * 2000)
                                )
                            )
                    )
                }

                if (playerBackground != PlayerBackgroundStyle.FOLLOW_THEME && showLyrics) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                }
            }

            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && !tabMode) {
                val vPadding = max(
                    WindowInsets.safeDrawing.getTop(LocalDensity.current),
                    WindowInsets.safeDrawing.getBottom(LocalDensity.current)
                )
                val vPaddingDp = with(LocalDensity.current) { vPadding.toDp() }
                val verticalInsets = WindowInsets(left = 0.dp, top = vPaddingDp, right = 0.dp, bottom = vPaddingDp)
                Row(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).add(verticalInsets))
                        .fillMaxSize()
                ) {
                    BoxWithConstraints(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .nestedScroll(state.preUpPostDownNestedScrollConnection)
                    ) {
                        val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
                        LazyHorizontalGrid(
                            state = thumbnailLazyGridState,
                            rows = GridCells.Fixed(1),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                            userScrollEnabled = state.isExpanded && swipeToSkip
                        ) {
                            items(items = mediaItems, key = { it.id }) {
                                Thumbnail(
                                    sliderPositionProvider = { sliderPosition },
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .animateContentSize(),
                                    contentScale = ContentScale.Crop,
                                    showLyricsOnClick = true,
                                    customMediaMetadata = it
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(if (showLyrics) 0.35f else 1f, false)
                            .animateContentSize()
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                            .padding(vertical = if (showLyrics) 8.dp else 16.dp)
                    ) {
                        Spacer(Modifier.weight(1f))
                        mediaMetadata?.let { controlsContent(it) }
                        Spacer(Modifier.weight(1f))
                    }
                }
            } else {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                        .padding(bottom = queueSheetState.collapsedBound)
                ) {

                    BoxWithConstraints(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .nestedScroll(state.preUpPostDownNestedScrollConnection)
                    ) {
                        val topPadding = WindowInsets.systemBars.asPaddingValues(LocalDensity.current).calculateTopPadding() + 24.dp
                        LazyHorizontalGrid(
                            state = thumbnailLazyGridState,
                            rows = GridCells.Fixed(1),
                            modifier = Modifier.padding(top = topPadding),
                            flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                            userScrollEnabled = swipeToSkip && state.isExpanded
                        ) {
                            items(items = mediaItems, key = { it.id }) {
                                Thumbnail(
                                    modifier = Modifier
                                        .width(maxWidth)
                                        .animateContentSize(),
                                    contentScale = ContentScale.Crop,
                                    sliderPositionProvider = { sliderPosition },
                                    showLyricsOnClick = true,
                                    customMediaMetadata = it
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(if (showLyrics) 12.dp else 24.dp))

                    mediaMetadata?.let {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = if (showLyrics) 12.dp else 24.dp)
                        ) {
                            controlsContent(it)
                        }
                    }
                }
            }

            QueueSheet(
                state = queueSheetState,
                playerBottomSheetState = state,
                onTerminate = {
                    state.dismiss()
                    playerConnection.service.queueBoard.detachedHead = false
                },
                onBackgroundColor = textColor,
                navController = navController
            )
        }
    }
}

/**
 * Fetches an image from the provided metadata, extracts a gradient color palette,
 * and caches the result.
 *
 * @param context The current context.
 * @param metadata The media metadata containing the thumbnail URL.
 * @param cache A mutable map to store and retrieve cached color lists.
 * @param defaultColors A list of colors to return in case of any failure.
 * @return A list of [Color] objects for the gradient.
 */
private suspend fun fetchAndExtractGradientColors(
    context: Context,
    metadata: MediaMetadata,
    cache: MutableMap<String, List<Color>>,
    defaultColors: List<Color>
): List<Color> {
    cache[metadata.id]?.let { return it }

    return try {
        val request = ImageRequest.Builder(context)
            .data(metadata.thumbnailUrl)
            .size(Size(200, 200))
            .allowHardware(false)
            .build()

        val bitmap = withContext(Dispatchers.IO) {
            val drawable = context.imageLoader.execute(request).image
            when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                else -> drawable?.toBitmap()
            }
        }

        if (bitmap != null) {
            val palette = withContext(Dispatchers.Default) {
                Palette.from(bitmap).maximumColorCount(16).generate()
            }

            val dominantColor = palette.dominantSwatch?.rgb?.let { Color(it) }

            fun isColorDark(color: Color): Boolean {
                val yiq = ((color.red * 255) * 299 + (color.green * 255) * 587 + (color.blue * 255) * 114) / 1000
                return yiq < 128
            }

            val newColors = if (dominantColor != null) {
                if (isColorDark(dominantColor)) {
                    listOf(
                        dominantColor,
                        Color(
                            red = (dominantColor.red + 0.2f).coerceAtMost(1f),
                            green = (dominantColor.green + 0.2f).coerceAtMost(1f),
                            blue = (dominantColor.blue + 0.2f).coerceAtMost(1f),
                            alpha = dominantColor.alpha
                        )
                    )
                } else {
                    listOf(
                        dominantColor,
                        Color(
                            red = (dominantColor.red - 0.2f).coerceAtLeast(0f),
                            green = (dominantColor.green - 0.2f).coerceAtLeast(0f),
                            blue = (dominantColor.blue - 0.2f).coerceAtLeast(0f),
                            alpha = dominantColor.alpha
                        )
                    )
                }
            } else {
                defaultColors
            }

            withContext(Dispatchers.Main) {
                cache[metadata.id] = newColors
            }
            newColors
        } else {
            defaultColors
        }
    } catch (e: Exception) {
        defaultColors
    }
}
