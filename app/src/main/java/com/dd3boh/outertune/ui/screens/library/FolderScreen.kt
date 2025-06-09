package com.dd3boh.outertune.ui.screens.library

import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastSumBy
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.MainActivity
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.CONTENT_TYPE_FOLDER
import com.dd3boh.outertune.constants.CONTENT_TYPE_HEADER
import com.dd3boh.outertune.constants.CONTENT_TYPE_SONG
import com.dd3boh.outertune.constants.FlatSubfoldersKey
import com.dd3boh.outertune.constants.LastLocalScanKey
import com.dd3boh.outertune.constants.LocalLibraryEnableKey
import com.dd3boh.outertune.constants.SongSortDescendingKey
import com.dd3boh.outertune.constants.SongSortType
import com.dd3boh.outertune.constants.SongSortTypeKey
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.models.DirectoryTree
import com.dd3boh.outertune.models.toMediaMetadata
import com.dd3boh.outertune.playback.queues.ListQueue
import com.dd3boh.outertune.ui.component.FloatingFooter
import com.dd3boh.outertune.ui.component.HideOnScrollFAB
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.LocalMenuState
import com.dd3boh.outertune.ui.component.ResizableIconButton
import com.dd3boh.outertune.ui.component.SelectHeader
import com.dd3boh.outertune.ui.component.SongFolderItem
import com.dd3boh.outertune.ui.component.SongListItem
import com.dd3boh.outertune.ui.component.SortHeader
import com.dd3boh.outertune.ui.component.shimmer.ListItemPlaceHolder
import com.dd3boh.outertune.ui.component.shimmer.ShimmerHost
import com.dd3boh.outertune.ui.screens.Screens
import com.dd3boh.outertune.ui.utils.MEDIA_PERMISSION_LEVEL
import com.dd3boh.outertune.ui.utils.STORAGE_ROOT
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.fixFilePath
import com.dd3boh.outertune.utils.numberToAlpha
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import com.dd3boh.outertune.viewmodels.LibraryFoldersViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

data class FolderScreenState(
    val allSongs: List<Song> = emptyList(),
    val displayedSongs: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val inSelectMode: Boolean = false,
    val selectedSongIds: Set<String> = emptySet(),
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: LibraryFoldersViewModel = hiltViewModel(),
    filterContent: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val (flatSubfolders, onFlatSubfoldersChange) = rememberPreference(FlatSubfoldersKey, defaultValue = true)
    val lastLocalScan by rememberPreference(
        LastLocalScanKey,
        LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond()
    )
    val localLibEnable by rememberPreference(LocalLibraryEnableKey, defaultValue = true)
    val (sortType, onSortTypeChange) = rememberEnumPreference(SongSortTypeKey, SongSortType.CREATE_DATE)
    val (sortDescending, onSortDescendingChange) = rememberPreference(SongSortDescendingKey, true)

    var screenState by remember { mutableStateOf(FolderScreenState()) }
    val lazyListState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

    val currentDir by viewModel.localSongDirectoryTree.collectAsState()
    val filteredSongs = remember { viewModel.filteredSongs }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop = backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    fun getAllSongsFromDirectory(directory: DirectoryTree, flat: Boolean): List<Song> {
        return if (flat) {
            val allSongs = mutableListOf<Song>()
            val queue = ArrayDeque<DirectoryTree>()
            queue.add(directory)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                allSongs.addAll(current.files)
                queue.addAll(current.subdirs)
            }
            allSongs.distinctBy { it.id }
        } else {
            directory.files.distinctBy { it.id }
        }
    }

    fun sortSongs(songs: List<Song>, sortType: SongSortType, descending: Boolean): List<Song> {
        val sorted = songs.sortedBy { song ->
            when (sortType) {
                SongSortType.CREATE_DATE -> numberToAlpha(song.song.inLibrary?.toEpochSecond(ZoneOffset.UTC) ?: -1L)
                SongSortType.MODIFIED_DATE -> numberToAlpha(song.song.dateModified?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli() ?: -1L)
                SongSortType.RELEASE_DATE -> numberToAlpha(song.song.year?.toLong() ?: -1L)
                SongSortType.NAME -> song.song.title.lowercase()
                SongSortType.ARTIST -> song.artists.joinToString { it.name }.lowercase()
                SongSortType.PLAY_TIME -> numberToAlpha(song.song.totalPlayTime)
                SongSortType.PLAY_COUNT -> numberToAlpha((song.playCount?.fastSumBy { it.count })?.toLong() ?: 0L)
            }
        }
        return if (descending) sorted.reversed() else sorted
    }

    fun updateScreenState() {
        if (currentDir.isSkeleton) {
            screenState = screenState.copy(isLoading = true)
            return
        }

        val allSongs = getAllSongsFromDirectory(currentDir, flatSubfolders)
        val sortedSongs = sortSongs(allSongs, sortType, sortDescending)

        screenState = screenState.copy(
            allSongs = allSongs,
            displayedSongs = sortedSongs,
            filteredSongs = filteredSongs,
            isLoading = false
        )
    }

    fun performSearch(searchText: String) {
        scope.launch {
            delay(300)
            if (query.text == searchText) {
                viewModel.searchInDir(searchText)
            }
        }
    }

    fun toggleSelectMode(enable: Boolean) {
        screenState = screenState.copy(
            inSelectMode = enable,
            selectedSongIds = if (!enable) emptySet() else screenState.selectedSongIds
        )
    }

    fun toggleSongSelection(songId: String) {
        val newSelection = if (screenState.selectedSongIds.contains(songId)) {
            screenState.selectedSongIds - songId
        } else {
            screenState.selectedSongIds + songId
        }

        screenState = screenState.copy(
            inSelectMode = true,
            selectedSongIds = newSelection
        )
    }

    fun selectAllSongs() {
        screenState = screenState.copy(
            selectedSongIds = screenState.displayedSongs.map { it.id }.toSet()
        )
    }

    fun clearSelection() {
        screenState = screenState.copy(selectedSongIds = emptySet())
    }

    fun startSearchMode() {
        screenState = screenState.copy(isSearching = true)
    }

    fun exitSearchMode() {
        screenState = screenState.copy(
            isSearching = false,
            searchQuery = ""
        )
        query = TextFieldValue()
    }

    LaunchedEffect(lastLocalScan) {
        if (viewModel.uiInit && !currentDir.isSkeleton && viewModel.lastLocalScan != lastLocalScan) {
            viewModel.lastLocalScan = lastLocalScan
            navController.backToMain()
            viewModel.getLocalSongs()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.lastLocalScan == 0L) {
            viewModel.lastLocalScan = lastLocalScan
        }

        if (!viewModel.uiInit) {
            scope.launch {
                viewModel.getLocalSongs()
                viewModel.getSongCount()
                viewModel.uiInit = true
            }
        }
    }

    LaunchedEffect(currentDir, flatSubfolders, sortType, sortDescending, filteredSongs) {
        updateScreenState()
    }

    LaunchedEffect(screenState.isSearching) {
        if (screenState.isSearching) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(query.text) {
        if (screenState.isSearching && query.text != screenState.searchQuery) {
            screenState = screenState.copy(searchQuery = query.text)
            performSearch(query.text)
        }
    }

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazyListState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(screenState.inSelectMode) {
        backStackEntry?.savedStateHandle?.set("inSelectMode", screenState.inSelectMode)
    }

    if (screenState.inSelectMode) {
        BackHandler { toggleSelectMode(false) }
    } else if (screenState.isSearching) {
        BackHandler { exitSearchMode() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
            modifier = Modifier.padding(bottom = if (screenState.inSelectMode) 64.dp else 0.dp)
        ) {
            item(
                key = "header",
                contentType = CONTENT_TYPE_HEADER
            ) {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (screenState.isSearching) {
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.search),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                        } else {
                            IconButton(onClick = { startSearchMode() }) {
                                Icon(Icons.Rounded.Search, contentDescription = null)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Icon(Icons.Rounded.SdCard, contentDescription = null)
                                TextButton(onClick = { navController.navigate("settings/local") }) {
                                    Text(text = stringResource(R.string.scanner_local_title))
                                }
                            }

                            ResizableIconButton(
                                icon = if (flatSubfolders) Icons.AutoMirrored.Rounded.List else Icons.Rounded.AccountTree,
                                onClick = { onFlatSubfoldersChange(!flatSubfolders) }
                            )
                        }
                    }

                    filterContent?.let { content ->
                        var showStoragePerm by remember {
                            mutableStateOf(
                                context.checkSelfPermission(MEDIA_PERMISSION_LEVEL)
                                        != PackageManager.PERMISSION_GRANTED
                            )
                        }

                        if (localLibEnable && showStoragePerm) {
                            TextButton(
                                onClick = {
                                    showStoragePerm = false
                                    (context as MainActivity).permissionLauncher.launch(MEDIA_PERMISSION_LEVEL)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.error)
                            ) {
                                Text(
                                    text = stringResource(R.string.missing_media_permission_warning),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        content()
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        SortHeader(
                            sortType = sortType,
                            sortDescending = sortDescending,
                            onSortTypeChange = onSortTypeChange,
                            onSortDescendingChange = onSortDescendingChange,
                            sortTypeText = { sortType ->
                                when (sortType) {
                                    SongSortType.CREATE_DATE -> R.string.sort_by_create_date
                                    SongSortType.MODIFIED_DATE -> R.string.sort_by_date_modified
                                    SongSortType.RELEASE_DATE -> R.string.sort_by_date_released
                                    SongSortType.NAME -> R.string.sort_by_name
                                    SongSortType.ARTIST -> R.string.sort_by_artist
                                    SongSortType.PLAY_TIME -> R.string.sort_by_play_time
                                    SongSortType.PLAY_COUNT -> R.string.sort_by_play_count
                                }
                            }
                        )

                        Spacer(Modifier.weight(1f))

                        val songCount = if (screenState.isSearching) {
                            screenState.filteredSongs.size
                        } else {
                            screenState.displayedSongs.size
                        }

                        Text(
                            text = pluralStringResource(R.plurals.n_song, songCount, songCount),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            if (screenState.isLoading) {
                item {
                    ShimmerHost {
                        repeat(8) {
                            ListItemPlaceHolder()
                        }
                    }
                }
                return@LazyColumn
            }

            if (!screenState.isSearching && !flatSubfolders) {
                if (fixFilePath(currentDir.getFullPath()) != STORAGE_ROOT) {
                    item(
                        key = "previous",
                        contentType = CONTENT_TYPE_FOLDER
                    ) {
                        SongFolderItem(
                            folderTitle = "..",
                            subtitle = "Previous folder",
                            modifier = Modifier.clickable {
                                if (currentDir.culmSongs.value > 0) {
                                    navController.navigateUp()
                                }
                            }
                        )
                    }
                }

                itemsIndexed(
                    items = currentDir.subdirs,
                    key = { _, item -> item.currentDir },
                    contentType = { _, _ -> CONTENT_TYPE_FOLDER }
                ) { _, folder ->
                    SongFolderItem(
                        folder = folder,
                        folderTitle = if (folder.files.isEmpty()) folder.getSquashedDir() else null,
                        subtitle = null,
                        modifier = Modifier
                            .combinedClickable {
                                val route = Screens.Folders.route + "/" +
                                        folder.getFullSquashedDir().replace('/', ';')
                                navController.navigate(route)
                            }
                            .animateItem(),
                        menuState = menuState,
                        navController = navController
                    )
                }

                if (currentDir.subdirs.isNotEmpty() && screenState.displayedSongs.isNotEmpty()) {
                    item(key = "folder_songs_divider") {
                        HorizontalDivider(
                            thickness = DividerDefaults.Thickness,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }

            val songsToShow = if (screenState.isSearching) {
                screenState.filteredSongs
            } else {
                screenState.displayedSongs
            }

            itemsIndexed(
                items = songsToShow,
                key = { _, item -> item.id },
                contentType = { _, _ -> CONTENT_TYPE_SONG }
            ) { _, song ->
                SongListItem(
                    song = song,
                    onPlay = {
                        playerConnection.playQueue(
                            ListQueue(
                                title = currentDir.currentDir.substringAfterLast('/'),
                                items = screenState.displayedSongs.map { it.toMediaMetadata() },
                                startIndex = screenState.displayedSongs.indexOf(song)
                            )
                        )
                    },
                    onSelectedChange = { toggleSongSelection(song.id) },
                    inSelectMode = screenState.inSelectMode,
                    isSelected = screenState.selectedSongIds.contains(song.id),
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }
        }

        HideOnScrollFAB(
            visible = screenState.displayedSongs.isNotEmpty(),
            lazyListState = lazyListState,
            icon = Icons.Rounded.Shuffle,
            onClick = {
                playerConnection.playQueue(
                    ListQueue(
                        title = currentDir.currentDir.substringAfterLast('/'),
                        items = screenState.displayedSongs.map { it.toMediaMetadata() },
                        startShuffled = true
                    )
                )
            }
        )

        TopAppBar(
            title = {
                Column {
                    val title = currentDir.currentDir.substringAfterLast('/')
                    val subtitle = currentDir.getFullPath().substringBeforeLast('/')

                    Text(
                        text = if (currentDir.currentDir == "storage") {
                            stringResource(R.string.local_player_settings_title)
                        } else {
                            title
                        },
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (screenState.isSearching) {
                            exitSearchMode()
                        } else {
                            navController.navigateUp()
                        }
                    },
                    onLongClick = {
                        if (!screenState.isSearching) {
                            navController.backToMain()
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                }
            },
            windowInsets = TopBarInsets,
            scrollBehavior = scrollBehavior
        )

        FloatingFooter(screenState.inSelectMode) {
            SelectHeader(
                navController = navController,
                selectedItems = screenState.selectedSongIds.mapNotNull { songId ->
                    screenState.displayedSongs.find { it.id == songId }
                }.map { it.toMediaMetadata() },
                totalItemCount = screenState.displayedSongs.size,
                onSelectAll = { selectAllSongs() },
                onDeselectAll = { clearSelection() },
                menuState = menuState,
                onDismiss = { toggleSelectMode(false) }
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter)
        )
    }
}