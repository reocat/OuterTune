/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.component

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.DarkMode
import com.dd3boh.outertune.constants.DarkModeKey
import com.dd3boh.outertune.constants.LyricFontSizeKey
import com.dd3boh.outertune.constants.LyricTrimKey
import com.dd3boh.outertune.constants.LyricsPosition
import com.dd3boh.outertune.constants.LyricsTextPositionKey
import com.dd3boh.outertune.constants.MultilineLrcKey
import com.dd3boh.outertune.constants.PlayerBackgroundStyle
import com.dd3boh.outertune.constants.PlayerBackgroundStyleKey
import com.dd3boh.outertune.constants.ShowLyricsKey
import com.dd3boh.outertune.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.dd3boh.outertune.lyrics.LyricsEntry
import com.dd3boh.outertune.lyrics.LyricsEntry.Companion.HEAD_LYRICS_ENTRY
import com.dd3boh.outertune.lyrics.LyricsUtils
import com.dd3boh.outertune.lyrics.LyricsUtils.findCurrentLineIndex
import com.dd3boh.outertune.lyrics.LyricsUtils.loadAndParseLyricsString
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.ui.component.shimmer.ShimmerHost
import com.dd3boh.outertune.ui.component.shimmer.TextPlaceholder
import com.dd3boh.outertune.ui.menu.LyricsMenu
import com.dd3boh.outertune.ui.utils.fadingEdge
import com.dd3boh.outertune.utils.ComposeToImage
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val density = LocalDensity.current
    val context = LocalContext.current
    var showLyrics by rememberPreference(ShowLyricsKey, false)
    val landscapeOffset = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scope = rememberCoroutineScope()

    val lyricsTextPosition by rememberEnumPreference(LyricsTextPositionKey, LyricsPosition.CENTER)
    val lyricsFontSize by rememberPreference(LyricFontSizeKey, 20)

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val lyricsEntity by playerConnection.currentLyrics.collectAsState(initial = null)
    val lyrics = remember(lyricsEntity) { lyricsEntity?.lyrics?.trim() }
    val multilineLrc = rememberPreference(MultilineLrcKey, defaultValue = true)
    val lyricTrim = rememberPreference(LyricTrimKey, defaultValue = false)

    val playerBackground by rememberEnumPreference(key = PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.DEFAULT)

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val lines: List<LyricsEntry> = remember(lyrics) {
        if (lyrics == null || lyrics == LYRICS_NOT_FOUND) emptyList()
        else if (lyrics.startsWith("[")) listOf(HEAD_LYRICS_ENTRY) +
                loadAndParseLyricsString(lyrics, LyricsUtils.LrcParserOptions(lyricTrim.value, multilineLrc.value, "Unable to parse lyrics"))
        else lyrics.lines().mapIndexed { index, line -> LyricsEntry(index * 100L, line) }
    }
    val isSynced = remember(lyrics) {
        !lyrics.isNullOrEmpty() && lyrics.startsWith("[")
    }

    val textColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.secondary
        else ->
            if (useDarkTheme)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onPrimary
    }

    var currentLineIndex by remember { mutableIntStateOf(-1) }
    var deferredCurrentLineIndex by rememberSaveable { mutableIntStateOf(0) }
    var lastPreviewTime by rememberSaveable { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }

    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var showShareDialog by remember { mutableStateOf(false) }
    var showShareImageDialog by remember { mutableStateOf(false) }

    var showProgressDialog by remember { mutableStateOf(false) }
    val paletteColors = remember { mutableStateListOf<Color>() }

    LaunchedEffect(lines) {
        isSelectionMode = false
        selectedIndices.clear()
    }

    LaunchedEffect(mediaMetadata?.thumbnailUrl) {
        val coverUrl = mediaMetadata?.thumbnailUrl ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val req = ImageRequest.Builder(context).data(coverUrl).allowHardware(false).build()
                val result = loader.execute(req)
                val bmp = result.drawable?.toBitmap() ?: return@withContext
                val palette = Palette.from(bmp).generate()
                val swatches = palette.swatches.sortedByDescending { it.population }
                val colors = swatches.map { Color(it.rgb) }
                    .filter { color ->
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                        hsv[1] > 0.2f
                    }
                paletteColors.clear()
                paletteColors.addAll(colors.take(3))
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(lyrics) {
        if (lyrics.isNullOrEmpty() || !lyrics.startsWith("[")) {
            currentLineIndex = -1
            return@LaunchedEffect
        }
        while (isActive) {
            delay(50)
            val sliderPosition = sliderPositionProvider()
            isSeeking = sliderPosition != null
            currentLineIndex = findCurrentLineIndex(lines, sliderPosition ?: playerConnection.player.currentPosition)
        }
    }

    LaunchedEffect(isSeeking, lastPreviewTime) {
        if (isSeeking) {
            lastPreviewTime = 0L
        } else if (lastPreviewTime != 0L) {
            delay(LyricsPreviewTime)
            lastPreviewTime = 0L
        }
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentLineIndex, lastPreviewTime) {
        fun countNewLine(str: String) = str.count { it == '\n' }

        fun calculateOffset() = with(density) {
            if (landscapeOffset) {
                24.dp.toPx().toInt() * countNewLine(lines[currentLineIndex].content)
            } else {
                32.dp.toPx().toInt() * countNewLine(lines[currentLineIndex].content)
            }
        }

        if (!isSynced) return@LaunchedEffect
        if (currentLineIndex != -1) {
            deferredCurrentLineIndex = currentLineIndex
            if (lastPreviewTime == 0L && !isSelectionMode) {
                if (isSeeking) {
                    lazyListState.scrollToItem(currentLineIndex,
                        with(density) { 48.dp.toPx().toInt() } + calculateOffset())
                } else {
                    lazyListState.animateScrollToItem(currentLineIndex,
                        with(density) { 48.dp.toPx().toInt() } + calculateOffset())
                }
            }
        }
    }

    val MAX_SELECTABLE_LYRICS = 5

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = WindowInsets.systemBars
                .only(WindowInsetsSides.Top)
                .add(WindowInsets(top = maxHeight / 2, bottom = maxHeight / 2))
                .asPaddingValues(),
            modifier = Modifier
                .fadingEdge(vertical = 96.dp)
                .nestedScroll(remember {
                    object : NestedScrollConnection {
                        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                            if (!isSelectionMode) {
                                lastPreviewTime = System.currentTimeMillis()
                            }
                            return super.onPostScroll(consumed, available, source)
                        }

                        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                            if (!isSelectionMode) {
                                lastPreviewTime = System.currentTimeMillis()
                            }
                            return super.onPostFling(consumed, available)
                        }
                    }
                })
        ) {
            val displayedCurrentLineIndex = if (isSeeking || isSelectionMode) deferredCurrentLineIndex else currentLineIndex

            if (lyrics == null) {
                item {
                    ShimmerHost {
                        repeat(10) { index ->
                            Box(
                                contentAlignment = when (lyricsTextPosition) {
                                    LyricsPosition.LEFT -> Alignment.CenterStart
                                    LyricsPosition.CENTER -> Alignment.Center
                                    LyricsPosition.RIGHT -> Alignment.CenterEnd
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                TextPlaceholder()
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(items = lines) { index, item ->
                    val isSelected = selectedIndices.contains(index)
                    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

                    Text(
                        text = item.content,
                        fontSize = lyricsFontSize.sp,
                        color = textColor.copy(alpha = if (index == displayedCurrentLineIndex) 1f else 0.6f),
                        textAlign = when (lyricsTextPosition) {
                            LyricsPosition.LEFT -> TextAlign.Left
                            LyricsPosition.CENTER -> TextAlign.Center
                            LyricsPosition.RIGHT -> TextAlign.Right
                        },
                        fontWeight = if (index == displayedCurrentLineIndex) FontWeight.ExtraBold else FontWeight.Medium,
                        lineHeight = (lyricsFontSize * 1.3).sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .graphicsLayer {
                                alpha = if (!isSynced || index == displayedCurrentLineIndex || isSelected) 1f else 0.6f
                                translationY = if (index == displayedCurrentLineIndex) 0.dp.toPx() else 0f
                            }
                            .combinedClickable(
                                onClick = {
                                    if (isSelectionMode) {
                                        if (isSelected) {
                                            selectedIndices.remove(index)
                                            if (selectedIndices.isEmpty()) {
                                                isSelectionMode = false
                                            }
                                        } else {
                                            if (selectedIndices.size < MAX_SELECTABLE_LYRICS) {
                                                selectedIndices.add(index)
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.max_selection_limit),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else if (isSynced) {
                                        playerConnection.player.seekTo(item.timeStamp)
                                        lastPreviewTime = 0L
                                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    }
                                },
                                onLongClick = {
                                    if (!isSelectionMode) {
                                        isSelectionMode = true
                                        selectedIndices.add(index)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .drawWithCache {
                                onDrawBehind {
                                    if (index == displayedCurrentLineIndex) {
                                        val strokeWidth = 2.dp.toPx()
                                        val y = size.height - strokeWidth / 2
                                        drawLine(
                                            color = lineColor,
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                                }
                            }
                    )
                }
            }
        }

        if (lyrics == LYRICS_NOT_FOUND) {
            Text(
                text = stringResource(R.string.lyrics_not_found),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = when (lyricsTextPosition) {
                    LyricsPosition.LEFT -> TextAlign.Left
                    LyricsPosition.CENTER -> TextAlign.Center
                    LyricsPosition.RIGHT -> TextAlign.Right
                },
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        mediaMetadata?.let { mediaMetadata ->
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 8.dp)
            ) {
                if (isSelectionMode) {
                    IconButton(
                        onClick = {
                            if (selectedIndices.isNotEmpty()) {
                                showShareDialog = true
                            }
                        },
                        enabled = selectedIndices.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.share),
                            tint = textColor.copy(alpha = if (selectedIndices.isNotEmpty()) 1f else 0.5f)
                        )
                    }

                    IconButton(
                        onClick = {
                            isSelectionMode = false
                            selectedIndices.clear()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cancel),
                            tint = textColor
                        )
                    }
                } else {
                    IconButton(
                        onClick = { showLyrics = false },
                        modifier = Modifier.graphicsLayer { alpha = 0.9f }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = textColor
                        )
                    }

                    IconButton(
                        onClick = {
                            menuState.show {
                                LyricsMenu(
                                    lyricsProvider = { lyricsEntity },
                                    mediaMetadataProvider = { mediaMetadata },
                                    onDismiss = menuState::dismiss
                                )
                            }
                        },
                        modifier = Modifier.graphicsLayer { alpha = 0.9f }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreHoriz,
                            contentDescription = null,
                            tint = textColor
                        )
                    }
                }
            }
        }
    }

    if (showShareDialog && selectedIndices.isNotEmpty()) {
        val selectedLyrics = selectedIndices.sorted()
            .mapNotNull { lines.getOrNull(it)?.content }
            .joinToString("\n")

        BasicAlertDialog(onDismissRequest = { showShareDialog = false }) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.share_lyrics),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val shareText = buildString {
                                append("\"$selectedLyrics\"\n\n")
                                mediaMetadata?.let { metadata ->
                                    append("${metadata.title} - ")
                                    append(metadata.artists.joinToString { it.name })
                                    append("\nhttps://music.youtube.com/watch?v=${metadata.id}")
                                }
                            }
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_lyrics)))
                            showShareDialog = false
                            isSelectionMode = false
                            selectedIndices.clear()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.share_as_text))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showShareDialog = false
                            showShareImageDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.share_as_image))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showShareDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }

    if (showShareImageDialog && selectedIndices.isNotEmpty()) {
        val selectedLyrics = selectedIndices.sorted()
            .mapNotNull { lines.getOrNull(it)?.content }
            .joinToString("\n")

        ShareAsImageDialog(
            selectedLyrics = selectedLyrics,
            mediaMetadata = mediaMetadata!!,
            paletteColors = paletteColors,
            onDismissRequest = { showShareImageDialog = false },
            onShareImage = { backgroundColor, textColor, secondaryTextColor ->
                showShareImageDialog = false
                showProgressDialog = true
                scope.launch {
                    try {
                        val image = ComposeToImage.createLyricsImage(
                            context = context,
                            coverArtUrl = mediaMetadata?.thumbnailUrl,
                            songTitle = mediaMetadata?.title ?: "",
                            artistName = mediaMetadata?.artists?.joinToString { it.name } ?: "",
                            lyrics = selectedLyrics,
                            width = 1080,
                            height = 1920,
                            backgroundColor = backgroundColor.toArgb(),
                            textColor = textColor.toArgb(),
                            secondaryTextColor = secondaryTextColor.toArgb()
                        )
                        val timestamp = System.currentTimeMillis()
                        val filename = "lyrics_$timestamp"
                        val uri = ComposeToImage.saveBitmapAsFile(context, image, filename)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Lyrics"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to share image: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        showProgressDialog = false
                        isSelectionMode = false
                        selectedIndices.clear()
                    }
                }
            }
        )
    }

    if (showProgressDialog) {
        BasicAlertDialog(onDismissRequest = { /* Non-dismissable during generation */ }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Generating Image...\nPlease wait",
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAsImageDialog(
    selectedLyrics: String,
    mediaMetadata: MediaMetadata,
    paletteColors: List<Color>,
    onDismissRequest: () -> Unit,
    onShareImage: (Color, Color, Color) -> Unit
) {
    var previewBackgroundColor by remember { mutableStateOf(Color(0xFF242424)) }
    var previewTextColor by remember { mutableStateOf(Color.White) }
    var previewSecondaryTextColor by remember { mutableStateOf(Color.White.copy(alpha = 0.7f)) }

    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.share_as_image),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    LyricsImageCard(
                        lyricText = selectedLyrics,
                        mediaMetadata = mediaMetadata,
                        backgroundColor = previewBackgroundColor,
                        textColor = previewTextColor,
                        secondaryTextColor = previewSecondaryTextColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.background_color),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    listOf(Color(0xFF242424), Color.White, Color.Black).plus(paletteColors).distinct().take(8).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, RoundedCornerShape(8.dp))
                                .clickable { previewBackgroundColor = color }
                                .border(
                                    2.dp,
                                    if (previewBackgroundColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.text_color),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    listOf(Color.White, Color.Black).plus(paletteColors).distinct().take(8).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, RoundedCornerShape(8.dp))
                                .clickable { previewTextColor = color }
                                .border(
                                    2.dp,
                                    if (previewTextColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }

                Text(
                    text = stringResource(id = R.string.secondary_text_color),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    listOf(Color.White.copy(alpha = 0.7f), Color.Black.copy(alpha = 0.7f)).plus(paletteColors.map { it.copy(alpha = 0.7f) }).distinct().take(8).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, RoundedCornerShape(8.dp))
                                .clickable { previewSecondaryTextColor = color }
                                .border(
                                    2.dp,
                                    if (previewSecondaryTextColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onShareImage(previewBackgroundColor, previewTextColor, previewSecondaryTextColor)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.share))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

const val animateScrollDuration = 300L
val LyricsPreviewTime = 4.seconds