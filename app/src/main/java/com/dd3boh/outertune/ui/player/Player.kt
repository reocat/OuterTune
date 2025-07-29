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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.alpha
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
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
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
import com.dd3boh.outertune.ui.component.PlayerIconButton
import com.dd3boh.outertune.ui.component.SquigglySlider
import com.dd3boh.outertune.ui.component.SquigglySliderDefaults
import com.dd3boh.outertune.ui.component.rememberBottomSheetState
import com.dd3boh.outertune.ui.menu.PlayerMenu
import com.dd3boh.outertune.ui.theme.darken
import com.dd3boh.outertune.ui.theme.lighten
import com.dd3boh.outertune.ui.utils.SnapLayoutInfoProvider
import com.dd3boh.outertune.utils.makeTimeString
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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

    val (textColor, controlIconColor, accentStyledColor, iconOnAccentColor) = remember(
        playerButtonsStyle,
        playerBackground,
        useDarkTheme,
        primaryContainerColor,
        primaryColor,
        onBackgroundColor,
        surfaceColor
    ) {
        val accentValue = if (useDarkTheme) primaryContainerColor else primaryColor

        if (playerButtonsStyle == PlayerButtonsStyle.SECONDARY) {
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

    // Cache for gradient colors to prevent re-extraction for the same song
    val gradientColorsCache = remember { mutableMapOf<String, List<Color>>() }

    // Default gradient colors for fallback
    val defaultGradientColors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager


    // gradient colours
    LaunchedEffect(mediaMetadata?.id, playerBackground) {
        if (useDarkTheme && playerBackground != PlayerBackgroundStyle.BLUR) {
            gradientColors = listOf(Color.Black, Color.Black)
        } else if (playerBackground == PlayerBackgroundStyle.GRADIENT) {
            val currentMetadata = mediaMetadata
            if (currentMetadata != null && currentMetadata.thumbnailUrl != null) {
                // Check cache first
                val cachedColors = gradientColorsCache[currentMetadata.id]
                if (cachedColors != null) {
                    gradientColors = cachedColors
                } else {
                    try {
                        val request = ImageRequest.Builder(context)
                            .data(currentMetadata.thumbnailUrl)
                            .size(Size(200, 200)) // Larger size for better color extraction
                            .allowHardware(false)
                            .build()

                        val drawable = context.imageLoader.execute(request).drawable
                        val bitmap = when (drawable) {
                            is BitmapDrawable -> drawable.bitmap
                            else -> drawable?.toBitmap()
                        }

                        if (bitmap != null) {
                            val palette = Palette.from(bitmap).maximumColorCount(16).generate()

                            val dominantColor = palette.dominantSwatch?.rgb?.let { Color(it) }
                            val mutedColor = palette.mutedSwatch?.rgb?.let { Color(it) }
                            val lightMutedColor = palette.lightMutedSwatch?.rgb?.let { Color(it) }

                            val colors = buildList<Color> {
                                dominantColor?.let { add(it) }
                                mutedColor?.let { add(it) }
                                lightMutedColor?.let { add(it) }
                            }.takeIf { it.isNotEmpty() } ?: defaultGradientColors

                            gradientColors = colors
                            gradientColorsCache[currentMetadata.id] = colors
                        } else {
                            gradientColors = defaultGradientColors
                        }
                    } catch (e: Exception) {
                        gradientColors = defaultGradientColors
                    }
                }
            } else {
                gradientColors = emptyList()
            }
        } else {
            gradientColors = emptyList()
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
        } else MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
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

        val actionButtons: @Composable RowScope.() -> Unit = {
            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .offset(y = 5.dp)
                    .size(36.dp)
            ) {
                PlayerIconButton(style = playerButtonsStyle,
                    icon = if (currentSong?.song?.liked == true) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    color = if (currentSong?.song?.liked == true) MaterialTheme.colorScheme.error else controlIconColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp),
                    onClick = playerConnection::toggleLike
                )
            }

            Spacer(modifier = Modifier.width(7.dp))

            Box(
                modifier = Modifier
                    .offset(y = 5.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(accentStyledColor)
            ) {
                PlayerIconButton(style = playerButtonsStyle,
                    icon = Icons.Outlined.MoreVert,
                    color = iconOnAccentColor,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
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
        val controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit = { mediaMetadata ->
            val playPauseRoundness by animateDpAsState(
                targetValue = if (isPlaying) 24.dp else 36.dp,
                animationSpec = tween(durationMillis = 100, easing = LinearEasing),
                label = "playPauseRoundness"
            )

            // action buttons for landscape (above title)
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && !tabMode) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = PlayerHorizontalPadding,
                            end = PlayerHorizontalPadding,
                            bottom = 16.dp
                        )
                ) {
                    actionButtons()
                }
            }

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mediaMetadata.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .basicMarquee(
                                    iterations = 1,
                                    initialDelayMillis = 3000
                                )
                                .clickable(enabled = mediaMetadata.album != null) {
                                    navController.navigate("album/${mediaMetadata.album!!.id}")
                                    state.collapseSoft()
                                }
                        )

                        Row {
                            mediaMetadata.artists.fastForEachIndexed { index, artist ->
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .basicMarquee(
                                            iterations = 1,
                                            initialDelayMillis = 5000
                                        )
                                        .clickable(enabled = artist.id != null) {
                                            navController.navigate("artist/${artist.id}")
                                            state.collapseSoft()
                                        }
                                )

                                if (index != mediaMetadata.artists.lastIndex) {
                                    Text(
                                        text = ", ",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }

                    // action buttons for portrait (inline with title)
                    if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE && !tabMode) {
                        actionButtons()
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            when (sliderStyle) {
                SliderStyle.DEFAULT -> {
                    Slider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            sliderPosition = it.toLong()
                        },
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
                        ),
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                    )
                }
                SliderStyle.SQUIGGLY -> {
                    SquigglySlider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            sliderPosition = it.toLong()
                        },
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
                        ),
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)
                    )
                }
                SliderStyle.SLIM -> {
                    Slider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            sliderPosition = it.toLong()
                        },
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
                        },
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding + 4.dp)
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: position),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()

                Box(modifier = Modifier.weight(1f)) {
                    PlayerIconButton(style = playerButtonsStyle,
                        icon = Icons.Outlined.Shuffle,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                            .align(Alignment.Center)
                            .alpha(if (shuffleModeEnabled) 1f else 0.5f),
                        color = controlIconColor,
                        onClick = {
                            playerConnection.triggerShuffle()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    PlayerIconButton(style = playerButtonsStyle,
                        icon = Icons.Outlined.SkipPrevious,
                        enabled = canSkipPrevious,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        color = controlIconColor,
                        onClick = {
                            playerConnection.player.seekToPrevious()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        }
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(if (showLyrics) 56.dp else 72.dp)
                        .animateContentSize()
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
                        }
                ) {
                    Image(
                        imageVector = if (playbackState == STATE_ENDED) Icons.Outlined.Replay else if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(iconOnAccentColor),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    PlayerIconButton(style = playerButtonsStyle,
                        icon = Icons.Outlined.SkipNext,
                        enabled = canSkipNext,
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center),
                        color = controlIconColor,
                        onClick = {
                            playerConnection.player.seekToNext()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    PlayerIconButton(style = playerButtonsStyle,
                        icon = when (repeatMode) {
                            REPEAT_MODE_OFF, REPEAT_MODE_ALL -> Icons.Outlined.Repeat
                            REPEAT_MODE_ONE -> Icons.Outlined.RepeatOne
                            else -> throw IllegalStateException()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                            .align(Alignment.Center)
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
                    transitionSpec = {
                        fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000)))
                    },
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
                                    .alpha(1f)
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
                                    .alpha(1f)
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = gradientColors,
                    transitionSpec = {
                        fadeIn(tween(1000)) togetherWith fadeOut(tween(1000))
                    },
                    label = "playerGradient"
                ) { colors ->
                    if (playerBackground == PlayerBackgroundStyle.GRADIENT && colors.size >= 2) {
                        val gradientBrush = remember(colors, useDarkTheme) {
                            val topColor = colors[0]
                            val bottomColor = colors.last()

                            val finalTopColor = if (useDarkTheme) topColor.darken(0.2f) else topColor.lighten(0.1f)
                            val finalBottomColor = if (useDarkTheme) bottomColor.darken(0.4f) else bottomColor.darken(0.2f)

                            Brush.verticalGradient(
                                colors = listOf(
                                    finalTopColor.copy(alpha = 0.85f),
                                    Color.Transparent,
                                    finalBottomColor.copy(alpha = 0.95f)
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(gradientBrush)
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
                        animationSpec = infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "shimmerTranslate"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.1f),
                                        Color.Transparent,
                                    ),
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
                            .background(Color.Black.copy(alpha = 0.3f))
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
                            items(
                                items = mediaItems,
                                key = { it.id }
                            ) {
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
                            // "percentage to half width", not "percentage of width"
                            .weight(if (showLyrics) 0.65f else 1f, false)
                            .animateContentSize()
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                    ) {
                        Spacer(Modifier.weight(1f))

                        mediaMetadata?.let {
                            controlsContent(it)
                        }

                        Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                        .padding(bottom = queueSheetState.collapsedBound)
                ) {
                    BoxWithConstraints(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .nestedScroll(state.preUpPostDownNestedScrollConnection)
                    ) {
                        val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor

                        LazyHorizontalGrid(
                            state = thumbnailLazyGridState,
                            rows = GridCells.Fixed(1),
                            flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                            userScrollEnabled = swipeToSkip && state.isExpanded
                        ) {
                            items(
                                items = mediaItems,
                                key = { it.id }
                            ) {
                                Thumbnail(
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .animateContentSize(),
                                    contentScale = ContentScale.Crop,
                                    sliderPositionProvider = { sliderPosition },
                                    showLyricsOnClick = true,
                                    customMediaMetadata = it
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    mediaMetadata?.let {
                        controlsContent(it)
                    }

                    Spacer(Modifier.height(24.dp))
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