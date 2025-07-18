package com.dd3boh.outertune.ui.menu

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.LibraryAddCheck
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastSumBy
import androidx.media3.exoplayer.offline.Download.STATE_COMPLETED
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.LocalDownloadUtil
import com.dd3boh.outertune.LocalImageCache
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.LocalSyncUtils
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.ListItemHeight
import com.dd3boh.outertune.constants.ListThumbnailSize
import com.dd3boh.outertune.constants.SyncMode
import com.dd3boh.outertune.constants.ThumbnailCornerRadius
import com.dd3boh.outertune.constants.YtmSyncModeKey
import com.dd3boh.outertune.db.entities.Event
import com.dd3boh.outertune.db.entities.Playlist
import com.dd3boh.outertune.db.entities.PlaylistSong
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.extensions.toMediaItem
import com.dd3boh.outertune.models.toMediaMetadata
import com.dd3boh.outertune.playback.ExoDownloadService
import com.dd3boh.outertune.playback.queues.YouTubeQueue
import com.dd3boh.outertune.ui.component.AsyncImageLocal
import com.dd3boh.outertune.ui.component.DetailsDialog
import com.dd3boh.outertune.ui.component.DownloadGridMenu
import com.dd3boh.outertune.ui.component.GridMenu
import com.dd3boh.outertune.ui.component.GridMenuItem
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.ListDialog
import com.dd3boh.outertune.ui.component.ListItem
import com.dd3boh.outertune.ui.component.TextFieldDialog
import com.dd3boh.outertune.utils.joinByBullet
import com.dd3boh.outertune.utils.makeTimeString
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.zionhuang.innertube.YouTube
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SongMenu(
    originalSong: Song,
    playlistSong: PlaylistSong? = null,
    playlist: Playlist? = null,
    event: Event? = null,
    navController: NavController,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val downloadUtil = LocalDownloadUtil.current
    val clipboard = LocalClipboard.current
    val imageCache = LocalImageCache.current
    val syncUtils = LocalSyncUtils.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val syncMode by rememberEnumPreference(key = YtmSyncModeKey, defaultValue = SyncMode.RO)

    val songState = database.song(originalSong.id).collectAsState(initial = originalSong)
    val song = songState.value ?: originalSong
    val download by LocalDownloadUtil.current.getDownload(originalSong.id).collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    val currentFormatState = database.format(originalSong.id).collectAsState(initial = null)
    val currentFormat = currentFormatState.value

    var showEditDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(song.song.liked) {
        downloadUtil.autoDownloadIfLiked(song.song)
    }

    if (showEditDialog) {
        TextFieldDialog(
            icon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) },
            title = { Text(text = stringResource(R.string.edit_song)) },
            onDismiss = { showEditDialog = false },
            initialTextFieldValue = TextFieldValue(song.song.title, TextRange(song.song.title.length)),
            onDone = { title ->
                onDismiss()
                database.query {
                    update(song.song.copy(title = title))
                }
            }
        )
    }

    var showChooseQueueDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToQueueDialog(
        isVisible = showChooseQueueDialog,
        onAdd = { queueName ->
            playerConnection.service.queueBoard.addQueue(
                queueName, listOf(song.toMediaMetadata()),
                forceInsert = true, delta = false
            )
            playerConnection.service.queueBoard.setCurrQueue()
        },
        onDismiss = {
            showChooseQueueDialog = false
        }
    )

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToPlaylistDialog(
        navController = navController,
        isVisible = showChoosePlaylistDialog,
        onGetSong = { playlist ->
            coroutineScope.launch(Dispatchers.IO) {
                playlist.playlist.browseId?.let { browseId ->
                    YouTube.addToPlaylist(browseId, song.id)
                }
            }
            listOf(song.id)
        },
        onDismiss = { showChoosePlaylistDialog = false }
    )

    var showSelectArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSelectArtistDialog) {
        ListDialog(
            onDismiss = { showSelectArtistDialog = false }
        ) {
            items(
                items = song.artists,
                key = { it.id }
            ) { artist ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(ListItemHeight)
                        .clickable {
                            navController.navigate("artist/${artist.id}")
                            showSelectArtistDialog = false
                            onDismiss()
                        }
                        .padding(horizontal = 12.dp),
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = artist.thumbnailUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(ListThumbnailSize)
                                .clip(CircleShape)
                        )
                    }
                    Text(
                        text = artist.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }

    var showDetailsDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDetailsDialog) {
        DetailsDialog(
            mediaMetadata = song.toMediaMetadata(),
            currentFormat = currentFormat,
            currentPlayCount = song.playCount?.fastSumBy { it.count } ?: 0,
            volume = playerConnection.player.volume,
            clipboard = clipboard,
            setVisibility = { showDetailsDialog = it }
        )
    }

    ListItem(
        title = song.song.title,
        subtitle = joinByBullet(
            song.artists.joinToString { it.name },
            makeTimeString(song.song.duration * 1000L)
        ),
        thumbnailContent = {
            if (song.song.isLocal) {
                AsyncImageLocal(
                    image = { imageCache.getLocalThumbnail(song.song.localPath, true) },
                    modifier = Modifier
                        .size(ListThumbnailSize)
                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                )
            } else {
                AsyncImage(
                    model = song.song.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ListThumbnailSize)
                        .clip(RoundedCornerShape(ThumbnailCornerRadius))
                )
            }
        },
        trailingContent = {
            IconButton(
                onClick = {
                    val s = song.song.toggleLike()
                    database.query {
                        update(s)
                    }

                    if (!s.isLocal) {
                        syncUtils.likeSong(s)
                    }
                }
            ) {
                Icon(
                    imageVector = if (song.song.liked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    tint = if (song.song.liked) MaterialTheme.colorScheme.error else LocalContentColor.current,
                    contentDescription = null
                )
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
        if (!song.song.isLocal)
            GridMenuItem(
                icon = Icons.Outlined.Radio,
                title = R.string.start_radio
            ) {
                onDismiss()
                playerConnection.playQueue(YouTubeQueue.radio(song.toMediaMetadata()), isRadio = true)
            }
        GridMenuItem(
            icon = Icons.AutoMirrored.Outlined.PlaylistPlay,
            title = R.string.play_next
        ) {
            onDismiss()
            playerConnection.enqueueNext(song.toMediaItem())
        }
        GridMenuItem(
            icon = Icons.Outlined.Edit,
            title = R.string.edit
        ) {
            showEditDialog = true
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

        if (playlistSong != null && (playlist?.playlist?.isLocal == true
                    || (playlistSong.song.song.isLocal || syncMode == SyncMode.RW))) {
            GridMenuItem(
                icon = Icons.Outlined.PlaylistRemove,
                title = R.string.remove_from_playlist
            ) {
                database.transaction {
                    coroutineScope.launch {
                        playlist?.id?.let { playlistId ->
                            if (playlistSong.map.setVideoId != null) {
                                YouTube.removeFromPlaylist(
                                    playlistId, playlistSong.map.songId, playlistSong.map.setVideoId
                                )
                            }
                        }
                    }
                    move(playlistSong.map.playlistId, playlistSong.map.position, Int.MAX_VALUE)
                    delete(playlistSong.map.copy(position = Int.MAX_VALUE))
                }

                onDismiss()
            }
        }

        if (!song.song.isLocal)
            DownloadGridMenu(
                state = if (downloadUtil.getCustomDownload(song.id)) STATE_COMPLETED else download?.state,
                onDownload = {
                    downloadUtil.download(song.toMediaMetadata())
                },
                onRemoveDownload = {
                    if (song.song.localPath != null) {
                        downloadUtil.delete(song)
                    } else {
                        DownloadService.sendRemoveDownload(
                            context,
                            ExoDownloadService::class.java,
                            song.id,
                            false
                        )
                    }
                }
            )


        GridMenuItem(
            icon = R.drawable.artist,
            title = R.string.view_artist,
            enabled = song.artists.isNotEmpty()
        ) {
            if (song.artists.size == 1) {
                navController.navigate("artist/${song.artists[0].id}")
                onDismiss()
            } else {
                showSelectArtistDialog = true
            }
        }
        GridMenuItem(
            icon = Icons.Outlined.Album,
            title = R.string.view_album,
            enabled = song.song.albumId != null && !song.song.isLocal
        ) {
            onDismiss()
            navController.navigate("album/${song.song.albumId}")
        }
        GridMenuItem(
            icon = Icons.Outlined.Share,
            title = R.string.share,
            enabled = !song.song.isLocal
        ) {
            onDismiss()
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "https://music.youtube.com/watch?v=${song.id}")
            }
            context.startActivity(Intent.createChooser(intent, null))
            }
        GridMenuItem(
            icon = Icons.Outlined.Info,
            title = R.string.details
        ) {
            showDetailsDialog = true
        }
        if (song.song.inLibrary == null) {
            GridMenuItem(
                icon = Icons.Outlined.LibraryAdd,
                title = R.string.add_to_library,
                enabled = !song.song.isLocal
            ) {
                database.query {
                    update(song.song.toggleLibrary())
                }
            }
        } else {
            GridMenuItem(
                icon = Icons.Outlined.LibraryAddCheck,
                title = R.string.remove_from_library,
                enabled = !song.song.isLocal
            ) {
                database.query {
                    update(song.song.toggleLibrary())
                }
            }
        }
        if (event != null) {
            GridMenuItem(
                icon = Icons.Outlined.Delete,
                title = R.string.remove_from_history
            ) {
                onDismiss()
                database.query {
                    delete(event)
                }
            }
        }
    }
}
