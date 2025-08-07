package com.dd3boh.outertune.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.LocalDownloadUtil
import com.dd3boh.outertune.LocalMenuState
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.LocalSnackbarHostState
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.AlbumThumbnailSize
import com.dd3boh.outertune.constants.ThumbnailCornerRadius
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.db.entities.Album
import com.dd3boh.outertune.models.toMediaMetadata
import com.dd3boh.outertune.playback.ExoDownloadService
import com.dd3boh.outertune.playback.queues.ListQueue
import com.dd3boh.outertune.ui.component.FloatingFooter
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.SelectHeader
import com.dd3boh.outertune.ui.component.SongListItem
import com.dd3boh.outertune.ui.component.YouTubeGridItem
import com.dd3boh.outertune.ui.component.shimmer.ButtonPlaceholder
import com.dd3boh.outertune.ui.component.shimmer.ListItemPlaceHolder
import com.dd3boh.outertune.ui.component.shimmer.ShimmerHost
import com.dd3boh.outertune.ui.component.shimmer.TextPlaceholder
import com.dd3boh.outertune.ui.menu.AlbumMenu
import com.dd3boh.outertune.ui.menu.YouTubeAlbumMenu
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.ui.utils.getNSongsString
import com.dd3boh.outertune.utils.joinByBullet
import com.dd3boh.outertune.viewmodels.AlbumViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val scope = rememberCoroutineScope()

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val albumWithSongs by viewModel.albumWithSongs.collectAsState()
    val otherVersions by viewModel.otherVersions.collectAsState()
    val state = rememberLazyListState()

    // multiselect
    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selection = rememberSaveable(
        saver = listSaver<MutableList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf() }
    val onExitSelectionMode = {
        inSelectMode = false
        selection.clear()
    }
    if (inSelectMode) {
        BackHandler(onBack = onExitSelectionMode)
    }

    val snackbarHostState = LocalSnackbarHostState.current

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    LaunchedEffect(albumWithSongs) {
        if (albumWithSongs?.album?.isLocal != false) return@LaunchedEffect
        val songs = albumWithSongs?.songs?.filterNot { it.song.isLocal }?.map { it.id }
        if (songs.isNullOrEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            val remaining = songs.filterNot { downloads[it]?.state == Download.STATE_COMPLETED }
            downloadState =
                if (remaining.filterNot { s -> downloadUtil.customDownloads.value.any { s == it.key } }.isEmpty())
                    Download.STATE_COMPLETED
                else if (songs.all {
                        downloads[it]?.state == Download.STATE_QUEUED
                                || downloads[it]?.state == Download.STATE_DOWNLOADING
                                || downloads[it]?.state == Download.STATE_COMPLETED
                    })
                    Download.STATE_DOWNLOADING
                else
                    Download.STATE_STOPPED
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (inSelectMode) 64.dp else 0.dp)
        ) {
            LazyColumn(
                state = state,
                contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                modifier = Modifier.weight(1f)
            ) {
                val albumWithSongsLocal = albumWithSongs
                if (albumWithSongsLocal != null && albumWithSongsLocal.songs.isNotEmpty()) {
                    item {
                        CollectionScreenHeader(
                            thumbnailUrl = albumWithSongsLocal.album.thumbnailUrl,
                            title = albumWithSongsLocal.album.title,
                            artists = {
                                val annotatedString = buildAnnotatedString {
                                    withStyle(
                                        style = MaterialTheme.typography.titleSmall.toSpanStyle()
                                    ) {
                                        albumWithSongsLocal.artists.fastForEachIndexed { index, artist ->
                                            withLink(
                                                LinkAnnotation.Clickable(artist.id) {
                                                    navController.navigate("artist/${artist.id}")
                                                }
                                            ) { append(artist.name) }
                                            if (index != albumWithSongsLocal.artists.lastIndex) {
                                                append(", ")
                                            }
                                        }
                                    }
                                }
                                Text(annotatedString)
                            },
                            metadata = if (albumWithSongsLocal.album.year != null) {
                                joinByBullet(
                                    getNSongsString(
                                        albumWithSongsLocal.album.songCount,
                                        albumWithSongsLocal.downloadCount
                                    ),
                                    albumWithSongsLocal.album.year.toString()
                                )
                            } else {
                                getNSongsString(
                                    albumWithSongsLocal.album.songCount,
                                    albumWithSongsLocal.downloadCount
                                )
                            },
                            isLiked = albumWithSongsLocal.album.bookmarkedAt != null,
                            downloadState = downloadState,
                            onPlay = {
                                playerConnection.playQueue(
                                    ListQueue(
                                        title = albumWithSongsLocal.album.title,
                                        items = albumWithSongs?.songs?.mapNotNull { it.toMediaMetadata() }
                                            ?: emptyList(),
                                        playlistId = albumWithSongsLocal.album.playlistId
                                    )
                                )
                            },
                            onShuffle = {
                                playerConnection.playQueue(
                                    ListQueue(
                                        title = albumWithSongsLocal.album.title,
                                        items = albumWithSongs?.songs?.mapNotNull { it.toMediaMetadata() }
                                            ?: emptyList(),
                                        playlistId = albumWithSongsLocal.album.playlistId,
                                        startShuffled = true,
                                    )
                                )
                            },
                            onToggleLike = {
                                database.query {
                                    update(albumWithSongsLocal.album.toggleLike())
                                }
                            },
                            onDownload = {
                                val songs = albumWithSongsLocal.songs.map { it.toMediaMetadata() }
                                downloadUtil.download(songs)
                            },
                            onRemoveDownload = {
                                albumWithSongsLocal.songs.forEach { song ->
                                    DownloadService.sendRemoveDownload(
                                        context,
                                        ExoDownloadService::class.java,
                                        song.id,
                                        false
                                    )
                                }
                            },
                            onShowMenu = {
                                menuState.show {
                                    AlbumMenu(
                                        originalAlbum = Album(
                                            albumWithSongsLocal.album,
                                            albumWithSongsLocal.downloadCount,
                                            albumWithSongsLocal.artists
                                        ),
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            }
                        )
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // --- Song List Section ---
                item {
                    Text(
                        text = stringResource(R.string.songs),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(
                    items = albumWithSongs!!.songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    val selected = selection.contains(song.id)
                    val inSelection = inSelectMode
                    val animatedElevation by animateDpAsState(
                        targetValue = if (selected && inSelection) 8.dp else 0.dp,
                        label = "SongItemElevation"
                    )
                    val animatedColor by animateColorAsState(
                        targetValue = when {
                            selected && inSelection -> MaterialTheme.colorScheme.secondaryContainer
                            !selected && inSelection -> MaterialTheme.colorScheme.surfaceContainerLow.copy(
                                alpha = 0.6f
                            )

                            else -> MaterialTheme.colorScheme.surface
                        },
                        label = "SongItemColor"
                    )
                    val haptic = LocalHapticFeedback.current
                    val interactionSource = remember { MutableInteractionSource() }
                    androidx.compose.material3.Surface(
                        tonalElevation = animatedElevation,
                        color = animatedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (inSelection) Modifier else Modifier
                            )
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = ripple(
                                    bounded = true,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                ),
                                onClick = {
                                    if (inSelection) {
                                        val isSelected = selection.contains(song.id)
                                        if (isSelected) selection.remove(song.id) else selection.add(
                                            song.id
                                        )
                                        // Animate scroll to selected item
                                        if (!isSelected) {
                                            scope.launch {
                                                state.animateScrollToItem(index)
                                            }
                                        }
                                        // Haptic feedback for selection
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } else {
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = albumWithSongs!!.album.title,
                                                items = albumWithSongs!!.songs.map { it.toMediaMetadata() },
                                                startIndex = index,
                                                playlistId = albumWithSongs!!.album.playlistId
                                            )
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (!inSelection) {
                                        inSelectMode = true
                                        selection.add(song.id)
                                        // Animate scroll to selected item
                                        scope.launch {
                                            state.animateScrollToItem(index)
                                        }
                                        // Haptic feedback for long-press
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            )
                            .animateItem()
                    ) {
                        SongListItem(
                            song = song,
                            albumIndex = index + 1,
                            onPlay = {
                                playerConnection.playQueue(
                                    ListQueue(
                                        title = albumWithSongs!!.album.title,
                                        items = albumWithSongs!!.songs.map { it.toMediaMetadata() },
                                        startIndex = index,
                                        playlistId = albumWithSongs!!.album.playlistId
                                    )
                                )
                            },
                            onSelectedChange = {
                                inSelectMode = true
                                if (it) {
                                    selection.add(song.id)
                                    // Animate scroll to selected item
                                    scope.launch {
                                        state.animateScrollToItem(index)
                                    }
                                    // Haptic feedback for selection
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } else {
                                    selection.remove(song.id)
                                }
                            },
                            inSelectMode = inSelectMode,
                            isSelected = selection.contains(song.id),
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        )
                    }
                }
                // --- Other Versions Section ---
                if (otherVersions.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = stringResource(R.string.other_versions),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                            )
                            LazyRow(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 12.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = otherVersions,
                                    key = { it.id },
                                ) { item ->
                                    val interactionSource = remember { MutableInteractionSource() }
                                    YouTubeGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = scope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                interactionSource = interactionSource,
                                                indication = ripple(),
                                                onClick = { navController.navigate("album/${item.id}") },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubeAlbumMenu(
                                                            albumItem = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            state = state,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
            modifier = Modifier.padding(bottom = if (inSelectMode) 64.dp else 0.dp)
        ) {
            val albumWithSongsLocal = albumWithSongs
            if (albumWithSongsLocal != null && albumWithSongsLocal.songs.isNotEmpty()) {
                item {
                    CollectionScreenHeader(
                        thumbnailUrl = albumWithSongsLocal.album.thumbnailUrl,
                        title = albumWithSongsLocal.album.title,
                        artists = {
                            val annotatedString = buildAnnotatedString {
                                withStyle(
                                    style = MaterialTheme.typography.titleSmall.toSpanStyle()
                                ) {
                                    albumWithSongsLocal.artists.fastForEachIndexed { index, artist ->
                                        withLink(
                                            LinkAnnotation.Clickable(artist.id) {
                                                navController.navigate("artist/${artist.id}")
                                            }
                                        ) { append(artist.name) }
                                        if (index != albumWithSongsLocal.artists.lastIndex) {
                                            append(", ")
                                        }
                                    }
                                }
                            }
                            Text(annotatedString)
                        },
                        metadata = if (albumWithSongsLocal.album.year != null) {
                            joinByBullet(
                                getNSongsString(
                                    albumWithSongsLocal.album.songCount,
                                    albumWithSongsLocal.downloadCount
                                ),
                                albumWithSongsLocal.album.year.toString()
                            )
                        } else {
                            getNSongsString(
                                albumWithSongsLocal.album.songCount,
                                albumWithSongsLocal.downloadCount
                            )
                        },
                        isLiked = albumWithSongsLocal.album.bookmarkedAt != null,
                        downloadState = downloadState,
                        onPlay = {
                            playerConnection.playQueue(
                                ListQueue(
                                    title = albumWithSongsLocal.album.title,
                                    items = albumWithSongs?.songs?.mapNotNull { it.toMediaMetadata() }
                                        ?: emptyList(),
                                    playlistId = albumWithSongsLocal.album.playlistId
                                )
                            )
                        },
                        onShuffle = {
                            playerConnection.playQueue(
                                ListQueue(
                                    title = albumWithSongsLocal.album.title,
                                    items = albumWithSongs?.songs?.mapNotNull { it.toMediaMetadata() }
                                        ?: emptyList(),
                                    playlistId = albumWithSongsLocal.album.playlistId,
                                    startShuffled = true,
                                )
                            )
                        },
                        onToggleLike = {
                            database.query {
                                update(albumWithSongsLocal.album.toggleLike())
                            }
                        },
                        onDownload = {
                            val songs = albumWithSongsLocal.songs.map { it.toMediaMetadata() }
                            downloadUtil.download(songs)
                        },
                        onRemoveDownload = {
                            albumWithSongsLocal.songs.forEach { song ->
                                DownloadService.sendRemoveDownload(
                                    context,
                                    ExoDownloadService::class.java,
                                    song.id,
                                    false
                                )
                            }
                        },
                        onShowMenu = {
                            menuState.show {
                                AlbumMenu(
                                    originalAlbum = Album(
                                        albumWithSongsLocal.album,
                                        albumWithSongsLocal.downloadCount,
                                        albumWithSongsLocal.artists
                                    ),
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                        isCompact = true
                    )
                }

                // --- Song List Section ---
                item {
                    Text(
                        text = stringResource(R.string.songs),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(
                    items = albumWithSongs!!.songs,
                    key = { _, song -> song.id }
                ) { index, song ->
                    val selected = selection.contains(song.id)
                    val inSelection = inSelectMode
                    val animatedElevation by animateDpAsState(
                        targetValue = if (selected && inSelection) 8.dp else 0.dp,
                        label = "SongItemElevation"
                    )
                    val animatedColor by animateColorAsState(
                        targetValue = when {
                            selected && inSelection -> MaterialTheme.colorScheme.secondaryContainer
                            !selected && inSelection -> MaterialTheme.colorScheme.surfaceContainerLow.copy(
                                alpha = 0.6f
                            )

                            else -> MaterialTheme.colorScheme.surface
                        },
                        label = "SongItemColor"
                    )
                    val haptic = LocalHapticFeedback.current
                    val interactionSource = remember { MutableInteractionSource() }
                    androidx.compose.material3.Surface(
                        tonalElevation = animatedElevation,
                        color = animatedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (inSelection) Modifier else Modifier
                            )
                            .combinedClickable(
                                interactionSource = interactionSource,
                                indication = ripple(
                                    bounded = true,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                ),
                                onClick = {
                                    if (inSelection) {
                                        val isSelected = selection.contains(song.id)
                                        if (isSelected) selection.remove(song.id) else selection.add(
                                            song.id
                                        )
                                        // Animate scroll to selected item
                                        if (!isSelected) {
                                            scope.launch {
                                                state.animateScrollToItem(index)
                                            }
                                        }
                                        // Haptic feedback for selection
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } else {
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = albumWithSongsLocal.album.title,
                                                items = albumWithSongsLocal.songs.map { it.toMediaMetadata() },
                                                startIndex = index,
                                                playlistId = albumWithSongsLocal.album.playlistId
                                            )
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (!inSelection) {
                                        inSelectMode = true
                                        selection.add(song.id)
                                        // Animate scroll to selected item
                                        scope.launch {
                                            state.animateScrollToItem(index)
                                        }
                                        // Haptic feedback for long-press
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            )
                            .animateItem()
                    ) {
                        SongListItem(
                            song = song,
                            albumIndex = index + 1,
                            onPlay = {
                                playerConnection.playQueue(
                                    ListQueue(
                                        title = albumWithSongsLocal.album.title,
                                        items = albumWithSongsLocal.songs.map { it.toMediaMetadata() },
                                        startIndex = index,
                                        playlistId = albumWithSongsLocal.album.playlistId
                                    )
                                )
                            },
                            onSelectedChange = {
                                inSelectMode = true
                                if (it) {
                                    selection.add(song.id)
                                    // Animate scroll to selected item
                                    scope.launch {
                                        state.animateScrollToItem(index)
                                    }
                                    // Haptic feedback for selection
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } else {
                                    selection.remove(song.id)
                                }
                            },
                            inSelectMode = inSelectMode,
                            isSelected = selection.contains(song.id),
                            navController = navController,
                            snackbarHostState = snackbarHostState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        )
                    }
                }
                // --- Other Versions Section ---
                if (otherVersions.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = stringResource(R.string.other_versions),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                            )
                            LazyRow(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 12.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = otherVersions,
                                    key = { it.id },
                                ) { item ->
                                    val interactionSource = remember { MutableInteractionSource() }
                                    YouTubeGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = scope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                interactionSource = interactionSource,
                                                indication = ripple(),
                                                onClick = { navController.navigate("album/${item.id}") },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        YouTubeAlbumMenu(
                                                            albumItem = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    ShimmerHost {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(
                                    modifier = Modifier
                                        .size(AlbumThumbnailSize)
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )

                                Spacer(Modifier.width(16.dp))

                                Column(
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    TextPlaceholder()
                                    TextPlaceholder()
                                    TextPlaceholder()
                                }
                            }

                            Spacer(Modifier.padding(8.dp))

                            Row {
                                ButtonPlaceholder(Modifier.weight(1f))

                                Spacer(Modifier.width(12.dp))

                                ButtonPlaceholder(Modifier.weight(1f))
                            }
                        }

                        repeat(6) {
                            ListItemPlaceHolder()
                        }
                    }
                }
            }
        }
    }

    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null
                )
            }
        },
        windowInsets = TopBarInsets,
        scrollBehavior = scrollBehavior
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        FloatingFooter(inSelectMode) {
            val albumWithSongsLocal = albumWithSongs
            if (albumWithSongsLocal != null && albumWithSongsLocal.songs.isNotEmpty()) {
                SelectHeader(
                    navController = navController,
                    selectedItems = selection.mapNotNull { id ->
                        albumWithSongsLocal.songs.find { it.song.id == id }
                    }.map { it.toMediaMetadata() },
                    totalItemCount = albumWithSongsLocal.songs.size,
                    onSelectAll = {
                        selection.clear()
                        selection.addAll(albumWithSongsLocal.songs.map { it.id })
                    },
                    onDeselectAll = { selection.clear() },
                    menuState = menuState,
                    onDismiss = onExitSelectionMode
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter)
        )
    }
}