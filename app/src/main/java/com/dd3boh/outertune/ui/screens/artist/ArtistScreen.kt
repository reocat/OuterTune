package com.dd3boh.outertune.ui.screens.artist

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.LocalMenuState
import com.dd3boh.outertune.LocalNetworkConnected
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.LocalSnackbarHostState
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.AppBarHeight
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.db.entities.ArtistEntity
import com.dd3boh.outertune.extensions.toMediaItem
import com.dd3boh.outertune.extensions.togglePlayPause
import com.dd3boh.outertune.models.toMediaMetadata
import com.dd3boh.outertune.playback.queues.ListQueue
import com.dd3boh.outertune.playback.queues.YouTubeQueue
import com.dd3boh.outertune.ui.component.AlbumGridItem
import com.dd3boh.outertune.ui.component.AutoResizeText
import com.dd3boh.outertune.ui.component.FontSizeRange
import com.dd3boh.outertune.ui.component.HideOnScrollFAB
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.NavigationTitle
import com.dd3boh.outertune.ui.component.SongListItem
import com.dd3boh.outertune.ui.component.SwipeToQueueBox
import com.dd3boh.outertune.ui.component.YouTubeGridItem
import com.dd3boh.outertune.ui.component.YouTubeListItem
import com.dd3boh.outertune.ui.component.shimmer.ArtistPagePlaceholder
import com.dd3boh.outertune.ui.menu.AlbumMenu
import com.dd3boh.outertune.ui.menu.YouTubeAlbumMenu
import com.dd3boh.outertune.ui.menu.YouTubeArtistMenu
import com.dd3boh.outertune.ui.menu.YouTubePlaylistMenu
import com.dd3boh.outertune.ui.menu.YouTubeSongMenu
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.ui.utils.fadingEdge
import com.dd3boh.outertune.ui.utils.resize
import com.dd3boh.outertune.viewmodels.ArtistViewModel
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.SongItem

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: ArtistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val isNetworkConnected = LocalNetworkConnected.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val artistPage = viewModel.artistPage
    val libraryArtist by viewModel.libraryArtist.collectAsState()
    val librarySongs by viewModel.librarySongs.collectAsState()
    val libraryAlbums by viewModel.libraryAlbums.collectAsState()

    val lazyListState = rememberLazyListState()
    val snackbarHostState = LocalSnackbarHostState.current
    var showLocal by rememberSaveable { mutableStateOf(false) }

    val transparentAppBar by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0
        }
    }

    val librarySongsAvailable = {
        librarySongs.filter { it.song.isAvailableOffline() || isNetworkConnected }
        .map { it.toMediaMetadata() }
        .toList()
    }

    LaunchedEffect(isNetworkConnected, libraryArtist) {
        // always show local page for local artists. Show local page remote artist when offline
        showLocal = libraryArtist?.artist?.isLocal == true
    }

    val artistHead = @Composable {
        if (artistPage != null || libraryArtist != null) {
            val thumbnail = artistPage?.artist?.thumbnail ?: libraryArtist?.artist?.thumbnailUrl
            val artistName = artistPage?.artist?.title ?: libraryArtist?.artist?.name

            Column {
                // Enhanced Hero Section with MD3 Expressive design
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (thumbnail != null) Modifier.aspectRatio(3f / 2) else Modifier.height(320.dp)
                        )
                ) {
                    if (thumbnail != null) {
                        // Enhanced image with gradient overlay
                        AsyncImage(
                            model = thumbnail.resize(1200, 900),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .fadingEdge(
                                    top = WindowInsets.systemBars
                                        .asPaddingValues()
                                        .calculateTopPadding() + AppBarHeight,
                                    bottom = 80.dp
                                )
                        )
                        
                        // Gradient overlay for better text readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = 0.6f
                                }
                                .clip(
                                    RoundedCornerShape(
                                        bottomStart = 24.dp,
                                        bottomEnd = 24.dp
                                    )
                                )
                        ) {
                            // Gradient background
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Transparent,
                                shape = RoundedCornerShape(
                                    bottomStart = 24.dp,
                                    bottomEnd = 24.dp
                                )
                            ) {
                                // Gradient overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            alpha = 0.3f
                                        }
                                )
                            }
                        }
                    } else {
                        // Fallback surface for artists without thumbnails
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(
                                bottomStart = 24.dp,
                                bottomEnd = 24.dp
                            )
                        ) {}
                    }
                    
                    // Enhanced artist name with better typography
                    AutoResizeText(
                        text = artistName ?: "Unknown",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.25).sp
                        ),
                        fontSizeRange = FontSizeRange(28.sp, 48.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .then(
                                if (thumbnail == null) {
                                    Modifier.padding(
                                        top = WindowInsets.systemBars
                                            .asPaddingValues()
                                            .calculateTopPadding() + AppBarHeight
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    )
                }

                // Enhanced action buttons with MD3 Expressive design
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val watchEndpoint = artistPage?.artist?.shuffleEndpoint?: artistPage?.artist?.playEndpoint
                                playerConnection.playQueue(
                                    if (!showLocal && watchEndpoint != null) YouTubeQueue(watchEndpoint)
                                    else ListQueue(
                                        title = artistName,
                                        items = librarySongs.map { it.toMediaMetadata() },
                                        startShuffled = true,
                                    ),
                                    isRadio = true,
                                    title = artistName
                                )
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 20.dp,
                                vertical = 12.dp
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shuffle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                text = stringResource(R.string.shuffle),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        if (!showLocal) {
                            artistPage?.artist?.radioEndpoint?.let { radioEndpoint ->
                                OutlinedButton(
                                    onClick = {
                                        playerConnection.playQueue(
                                            YouTubeQueue(radioEndpoint),
                                            isRadio = true,
                                            title = "Radio: ${artistPage.artist.title}"
                                        )
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 20.dp,
                                        vertical = 12.dp
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Radio,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.size(6.dp))
                                    Text(
                                        text = stringResource(R.string.radio),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .add(
                    WindowInsets(
                        top = -WindowInsets.systemBars.asPaddingValues()
                            .calculateTopPadding() - AppBarHeight
                    )
                )
                .asPaddingValues()
        ) {
            if (artistPage == null && !showLocal) {
                item(key = "shimmer") {
                    ArtistPagePlaceholder()
                }
            }
            else {
                item(key = "header") {
                    artistHead()
                }

                if (showLocal) {
                    if (librarySongs.isNotEmpty()) {
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.songs),
                                onClick = {
                                    navController.navigate("artist/${viewModel.artistId}/songs")
                                }
                            )
                        }

                        itemsIndexed(
                            items = librarySongs,
                            key = { _, item -> item.hashCode() }
                        ) { index, song ->
                            SongListItem(
                                song = song,
                                onPlay = {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = "Library: ${libraryArtist?.artist?.name}",
                                            items = librarySongs.map { it.toMediaMetadata() },
                                            startIndex = index
                                        )
                                    )
                                },
                                onSelectedChange = { },
                                inSelectMode = false,
                                isSelected = false,
                                navController = navController,
                                snackbarHostState = snackbarHostState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                            )

                        }
                    }

                    if (libraryAlbums.isNotEmpty()) {
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.albums),
                                onClick = {
                                    navController.navigate("artist/${viewModel.artistId}/albums")
                                }
                            )
                        }

                        item {
                            LazyRow(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = libraryAlbums,
                                    key = { it.id }
                                ) { album ->
                                    AlbumGridItem(
                                        album = album,
                                        isActive = album.id == mediaMetadata?.album?.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("album/${album.id}")
                                                },
                                                onLongClick = {
                                                    menuState.show {
                                                        AlbumMenu(
                                                            originalAlbum = album,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                            .animateItem()
                                    )
                                }
                            }
                        }

                    }
                }
                else artistPage?.sections?.fastForEach { section ->
                    val isSongsSection = (section.items.firstOrNull() as? SongItem)?.album != null

                    item {
                        NavigationTitle(
                            title = if (isSongsSection) stringResource(R.string.songs) else section.title,
                            onClick = section.moreEndpoint?.let {
                                {
                                    navController.navigate("artist/${viewModel.artistId}/items?browseId=${it.browseId}?params=${it.params}")
                                }
                            }
                        )
                    }

                    if (isSongsSection) {
                        items(
                            items = section.items,
                            key = { it.id }
                        ) { song ->
                            SwipeToQueueBox(
                                item = (song as SongItem).toMediaItem(),
                                content = {
                                    YouTubeListItem(
                                        item = song,
                                        isActive = mediaMetadata?.id == song.id,
                                        isPlaying = isPlaying,
                                        trailingContent = {
                                            IconButton(
                                                onClick = {
                                                    menuState.show {
                                                        YouTubeSongMenu(
                                                            song = song,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.MoreVert,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    if (song.id == mediaMetadata?.id) {
                                                        playerConnection.player.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            ListQueue(
                                                                title = "Artist songs (preview): ${artistPage.artist.title}",
                                                                items = section.items.map { (it as SongItem).toMediaMetadata() },
                                                                startIndex = section.items.indexOf(
                                                                    song
                                                                )
                                                            )
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    menuState.show {
                                                        YouTubeSongMenu(
                                                            song = song,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss
                                                        )
                                                    }
                                                }
                                            )
                                            .animateItem()
                                    )
                                },
                                snackbarHostState = snackbarHostState
                            )

                        }
                    }
                    else {
                        item {
                            LazyRow(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = section.items,
                                    key = { it.id }
                                ) { item ->
                                    YouTubeGridItem(
                                        item = item,
                                        isActive = when (item) {
                                            is SongItem -> mediaMetadata?.id == item.id
                                            is AlbumItem -> mediaMetadata?.album?.id == item.id
                                            else -> false
                                        },
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    when (item) {
                                                        is SongItem -> playerConnection.playQueue(
                                                            YouTubeQueue.radio(item.toMediaMetadata()),
                                                            title = artistPage.artist.title
                                                        )

                                                        is AlbumItem -> navController.navigate(
                                                            "album/${item.id}"
                                                        )

                                                        is ArtistItem -> navController.navigate(
                                                            "artist/${item.id}"
                                                        )

                                                        is PlaylistItem -> navController.navigate(
                                                            "online_playlist/${item.id}"
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    menuState.show {
                                                        when (item) {
                                                            is SongItem -> YouTubeSongMenu(
                                                                song = item,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss
                                                            )

                                                            is AlbumItem -> YouTubeAlbumMenu(
                                                                albumItem = item,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss
                                                            )

                                                            is ArtistItem -> YouTubeArtistMenu(
                                                                artist = item,
                                                                onDismiss = menuState::dismiss
                                                            )

                                                            is PlaylistItem -> YouTubePlaylistMenu(
                                                                navController = navController,
                                                                playlist = item,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                            .animateItem()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HideOnScrollFAB(
            visible = librarySongs.isNotEmpty() && libraryArtist?.artist?.isLocal != true,
            lazyListState = lazyListState,
            icon = if (showLocal) Icons.Outlined.LibraryMusic else Icons.Outlined.Language,
            onClick = {
                if (isNetworkConnected) {
                    showLocal = showLocal.not()
                    if (!showLocal && artistPage == null) viewModel.fetchArtistsFromYTM()
                }
                else showLocal = true
            }
        )

        TopAppBar(
            title = { if (!transparentAppBar) Text(artistPage?.artist?.title.orEmpty()) },
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
            actions = {
                IconButton(
                    onClick = {
                        database.transaction {
                            val artist = libraryArtist?.artist
                            if (artist != null) {
                                update(artist.toggleLike())
                            } else {
                                artistPage?.artist?.let {
                                    insert(
                                        ArtistEntity(
                                            id = it.id,
                                            name = it.title,
                                            channelId = it.channelId,
                                            thumbnailUrl = it.thumbnail,
                                        ).toggleLike()
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (libraryArtist?.artist?.bookmarkedAt != null) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                        tint = if (libraryArtist?.artist?.bookmarkedAt != null) MaterialTheme.colorScheme.error else LocalContentColor.current,
                        contentDescription = null
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.artistPage?.artist?.shareLink?.let { link ->
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, link)
                            }
                            context.startActivity(Intent.createChooser(intent, null))
                        }
                    }
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = null
                    )
                }
            },
            windowInsets = TopBarInsets,
            scrollBehavior = scrollBehavior,
            colors = if (transparentAppBar) {
                TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            } else {
                TopAppBarDefaults.topAppBarColors()
            }
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}