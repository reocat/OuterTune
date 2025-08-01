package com.dd3boh.outertune.ui.menu

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.LocalDownloadUtil
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.R
import com.dd3boh.outertune.db.entities.PlaylistEntity
import com.dd3boh.outertune.db.entities.PlaylistSongMap
import com.dd3boh.outertune.extensions.toMediaItem
import com.dd3boh.outertune.models.toMediaMetadata
import com.dd3boh.outertune.playback.ExoDownloadService
import com.dd3boh.outertune.playback.queues.ListQueue
import com.dd3boh.outertune.playback.queues.YouTubeQueue
import com.dd3boh.outertune.ui.dialog.DefaultDialog
import com.dd3boh.outertune.ui.component.button.IconButton
import com.dd3boh.outertune.ui.component.items.YouTubeListItem
import com.dd3boh.outertune.ui.dialog.AddToPlaylistDialog
import com.dd3boh.outertune.ui.dialog.AddToQueueDialog
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.utils.completed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun YouTubePlaylistMenu(
    navController: NavController,
    playlist: PlaylistItem,
    songs: List<SongItem> = emptyList(),
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val downloadUtil = LocalDownloadUtil.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val dbPlaylist by database.playlistByBrowseId(playlist.id).collectAsState(initial = null)

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showChooseQueueDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showRemoveDownloadDialog by remember {
        mutableStateOf(false)
    }
    var showDeletePlaylistDialog by remember {
        mutableStateOf(false)
    }

    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    LaunchedEffect(songs) {
        if (songs.isEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            val remaining = songs.filterNot { downloads[it.id]?.state == Download.STATE_COMPLETED }
            downloadState =
                if (remaining.filterNot { s -> downloadUtil.customDownloads.value.any { s.id == it.key } }.isEmpty())
                    Download.STATE_COMPLETED
                else if (songs.all {
                        downloads[it.id]?.state == Download.STATE_QUEUED
                                || downloads[it.id]?.state == Download.STATE_DOWNLOADING
                                || downloads[it.id]?.state == Download.STATE_COMPLETED
                    })
                    Download.STATE_DOWNLOADING
                else
                    Download.STATE_STOPPED
        }
    }

    YouTubeListItem(
        item = playlist,
        trailingContent = {
            if (playlist.id != "LM" && !playlist.isEditable) {
                IconButton(
                    onClick = {
                        if (dbPlaylist?.playlist == null) {
                            database.transaction {
                                val playlistEntity = PlaylistEntity(
                                    name = playlist.title,
                                    description = playlist.description,
                                    privacyStatus = playlist.privacyStatus,
                                    browseId = playlist.id,
                                    isEditable = false,
                                    thumbnailUrl = playlist.thumbnail,
                                    remoteSongCount = playlist.songCountText?.let { Regex("""\d+""").find(it)?.value?.toIntOrNull() },
                                    playEndpointParams = playlist.playEndpoint?.params,
                                    shuffleEndpointParams = playlist.shuffleEndpoint?.params,
                                    radioEndpointParams = playlist.radioEndpoint?.params
                                ).toggleLike()

                                insert(playlistEntity)
                                coroutineScope.launch(Dispatchers.IO) {
                                    songs.ifEmpty {
                                        YouTube.playlist(playlist.id).completed()
                                            .getOrNull()?.songs.orEmpty()
                                    }.map { it.toMediaMetadata() }
                                        .onEach(::insert)
                                        .mapIndexed { index, song ->
                                            PlaylistSongMap(
                                                songId = song.id,
                                                playlistId = playlistEntity.id,
                                                position = index
                                            )
                                        }
                                        .forEach(::insert)
                                }
                            }
                        } else {
                            database.transaction {
                                update(dbPlaylist!!.playlist.toggleLike())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (dbPlaylist?.playlist?.bookmarkedAt != null) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        tint = if (dbPlaylist?.playlist?.bookmarkedAt != null) MaterialTheme.colorScheme.error else LocalContentColor.current,
                        contentDescription = null
                    )
                }
            }
        }
    )

    HorizontalDivider()

    GridMenu(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        playlist.playEndpoint?.let {
            GridMenuItem(
                icon = Icons.Outlined.PlayArrow,
                title = R.string.play
            ) {
                playerConnection.playQueue(
                    ListQueue(
                        playlistId = playlist.playEndpoint!!.playlistId,
                        title = playlist.title,
                        items = songs.map { it.toMediaMetadata() },
                    )
                )

                onDismiss()
            }
        }

        playlist.shuffleEndpoint?.let { shuffleEndpoint ->
            GridMenuItem(
                icon = Icons.Outlined.Shuffle,
                title = R.string.shuffle
            ) {
                playerConnection.playQueue(
                    ListQueue(
                        playlistId = playlist.playEndpoint!!.playlistId,
                        title = playlist.title,
                        items = songs.map { it.toMediaMetadata() },
                        startShuffled = true,
                    )
                )
                onDismiss()
            }
        }

        playlist.radioEndpoint?.let { radioEndpoint ->
            GridMenuItem(
                icon = Icons.Outlined.Radio,
                title = R.string.start_radio
            ) {
                playerConnection.playQueue(YouTubeQueue(radioEndpoint), isRadio = true)
                onDismiss()
            }
        }

        GridMenuItem(
            icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
            title = R.string.play_next
        ) {
            coroutineScope.launch {
                songs.ifEmpty {
                    withContext(Dispatchers.IO) {
                        YouTube.playlist(playlist.id).completed().getOrNull()?.songs.orEmpty()
                    }
                }.let { songs ->
                    playerConnection.enqueueNext(songs.map { it.toMediaItem() })
                }
            }
            onDismiss()
        }

        GridMenuItem(
            icon = Icons.AutoMirrored.Outlined.QueueMusic,
            title = R.string.add_to_queue
        ) {
            showChooseQueueDialog = true
        }

        GridMenuItem(
            icon = Icons.AutoMirrored.Outlined.PlaylistAdd,
            title = R.string.add_to_playlist
        ) {
            showChoosePlaylistDialog = true
        }

        if (songs.isNotEmpty()) {
            DownloadGridMenu(
                state = downloadState,
                onDownload = {
                    val _songs = songs.map { it.toMediaMetadata() }
                    downloadUtil.download(_songs)
                },
                onRemoveDownload = {
                    showRemoveDownloadDialog = true
                }
            )
        }

        GridMenuItem(
            icon = Icons.Outlined.Share,
            title = R.string.share
        ) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, playlist.shareLink)
            }
            context.startActivity(Intent.createChooser(intent, null))
            onDismiss()
        }

        GridMenuItem(
            icon = Icons.Outlined.PlaylistRemove,
            title = R.string.delete
        ) {
            showDeletePlaylistDialog = true
        }
    }

    /**
     * ---------------------------
     * Dialogs
     * ---------------------------
     */

    if (showChooseQueueDialog) {
        AddToQueueDialog(
            onAdd = { queueName ->
                coroutineScope.launch {
                    songs.ifEmpty {
                        withContext(Dispatchers.IO) {
                            YouTube.playlist(playlist.id).completed().getOrNull()?.songs.orEmpty()
                        }
                    }.let { songs ->
                        playerConnection.service.queueBoard.addQueue(
                            queueName, songs.map { it.toMediaMetadata() },
                            forceInsert = true, delta = false
                        )
                        playerConnection.service.queueBoard.setCurrQueue()
                    }
                }
            },
            onDismiss = {
                showChooseQueueDialog = false
            }
        )
    }

    if (showChoosePlaylistDialog) {
        AddToPlaylistDialog(
            navController = navController,
            onGetSong = { targetPlaylist ->
                val allSongs = songs
                    .ifEmpty {
                        YouTube.playlist(targetPlaylist.id).completed().getOrNull()?.songs.orEmpty()
                    }.map {
                        it.toMediaMetadata()
                    }
                database.transaction {
                    allSongs.forEach(::insert)
                }

                coroutineScope.launch(Dispatchers.IO) {
                    targetPlaylist.playlist.browseId?.let { playlistId ->
                        YouTube.addPlaylistToPlaylist(playlistId, targetPlaylist.id)
                    }
                }

                allSongs.map { it.id }
            },
            onDismiss = { showChoosePlaylistDialog = false }
        )
    }

    if (showRemoveDownloadDialog) {
        DefaultDialog(
            onDismiss = { showRemoveDownloadDialog = false },
            content = {
                Text(
                    text = stringResource(R.string.remove_download_playlist_confirm, playlist.title),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            },
            buttons = {
                TextButton(
                    onClick = {
                        showRemoveDownloadDialog = false
                    }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }

                TextButton(
                    onClick = {
                        showRemoveDownloadDialog = false
                        songs.forEach { song ->
                            DownloadService.sendRemoveDownload(
                                context,
                                ExoDownloadService::class.java,
                                song.id,
                                false
                            )
                        }
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        )
    }

    if (showDeletePlaylistDialog) {
        DefaultDialog(
            onDismiss = { showDeletePlaylistDialog = false },
            content = {
                Text(
                    text = stringResource(R.string.delete_playlist_confirm, playlist.title),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            },
            buttons = {
                TextButton(
                    onClick = {
                        showDeletePlaylistDialog = false
                    }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }

                TextButton(
                    onClick = {
                        showDeletePlaylistDialog = false
                        onDismiss()
                        database.transaction {
                            deletePlaylistById(playlist.id)
                        }
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        )
    }
}
