package com.dd3boh.outertune.ui.screens

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.navigation.NavController
import com.dd3boh.outertune.BuildConfig
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.AccountChannelHandleKey
import com.dd3boh.outertune.constants.AccountEmailKey
import com.dd3boh.outertune.constants.AccountNameKey
import com.dd3boh.outertune.constants.AutomaticScannerKey
import com.dd3boh.outertune.constants.DarkModeKey
import com.dd3boh.outertune.constants.FirstSetupPassed
import com.dd3boh.outertune.constants.InnerTubeCookieKey
import com.dd3boh.outertune.constants.LibraryFilter
import com.dd3boh.outertune.constants.LibraryFilterKey
import com.dd3boh.outertune.constants.LocalLibraryEnableKey
import com.dd3boh.outertune.constants.LyricTrimKey
import com.dd3boh.outertune.constants.NewInterfaceKey
import com.dd3boh.outertune.constants.PureBlackKey
import com.dd3boh.outertune.constants.SongSortType
import com.dd3boh.outertune.db.entities.ArtistEntity
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.db.entities.SongEntity
import com.dd3boh.outertune.extensions.move
import com.dd3boh.outertune.ui.component.ChipsLazyRow
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.SongListItem
import com.dd3boh.outertune.ui.component.SortHeader
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.component.TokenEditorDialog
import com.dd3boh.outertune.ui.screens.settings.DarkMode
import com.dd3boh.outertune.ui.screens.settings.NavigationTab
import com.dd3boh.outertune.utils.decodeTabString
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import com.zionhuang.innertube.utils.parseCookieString
import kotlinx.coroutines.delay
import java.time.LocalDateTime

data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun SetupWizard(
    navController: NavController,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    val uriHandler = LocalUriHandler.current

    val (firstSetupPassed, onFirstSetupPassedChange) = rememberPreference(FirstSetupPassed, defaultValue = false)

    // content prefs
    val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)
    val (newInterfaceStyle, onNewInterfaceStyleChange) = rememberPreference(key = NewInterfaceKey, defaultValue = true)
    var filter by rememberEnumPreference(LibraryFilterKey, LibraryFilter.ALL)

    val accountName by rememberPreference(AccountNameKey, "")
    val accountEmail by rememberPreference(AccountEmailKey, "")
    val accountChannelHandle by rememberPreference(AccountChannelHandleKey, "")
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val (ytmSync, onYtmSyncChange) = rememberPreference(LyricTrimKey, defaultValue = true)

    // local media prefs
    val (localLibEnable, onLocalLibEnableChange) = rememberPreference(LocalLibraryEnableKey, defaultValue = true)
    val (autoScan, onAutoScanChange) = rememberPreference(AutomaticScannerKey, defaultValue = false)

    var position by remember {
        mutableIntStateOf(0)
    }

    val MAX_POS = 4

    if (position > 0) {
        BackHandler {
            position -= 1
        }
    }

    if (firstSetupPassed) {
        navController.navigateUp()
    }

    val navBar = @Composable {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    if (position > 0) {
                        position -= 1
                    }
                }
            ) {
                Text(
                    text = "Back",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                    contentDescription = null
                )
            }

            LinearProgressIndicator(
                progress = { position.toFloat() / MAX_POS },
                strokeCap = StrokeCap.Butt,
                drawStopIndicator = {},
                modifier = Modifier
                    .weight(1f, false)
                    .height(8.dp)
                    .padding(2.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    if (position == 1) {
                        filter = LibraryFilter.ALL // hax
                    }

                    if (position < MAX_POS) {
                        position += 1
                    }
                }
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                    contentDescription = null
                )
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (position in 1..<MAX_POS) {
                Box(
                    Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        navBar()
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        top = 0.dp,
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                )
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp))

                when (position) {
                    0 -> { // landing page
                        WelcomePage(
                            onNext = { position += 1 },
                            onSkip = {
                                onFirstSetupPassedChange(true)
                                navController.navigateUp()
                            },
                            onRestoreBackup = {
                                navController.navigate("settings/backup_restore")
                            }
                        )
                    }

                    1 -> {
                        InterfacePage(
                            navController = navController,
                            newInterfaceStyle = newInterfaceStyle,
                            onNewInterfaceStyleChange = onNewInterfaceStyleChange,
                            filter = filter,
                            onFilterChange = { newFilter -> filter = newFilter },
                            darkMode = darkMode,
                            onDarkModeChange = onDarkModeChange,
                            pureBlack = pureBlack,
                            onPureBlackChange = onPureBlackChange
                        )
                    }

                    2 -> {
                        AccountPage(
                            navController = navController,
                            isLoggedIn = isLoggedIn,
                            accountName = accountName,
                            accountEmail = accountEmail,
                            accountChannelHandle = accountChannelHandle,
                            innerTubeCookie = innerTubeCookie,
                            onInnerTubeCookieChange = onInnerTubeCookieChange,
                            ytmSync = ytmSync,
                            onYtmSyncChange = onYtmSyncChange
                        )
                    }

                    3 -> {
                        LocalMediaPage(
                            navController = navController,
                            localLibEnable = localLibEnable,
                            onLocalLibEnableChange = onLocalLibEnableChange,
                            autoScan = autoScan,
                            onAutoScanChange = onAutoScanChange
                        )
                    }

                    4 -> {
                        FinalPage(
                            uriHandler = uriHandler,
                            onFinish = {
                                onFirstSetupPassedChange(true)
                                navController.navigateUp()
                            }
                        )
                    }
                }
            }

            if (position == 0 || position == MAX_POS) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    onClick = {
                        if (position == 0) {
                            position += 1
                        } else {
                            onFirstSetupPassedChange(true)
                            navController.navigateUp()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomePage(
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onRestoreBackup: () -> Unit
) {
    val welcomeFeatures = listOf(
        Feature(
            title = "YouTube Music Integration",
            description = "Access your favorite tracks seamlessly",
            icon = Icons.Rounded.MusicNote
        ),
        Feature(
            title = "AD-Free Experience",
            description = "Enjoy uninterrupted music playback",
            icon = Icons.Rounded.Block
        ),
        Feature(
            title = "Local Music Support",
            description = "Play your downloaded tracks anywhere",
            icon = Icons.Rounded.SdCard
        ),
        Feature(
            title = "Cross-Platform Sync",
            description = "Keep your music in harmony across devices",
            icon = Icons.Rounded.Sync
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.launcher_monochrome),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary, BlendMode.SrcIn),
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                        NavigationBarDefaults.Elevation
                    )
                )
                .clickable { }
        )

        Text(
            text = "Welcome to OuterTune",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            welcomeFeatures.forEach { feature ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = feature.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Maybe add quick restore from backup here
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        ) {
            TextButton(
                onClick = onRestoreBackup
            ) {
                Text(
                    text = "I have a backup",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            TextButton(
                onClick = onSkip
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun InterfacePage(
    navController: NavController,
    newInterfaceStyle: Boolean,
    onNewInterfaceStyleChange: (Boolean) -> Unit,
    filter: LibraryFilter,
    onFilterChange: (LibraryFilter) -> Unit,
    darkMode: DarkMode,
    onDarkModeChange: (DarkMode) -> Unit,
    pureBlack: Boolean,
    onPureBlackChange: (Boolean) -> Unit
) {
    val dummySong = Song(
        artists = listOf(
            ArtistEntity(
                id = "uwu",
                name = "Artist",
                isLocal = true
            )
        ),
        song = SongEntity(
            id = "owo",
            title = "Title",
            duration = 310,
            inLibrary = LocalDateTime.now(),
            isLocal = true,
            localPath = "/storage"
        ),
    )

    val dummySongs = ArrayList<Song>()
    for (i in 0..4) {
        dummySongs.add(dummySong)
    }

    Text(
        text = "Interface",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    )

    // interface style
    SwitchPreference(
        title = { Text(stringResource(R.string.new_interface)) },
        icon = { Icon(Icons.Rounded.Palette, null) },
        checked = newInterfaceStyle,
        onCheckedChange = onNewInterfaceStyleChange
    )

    Column(
        Modifier.background(MaterialTheme.colorScheme.secondary.copy(0.2f))
    ) {
        Spacer(Modifier.height(24.dp))

        if (newInterfaceStyle) {
            // for new layout
            val filterString = when (filter) {
                LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                LibraryFilter.SONGS -> stringResource(R.string.songs)
                LibraryFilter.FOLDERS -> stringResource(R.string.folders)
                LibraryFilter.ALL -> ""
            }

            val defaultFilter: Collection<Pair<LibraryFilter, String>> =
                decodeTabString("HSABL").map {
                    when (it) {
                        NavigationTab.ALBUM -> LibraryFilter.ALBUMS to stringResource(R.string.albums)
                        NavigationTab.ARTIST -> LibraryFilter.ARTISTS to stringResource(R.string.artists)
                        NavigationTab.PLAYLIST -> LibraryFilter.PLAYLISTS to stringResource(R.string.playlists)
                        NavigationTab.SONG -> LibraryFilter.SONGS to stringResource(R.string.songs)
                        NavigationTab.FOLDERS -> LibraryFilter.FOLDERS to stringResource(R.string.folders)
                        else -> LibraryFilter.ALL to stringResource(R.string.home) // there is no all filter, use as null value
                    }
                }.filterNot { it.first == LibraryFilter.ALL }

            val chips = remember { SnapshotStateList<Pair<LibraryFilter, String>>() }

            var filterSelected by remember {
                mutableStateOf(filter)
            }

            LaunchedEffect(Unit) {
                if (filter == LibraryFilter.ALL)
                    chips.addAll(defaultFilter)
                else
                    chips.add(filter to filterString)
            }

            val animatorDurationScale = Settings.Global.getFloat(
                LocalContext.current.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f
            ).toLong()

            suspend fun animationBasedDelay(value: Long) {
                delay(value * animatorDurationScale)
            }

            // Update the filters list in a proper way so that the animations of the LazyRow can work.
            LaunchedEffect(filter) {
                val filterIndex = defaultFilter.indexOf(defaultFilter.find { it.first == filter })
                val currentPairIndex = if (chips.size > 0) defaultFilter.indexOf(chips[0]) else -1
                val currentPair = if (chips.size > 0) chips[0] else null

                if (filter == LibraryFilter.ALL) {
                    defaultFilter.reversed().fastForEachIndexed { index, it ->
                        val curFilterIndex = defaultFilter.indexOf(it)
                        if (!chips.contains(it)) {
                            chips.add(0, it)
                            if (currentPairIndex > curFilterIndex) animationBasedDelay(100)
                            else {
                                currentPair?.let {
                                    animationBasedDelay(2)
                                    chips.move(chips.indexOf(it), 0)
                                }
                                animationBasedDelay(80 + (index * 30).toLong())
                            }
                        }
                    }
                    animationBasedDelay(100)
                    filterSelected = LibraryFilter.ALL
                } else {
                    filterSelected = filter
                    chips.filter { it.first != filter }
                        .onEachIndexed { index, it ->
                            if (chips.contains(it)) {
                                chips.remove(it)
                                if (index > filterIndex) animationBasedDelay(150 + 30 * index.toLong())
                                else animationBasedDelay(80)
                            }
                        }
                }
            }

            // filter chips
            Row {
                ChipsLazyRow(
                    chips = chips,
                    currentValue = filter,
                    onValueUpdate = { newFilter ->
                        onFilterChange(if (filter == LibraryFilter.ALL) newFilter else LibraryFilter.ALL)
                    },
                    modifier = Modifier.weight(1f),
                    selected = { it == filterSelected }
                )

                if (filter != LibraryFilter.SONGS) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.List,
                            contentDescription = null
                        )
                    }
                }
            }
        } else {
            // for classic layout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                SortHeader(
                    sortType = SongSortType.NAME,
                    sortDescending = true,
                    onSortTypeChange = { },
                    onSortDescendingChange = { },
                    sortTypeText = { R.string.sort_by_name }
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = pluralStringResource(R.plurals.n_song, dummySongs.size, dummySongs.size),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // sample UI
        Column {
            dummySongs.forEach { song ->
                SongListItem(
                    song = song,
                    onPlay = {},
                    onSelectedChange = {},
                    inSelectMode = null,
                    isSelected = false,
                    navController = navController,
                    enableSwipeToQueue = false,
                    disableShowMenu = true
                )
            }
        }

        val navigationItems =
            if (!newInterfaceStyle) Screens.getScreens("HSABL") else Screens.MainScreensNew
        NavigationBar(Modifier) {
            navigationItems.fastForEach { screen ->
                NavigationBarItem(
                    selected = false,
                    icon = {
                        Icon(
                            screen.icon,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(screen.titleId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {}
                )
            }
        }
    }

    // light/dark theme
    EnumListPreference(
        title = { Text(stringResource(R.string.dark_theme)) },
        icon = { Icon(Icons.Rounded.DarkMode, null) },
        selectedValue = darkMode,
        onValueSelected = onDarkModeChange,
        valueText = {
            when (it) {
                DarkMode.ON -> stringResource(R.string.dark_theme_on)
                DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
            }
        }
    )
    SwitchPreference(
        title = { Text(stringResource(R.string.pure_black)) },
        icon = { Icon(Icons.Rounded.Contrast, null) },
        checked = pureBlack,
        onCheckedChange = onPureBlackChange
    )
}
@Composable
private fun AccountPage(
    navController: NavController,
    isLoggedIn: Boolean,
    accountName: String,
    accountEmail: String,
    accountChannelHandle: String,
    innerTubeCookie: String,
    onInnerTubeCookieChange: (String) -> Unit,
    ytmSync: Boolean,
    onYtmSyncChange: (Boolean) -> Unit
) {
    var showToken by remember { mutableStateOf(false) }
    var showTokenEditor by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title, Subtitle, and Icon
        Icon(
            imageVector = Icons.Rounded.AccountCircle,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Connect Your Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = "Sync with your music services",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Login/Account Info Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            PreferenceEntry(
                title = { Text(if (isLoggedIn) accountName else stringResource(R.string.login)) },
                description = if (isLoggedIn) {
                    accountEmail.takeIf { it.isNotEmpty() }
                        ?: accountChannelHandle.takeIf { it.isNotEmpty() }
                } else null,
                icon = { Icon(Icons.Rounded.Person, null) },
                onClick = { navController.navigate("login") }
            )
        }

        // Add spacing between cards
        Spacer(modifier = Modifier.height(16.dp))

        // Logout Card (if logged in)
        if (isLoggedIn) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.logout)) },
                    icon = { Icon(Icons.AutoMirrored.Rounded.Logout, null) },
                    onClick = { onInnerTubeCookieChange("") }
                )
            }

            // Add spacing between cards
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Token Management Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            PreferenceEntry(
                title = {
                    if (showToken) {
                        Text(stringResource(R.string.token_shown))
                        Text(
                            text = if (isLoggedIn) innerTubeCookie else stringResource(R.string.not_logged_in),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    } else {
                        Text(stringResource(R.string.token_hidden))
                    }
                },
                onClick = {
                    if (!showToken) {
                        showToken = true
                    } else {
                        showTokenEditor = true
                    }
                }
            )
        }

        // Add spacing between cards
        Spacer(modifier = Modifier.height(16.dp))

        // YTM Sync Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.ytm_sync)) },
                icon = { Icon(Icons.Rounded.Lyrics, null) },
                checked = ytmSync,
                onCheckedChange = onYtmSyncChange,
                isEnabled = isLoggedIn
            )
        }

        if (showTokenEditor) {
            TokenEditorDialog(
                initialValue = innerTubeCookie,
                onDone = { newToken ->
                    onInnerTubeCookieChange(newToken)
                    showTokenEditor = false
                },
                onDismiss = { showTokenEditor = false },
                modifier = Modifier
            )
        }
    }
}
@Composable
private fun LocalMediaPage(
    navController: NavController,
    localLibEnable: Boolean,
    onLocalLibEnableChange: (Boolean) -> Unit,
    autoScan: Boolean,
    onAutoScanChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title, Subtitle, and Icon
        Icon(
            imageVector = Icons.Rounded.LibraryMusic,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Local Media Setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = "Import your music collection",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Local Library Enable Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.local_library_enable_title)) },
                description = stringResource(R.string.local_library_enable_description),
                icon = { Icon(Icons.Rounded.SdCard, null) },
                checked = localLibEnable,
                onCheckedChange = onLocalLibEnableChange
            )
        }

        // Add spacing between cards
        Spacer(modifier = Modifier.height(16.dp))

        // Automatic Scanner Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.auto_scanner_title)) },
                description = stringResource(R.string.auto_scanner_description),
                icon = { Icon(Icons.Rounded.Autorenew, null) },
                checked = autoScan,
                onCheckedChange = onAutoScanChange,
                isEnabled = localLibEnable
            )
        }

        // Add spacing between cards
        Spacer(modifier = Modifier.height(16.dp))

        // Manual Scan Button (if local library is enabled)
        if (localLibEnable) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                ElevatedButton(
                    onClick = { navController.navigate("settings/local") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Scan for Local Music")
                }
            }
        }
    }
}
@Composable
private fun FinalPage(
    uriHandler: UriHandler,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Text(
            text = "OuterTune is set up and ready to use. Explore your music, discover new tracks, and enjoy the experience!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            FilledTonalIconButton(
                onClick = { uriHandler.openUri("https://github.com/DD3Boh/OuterTune") },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.github),
                    contentDescription = "GitHub",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Text(
            text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) | ${BuildConfig.FLAVOR}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}