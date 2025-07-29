/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.player

import android.R.attr.scaleX
import android.R.attr.scaleY
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
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.dd3boh.outertune.ui.component.ResizableIconButton
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

    // Enhanced Material 3 Expressive colors
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val tertiaryContainerColor = MaterialTheme.colorScheme.tertiaryContainer

    val (textColor, controlIconColor, accentStyledColor, iconOnAccentColor, secondaryAccentColor) = remember(
        playerButtonsStyle,
        playerBackground,
        useDarkTheme,
        primaryContainerColor,
        primaryColor,
        onBackgroundColor,
        surfaceColor,
        tertiaryColor,
        tertiaryContainerColor
    ) {
        val accentValue = if (useDarkTheme) primaryContainerColor else primaryColor
        val secondaryAccent = if (useDarkTheme) tertiaryContainerColor else tertiaryColor

        if (playerButtonsStyle == PlayerButtonsStyle.SECONDARY) {
            listOf(Color.White, Color.White, accentValue, Color.White, secondaryAccent)
        } else {
            val defaultText = when (playerBackground) {
                PlayerBackgroundStyle.FOLLOW_THEME -> onBackgroundColor
                else -> Color.White
            }
            val defaultIconOnButton = when (playerBackground) {
                PlayerBackgroundStyle.FOLLOW_THEME -> surfaceColor
                else -> Color.Black
            }
            listOf(defaultText, defaultText, defaultText, defaultIconOnButton, secondaryAccent)
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

    // Enhanced gradient colors with more sophisticated extraction
    LaunchedEffect(mediaMetadata?.id, playerBackground) {
        if (useDarkTheme && playerBackground != PlayerBackgroundStyle.BLUR) {
            gradientColors = listOf(Color.Black, Color.Black)
        } else if (playerBackground == PlayerBackgroundStyle.GRADIENT) {
            val currentMetadata = mediaMetadata
            if (currentMetadata != null && currentMetadata.thumbnailUrl != null) {
                val cachedColors = gradientColorsCache[currentMetadata.id]
                if (cachedColors != null) {
                    gradientColors = cachedColors
                } else {
                    try {
                        val request = ImageRequest.Builder(context)
                            .data(currentMetadata.thumbnailUrl)
                            .size(Size(300, 300))
                            .allowHardware(false)
                            .build()

                        val drawable = context.imageLoader.execute(request).drawable
                        val bitmap = when (drawable) {
                            is BitmapDrawable -> drawable.bitmap
                            else -> drawable?.toBitmap()
                        }

                        if (bitmap != null) {
                            val palette = Palette.from(bitmap).maximumColorCount(24).generate()

                            val vibrantColor = palette.vibrantSwatch?.rgb?.let { Color(it) }
                            val dominantColor = palette.dominantSwatch?.rgb?.let { Color(it) }
                            val mutedColor = palette.mutedSwatch?.rgb?.let { Color(it) }
                            val darkVibrantColor = palette.darkVibrantSwatch?.rgb?.let { Color(it) }
                            val lightVibrantColor = palette.lightVibrantSwatch?.rgb?.let { Color(it) }

                            val colors = buildList<Color> {
                                vibrantColor?.let { add(it) }
                                dominantColor?.let { add(it) }
                                lightVibrantColor?.let { add(it) }
                                mutedColor?.let { add(it) }
                                darkVibrantColor?.let { add(it) }
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

        // Enhanced action buttons with better visual hierarchy
        val actionButtons: @Composable RowScope.() -> Unit = {
            Spacer(modifier = Modifier.width(12.dp))

            // Enhanced favorite button with pulse animation
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (currentSong?.song?.liked == true)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            secondaryAccentColor.copy(alpha = 0.12f)
                    )
                    .clickable { playerConnection?.toggleLike() }
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "heartPulse")
                val heartScale by infiniteTransition.animateFloat(
                    initialValue = if (currentSong?.song?.liked == true) 0.9f else 1f,
                    targetValue = if (currentSong?.song?.liked == true) 1.1f else 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "heartScale"
                )

                ResizableIconButton(
                    icon = if (currentSong?.song?.liked == true) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    color = if (currentSong?.song?.liked == true)
                        MaterialTheme.colorScheme.error
                    else
                        controlIconColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = if (currentSong?.song?.liked == true) heartScale else 1f
                            scaleY = if (currentSong?.song?.liked == true) heartScale else 1f
                        },
                    onClick = { /* handled by parent clickable */ }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Enhanced menu button with modern styling
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentStyledColor,
                                accentStyledColor.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .clickable {
                        menuState.show {
                            PlayerMenu(
                                mediaMetadata = mediaMetadata,
                                navController = navController,
                                playerBottomSheetState = state,
                                onDismiss = menuState::dismiss
                            )
                        }
                    }
            ) {
                ResizableIconButton(
                    icon = Icons.Outlined.MoreVert,
                    color = iconOnAccentColor,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    onClick = { /* handled by parent clickable */ }
                )
            }
        }

        val controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit = { mediaMetadata ->
            // Enhanced play/pause button animation
            val playPauseScale by animateFloatAsState(
                targetValue = if (isPlaying) 1.05f else 1f,
                animationSpec = tween(durationMillis = 200, easing = LinearEasing),
                label = "playPauseScale"
            )
            val playPauseRoundness by animateDpAsState(
                targetValue = if (isPlaying) 28.dp else 40.dp,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "playPauseRoundness"
            )

            // Action buttons for landscape
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && !tabMode) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = PlayerHorizontalPadding,
                            end = PlayerHorizontalPadding,
                            bottom = 20.dp
                        )
                ) {
                    actionButtons()
                }
            }

            // Enhanced title and artist section with better typography
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mediaMetadata.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Row {
                            mediaMetadata.artists.fastForEachIndexed { index, artist ->
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColor.copy(alpha = 0.8f),
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
                                        color = textColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons for portrait
                    if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE && !tabMode) {
                        actionButtons()
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Enhanced slider with better visual feedback
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    inactiveTrackColor = accentStyledColor.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.fillMaxWidth()
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
                                    inactiveTrackColor = accentStyledColor.copy(alpha = 0.3f)
                                ),
                                squigglesSpec = SquigglySlider.SquigglesSpec(
                                    amplitude = if (isPlaying) 3.dp else 0.dp,
                                    strokeWidth = 5.dp,
                                ),
                                modifier = Modifier.fillMaxWidth()
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
                                            inactiveTrackColor = accentStyledColor.copy(alpha = 0.3f)
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = makeTimeString(sliderPosition ?: position),
                            style = MaterialTheme.typography.labelLarge,
                            color = textColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                            style = MaterialTheme.typography.labelLarge,
                            color = textColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // Enhanced control buttons with modern Material 3 styling
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                ) {
                    val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()

                    // Shuffle button with enhanced styling
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                if (shuffleModeEnabled)
                                    secondaryAccentColor.copy(alpha = 0.2f)
                                else
                                    Color.Transparent
                            )
                            .clickable {
                                playerConnection.triggerShuffle()
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            }
                    ) {
                        ResizableIconButton(
                            icon = Icons.Outlined.Shuffle,
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.Center)
                                .alpha(if (shuffleModeEnabled) 1f else 0.6f),
                            color = if (shuffleModeEnabled) secondaryAccentColor else controlIconColor,
                            onClick = { /* handled by parent clickable */ }
                        )
                    }

                    // Previous button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.Transparent)
                            .clickable(enabled = canSkipPrevious) {
                                playerConnection.player.seekToPrevious()
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            }
                    ) {
                        ResizableIconButton(
                            icon = Icons.Outlined.SkipPrevious,
                            enabled = canSkipPrevious,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center),
                            color = if (canSkipPrevious) controlIconColor else controlIconColor.copy(alpha = 0.4f),
                            onClick = { /* handled by parent clickable */ }
                        )
                    }

                    // Enhanced play/pause button
                    Box(
                        modifier = Modifier
                            .size(if (showLyrics) 64.dp else 80.dp)
                            .animateContentSize()
                            .graphicsLayer {
                                scaleX = playPauseScale
                                scaleY = playPauseScale
                            }
                            .clip(RoundedCornerShape(playPauseRoundness))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        accentStyledColor,
                                        accentStyledColor.copy(alpha = 0.9f)
                                    )
                                )
                            )
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
                            imageVector = if (playbackState == STATE_ENDED) Icons.Outlined.Replay
                            else if (isPlaying) Icons.Outlined.Pause
                            else Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(iconOnAccentColor),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(if (showLyrics) 32.dp else 40.dp)
                        )
                    }

                    // Next button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.Transparent)
                            .clickable(enabled = canSkipNext) {
                                playerConnection.player.seekToNext()
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            }
                    ) {
                        ResizableIconButton(
                            icon = Icons.Outlined.SkipNext,
                            enabled = canSkipNext,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center),
                            color = if (canSkipNext) controlIconColor else controlIconColor.copy(alpha = 0.4f),
                            onClick = { /* handled by parent clickable */ }
                        )
                    }

                    // Repeat button with enhanced styling
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                if (repeatMode != REPEAT_MODE_OFF)
                                    secondaryAccentColor.copy(alpha = 0.2f)
                                else
                                    Color.Transparent
                            )
                            .clickable {
                                playerConnection.player.toggleRepeatMode()
                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                            }
                    ) {
                        ResizableIconButton(
                            icon = when (repeatMode) {
                                REPEAT_MODE_OFF, REPEAT_MODE_ALL -> Icons.Outlined.Repeat
                                REPEAT_MODE_ONE -> Icons.Outlined.RepeatOne
                                else -> throw IllegalStateException()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .align(Alignment.Center)
                                .alpha(if (repeatMode == REPEAT_MODE_OFF) 0.6f else 1f),
                            color = if (repeatMode != REPEAT_MODE_OFF) secondaryAccentColor else controlIconColor,
                            onClick = { /* handled by parent clickable */ }
                        )
                    }
                }
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            // Enhanced background animations
            AnimatedVisibility(
                visible = !powerManager.isPowerSaveMode && state.isExpanded,
                enter = fadeIn(tween(800)),
                exit = fadeOut(tween(800))
            ) {
                AnimatedContent(
                    targetState = mediaMetadata,
                    transitionSpec = {
                        fadeIn(tween(1200)).togetherWith(fadeOut(tween(1200)))
                    },
                    label = "playerBackground"
                ) { metadata ->
                    if (playerBackground == PlayerBackgroundStyle.BLUR) {
                        val scrimColorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.25f), BlendMode.SrcOver)

                        if (metadata?.isLocal == true) {
                            AsyncImageLocal(
                                image = { imageCache.getLocalThumbnail(metadata.localPath, false) },
                                contentScale = ContentScale.FillBounds,
                                colorFilter = scrimColorFilter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(250.dp)
                                    .alpha(0.9f)
                            )
                        } else {
                            AsyncImage(
                                model = metadata?.thumbnailUrl,
                                contentDescription = "Blurred Album Art",
                                contentScale = ContentScale.FillBounds,
                                colorFilter = scrimColorFilter,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(250.dp)
                                    .alpha(0.9f)
                            )
                        }
                    }
                }

                // Enhanced gradient background with multiple layers
                AnimatedContent(
                    targetState = gradientColors,
                    transitionSpec = {
                        fadeIn(tween(1200)) togetherWith fadeOut(tween(1200))
                    },
                    label = "playerGradient"
                ) { colors ->
                    if (playerBackground == PlayerBackgroundStyle.GRADIENT && colors.size >= 2) {
                        val gradientBrush = remember(colors, useDarkTheme) {
                            val topColor = colors[0]
                            val bottomColor = colors.last()
                            val midColor = if (colors.size > 2) colors[1] else topColor

                            val finalTopColor = if (useDarkTheme) topColor.darken(0.3f) else topColor.lighten(0.2f)
                            val finalMidColor = if (useDarkTheme) midColor.darken(0.2f) else midColor.lighten(0.1f)
                            val finalBottomColor = if (useDarkTheme) bottomColor.darken(0.5f) else bottomColor.darken(0.3f)

                            Brush.verticalGradient(
                                colors = listOf(
                                    finalTopColor.copy(alpha = 0.9f),
                                    finalMidColor.copy(alpha = 0.6f),
                                    Color.Transparent,
                                    finalBottomColor.copy(alpha = 0.8f),
                                    finalBottomColor.copy(alpha = 0.95f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(gradientBrush)
                        )

                        // Add subtle overlay pattern
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.1f)
                                        ),
                                        radius = 800f
                                    )
                                )
                        )
                    }
                }

                // Enhanced shimmer effect for playing state
                AnimatedVisibility(
                    visible = !powerManager.isPowerSaveMode && state.isExpanded && isPlaying &&
                            playerBackground == PlayerBackgroundStyle.GRADIENT && gradientColors.isNotEmpty(),
                    enter = fadeIn(tween(1000)),
                    exit = fadeOut(tween(1000))
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "enhancedShimmer")
                    val shimmerTranslate by infiniteTransition.animateFloat(
                        initialValue = -1.5f,
                        targetValue = 2.5f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(5000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "shimmerTranslate"
                    )

                    val shimmerAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "shimmerAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = shimmerAlpha * 0.15f),
                                        Color.White.copy(alpha = shimmerAlpha * 0.08f),
                                        Color.Transparent,
                                    ),
                                    start = Offset(0f, (shimmerTranslate - 1f) * 1500),
                                    end = Offset(1500f, shimmerTranslate * 1500)
                                )
                            )
                    )
                }

                // Enhanced overlay for lyrics mode
                if (playerBackground != PlayerBackgroundStyle.FOLLOW_THEME && showLyrics) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Black.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )
                }
            }

            // Main content with enhanced layout
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
                    // Enhanced thumbnail section
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
                            contentPadding = PaddingValues(vertical = 24.dp, horizontal = 16.dp),
                            flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                            userScrollEnabled = state.isExpanded && swipeToSkip
                        ) {
                            items(
                                items = mediaItems,
                                key = { it.id }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .animateContentSize(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Thumbnail(
                                        sliderPositionProvider = { sliderPosition },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp)),
                                        contentScale = ContentScale.Crop,
                                        showLyricsOnClick = true,
                                        customMediaMetadata = it
                                    )
                                }
                            }
                        }
                    }

                    // Enhanced controls section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(if (showLyrics) 0.65f else 1f, false)
                            .animateContentSize()
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.weight(1f))

                        mediaMetadata?.let {
                            controlsContent(it)
                        }

                        Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                // Portrait layout with enhanced spacing
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                        .padding(bottom = queueSheetState.collapsedBound)
                        .fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Enhanced thumbnail section for portrait
                    BoxWithConstraints(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .nestedScroll(state.preUpPostDownNestedScrollConnection)
                            .padding(horizontal = 16.dp)
                    ) {
                        val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor

                        LazyHorizontalGrid(
                            state = thumbnailLazyGridState,
                            rows = GridCells.Fixed(1),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                            userScrollEnabled = swipeToSkip && state.isExpanded
                        ) {
                            items(
                                items = mediaItems,
                                key = { it.id }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .animateContentSize(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Thumbnail(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(20.dp)),
                                        contentScale = ContentScale.Crop,
                                        sliderPositionProvider = { sliderPosition },
                                        showLyricsOnClick = true,
                                        customMediaMetadata = it
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Controls section
                    mediaMetadata?.let {
                        controlsContent(it)
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }

            // Queue sheet
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