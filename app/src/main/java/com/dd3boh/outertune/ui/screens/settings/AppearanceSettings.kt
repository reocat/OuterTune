/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.Tab
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.DarkModeKey
import com.dd3boh.outertune.constants.DefaultOpenTabKey
import com.dd3boh.outertune.constants.DefaultOpenTabNewKey
import com.dd3boh.outertune.constants.DynamicThemeKey
import com.dd3boh.outertune.constants.EnabledTabsKey
import com.dd3boh.outertune.constants.FlatSubfoldersKey
import com.dd3boh.outertune.constants.ListItemHeight
import com.dd3boh.outertune.constants.NewInterfaceKey
import com.dd3boh.outertune.constants.PlayerBackgroundStyleKey
import com.dd3boh.outertune.constants.PureBlackKey
import com.dd3boh.outertune.constants.ShowLikedAndDownloadedPlaylist
import com.dd3boh.outertune.constants.SliderStyle
import com.dd3boh.outertune.constants.SliderStyleKey
import com.dd3boh.outertune.constants.SlimNavBarKey
import com.dd3boh.outertune.constants.SwipeToQueueKey
import com.dd3boh.outertune.constants.ThumbnailCornerRadius
import com.dd3boh.outertune.extensions.move
import com.dd3boh.outertune.ui.component.ActionPromptDialog
import com.dd3boh.outertune.ui.component.DefaultDialog
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.InfoLabel
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.decodeTabString
import com.dd3boh.outertune.utils.encodeTabString
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import me.saket.squiggles.SquigglySlider
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * H: Home
 * S: Songs
 * F: Folders
 * A: Artists
 * B: Albums
 * L: Playlists
 *
 * Not/won't implement
 * P: Player
 * Q: Queue
 * E: Search
 */
const val DEFAULT_ENABLED_TABS = "HSABLF"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)
    val (playerBackground, onPlayerBackgroundChange) = rememberEnumPreference(key = PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.DEFAULT)
    val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)
    val (enabledTabs, onEnabledTabsChange) = rememberPreference(EnabledTabsKey, defaultValue = DEFAULT_ENABLED_TABS)
    val (defaultOpenTab, onDefaultOpenTabChange) = rememberEnumPreference(DefaultOpenTabKey, defaultValue = NavigationTab.HOME)
    val (defaultOpenTabNew, onDefaultOpenTabNewChange) = rememberEnumPreference(DefaultOpenTabNewKey, defaultValue = NavigationTabNew.HOME)
    val (sliderStyle, onSliderStyleChange) = rememberEnumPreference(SliderStyleKey, defaultValue = SliderStyle.DEFAULT)
    val (newInterfaceStyle, onNewInterfaceStyleChange) = rememberPreference(key = NewInterfaceKey, defaultValue = true)
    val (showLikedAndDownloadedPlaylist, onShowLikedAndDownloadedPlaylistChange) = rememberPreference(key = ShowLikedAndDownloadedPlaylist, defaultValue = true)
    val (swipe2Queue, onSwipe2QueueChange) = rememberPreference(SwipeToQueueKey, defaultValue = true)
    val (slimNav, onSlimNavChange) = rememberPreference(SlimNavBarKey, defaultValue = false)
    val (flatSubfolders, onFlatSubfoldersChange) = rememberPreference(FlatSubfoldersKey, defaultValue = true)

    val availableBackgroundStyles = PlayerBackgroundStyle.entries.filter {
        it != PlayerBackgroundStyle.BLUR || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    // configurable tabs
    var showTabArrangement by rememberSaveable {
        mutableStateOf(false)
    }
    val mutableTabs = remember { mutableStateListOf<NavigationTab>() }


    val lazySongsListState = rememberLazyListState()
    var dragInfo by remember {
        mutableStateOf<Pair<Int, Int>?>(null)
    }
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazySongsListState,
        scrollThresholdPadding = WindowInsets.systemBars.add(
            WindowInsets(
                top = ListItemHeight,
                bottom = ListItemHeight
            )
        ).asPaddingValues()
    ) { from, to ->
        val currentDragInfo = dragInfo
        dragInfo = if (currentDragInfo == null) {
            from.index to to.index
        } else {
            currentDragInfo.first to to.index
        }
        mutableTabs.move(from.index, to.index)
    }

    fun updateTabs() {
        mutableTabs.apply {
            clear()

            val enabled = decodeTabString(enabledTabs)
            addAll(enabled)
            add(NavigationTab.NULL)
            addAll(NavigationTab.entries.filter { item -> enabled.none { it == item || item == NavigationTab.NULL } })
        }
    }

    LaunchedEffect(showTabArrangement) {
        updateTabs()
    }

    var showSliderOptionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSliderOptionDialog) {
        DefaultDialog(
            buttons = {
                TextButton(
                    onClick = { showSliderOptionDialog = false }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            onDismiss = {
                showSliderOptionDialog = false
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, if (sliderStyle == SliderStyle.DEFAULT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .clickable {
                            onSliderStyleChange(SliderStyle.DEFAULT)
                            showSliderOptionDialog = false
                        }
                        .padding(16.dp)
                ) {
                    var sliderValue by remember {
                        mutableFloatStateOf(0.5f)
                    }
                    Slider(
                        value = sliderValue,
                        valueRange = 0f..1f,
                        onValueChange = {
                            sliderValue = it
                        },
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {}
                                )
                            }
                    )

                    Text(
                        text = stringResource(R.string.default_),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, if (sliderStyle == SliderStyle.SQUIGGLY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .clickable {
                            onSliderStyleChange(SliderStyle.SQUIGGLY)
                            showSliderOptionDialog = false
                        }
                        .padding(16.dp)
                ) {
                    var sliderValue by remember {
                        mutableFloatStateOf(0.5f)
                    }
                    SquigglySlider(
                        value = sliderValue,
                        valueRange = 0f..1f,
                        onValueChange = {
                            sliderValue = it
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = stringResource(R.string.squiggly),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        PreferenceGroupTitle(
            title = stringResource(R.string.theme)
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_dynamic_theme)) },
            icon = { Icon(Icons.Rounded.Palette, null) },
            checked = dynamicTheme,
            onCheckedChange = onDynamicThemeChange
        )
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
        EnumListPreference(
            title = { Text(stringResource(R.string.player_background_style)) },
            icon = { Icon(Icons.Rounded.BlurOn, null) },
            selectedValue = playerBackground,
            onValueSelected = onPlayerBackgroundChange,
            valueText = {
                when (it) {
                    PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.player_background_default)
                    PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.player_background_gradient)
                    PlayerBackgroundStyle.BLUR -> stringResource(R.string.player_background_blur)
                }
            },
            values = availableBackgroundStyles
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_layout)
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.new_interface)) },
            icon = { Icon(Icons.Rounded.Palette, null) },
            checked = newInterfaceStyle,
            onCheckedChange = onNewInterfaceStyleChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.show_liked_and_downloaded_playlist)) },
            icon = { Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, null) },
            checked = showLikedAndDownloadedPlaylist,
            onCheckedChange = onShowLikedAndDownloadedPlaylistChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.swipe2Queue)) },
            description = stringResource(R.string.swipe2Queue_description),
            icon = { Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, null) },
            checked = swipe2Queue,
            onCheckedChange = onSwipe2QueueChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.slim_navbar_title)) },
            description = stringResource(R.string.slim_navbar_description),
            icon = { Icon(Icons.Rounded.MoreHoriz, null) },
            checked = slimNav,
            onCheckedChange = onSlimNavChange
        )

        PreferenceEntry(
            title = { Text(stringResource(R.string.tab_arrangement)) },
            icon = { Icon(Icons.Rounded.Reorder, null) },
            onClick = {
                showTabArrangement = true
            }
        )

        if (showTabArrangement)
            ActionPromptDialog(
                title = stringResource(R.string.tab_arrangement),
                onDismiss = { showTabArrangement = false },
                onConfirm = {
                    var encoded = encodeTabString(mutableTabs)

                    // reset defaultOpenTab if it got disabled
                    if (!mutableTabs.contains(defaultOpenTab)) {
                        onDefaultOpenTabChange(NavigationTab.HOME)
                    }

                    // home is required
                    if (!encoded.contains('H')) {
                        encoded += "H"
                    }

                    onEnabledTabsChange(encoded)
                    showTabArrangement = false
                },
                onReset = {
                    onEnabledTabsChange(DEFAULT_ENABLED_TABS)
                    updateTabs()
                },
                onCancel = {
                    showTabArrangement = false
                }
            ) {
                // tabs list
                LazyColumn(
                    state = lazySongsListState,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            RoundedCornerShape(ThumbnailCornerRadius)
                        )
                ) {
                    itemsIndexed(
                        items = mutableTabs,
                        key = { _, item -> item.hashCode() }
                    ) { index, tab ->
                        ReorderableItem(
                            state = reorderableState,
                            key = tab.hashCode()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = when (tab) {
                                        NavigationTab.HOME -> stringResource(R.string.home)
                                        NavigationTab.SONG -> stringResource(R.string.songs)
                                        NavigationTab.FOLDERS -> stringResource(R.string.folders)
                                        NavigationTab.ARTIST -> stringResource(R.string.artists)
                                        NavigationTab.ALBUM -> stringResource(R.string.albums)
                                        NavigationTab.PLAYLIST -> stringResource(R.string.playlists)
                                        else -> {
                                            stringResource(R.string.tab_arrangement_disable_tip)
                                        }
                                    }
                                )
                                Icon(
                                    imageVector = Icons.Rounded.DragHandle,
                                    contentDescription = null,
                                    modifier = Modifier.draggableHandle()
                                )
                            }
                        }
                    }
                }

                InfoLabel(stringResource(R.string.tab_arrangement_home_required))
            }

        if (newInterfaceStyle) {
            EnumListPreference(
                title = { Text(stringResource(R.string.default_open_tab)) },
                icon = { Icon(Icons.Rounded.Tab, null) },
                selectedValue = defaultOpenTabNew,
                onValueSelected = onDefaultOpenTabNewChange,
                valueText = {
                    when (it) {
                        NavigationTabNew.HOME -> stringResource(R.string.home)
                        NavigationTabNew.LIBRARY -> stringResource(R.string.library)
                    }
                }
            )
        } else {
            EnumListPreference(
                title = { Text(stringResource(R.string.default_open_tab)) },
                icon = { Icon(Icons.Rounded.Tab, null)},
                selectedValue = defaultOpenTab,
                onValueSelected = onDefaultOpenTabChange,
                values = NavigationTab.entries.filter { it != NavigationTab.NULL },
                valueText = {
                    when (it) {
                        NavigationTab.HOME -> stringResource(R.string.home)
                        NavigationTab.SONG -> stringResource(R.string.songs)
                        NavigationTab.FOLDERS -> stringResource(R.string.folders)
                        NavigationTab.ARTIST -> stringResource(R.string.artists)
                        NavigationTab.ALBUM -> stringResource(R.string.albums)
                        NavigationTab.PLAYLIST -> stringResource(R.string.playlists)
                        else -> ""
                    }
                }
            )
        }

        // flatten subfolders
        SwitchPreference(
            title = { Text(stringResource(R.string.flat_subfolders_title)) },
            description = stringResource(R.string.flat_subfolders_description),
            icon = { Icon(Icons.Rounded.FolderCopy, null) },
            checked = flatSubfolders,
            onCheckedChange = onFlatSubfoldersChange
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.misc)
        )

        PreferenceEntry(
            title = { Text(stringResource(R.string.player_slider_style)) },
            description = when (sliderStyle) {
                SliderStyle.DEFAULT -> stringResource(R.string.default_)
                SliderStyle.SQUIGGLY -> stringResource(R.string.squiggly)
            },
            icon = { Icon(painterResource(R.drawable.sliders), null) },
            onClick = {
                showSliderOptionDialog = true
            }
        )
//        EnumListPreference(
//            title = { Text(stringResource(R.string.slider_style)) },
//            icon = { Icon(painterResource(R.drawable.sliders), null) },
//            selectedValue = sliderStyle,
//            onValueSelected = onSliderStyleChange,
//            valueText = {
//                when (it) {
//                    SliderStyle.DEFAULT -> stringResource(R.string.default_)
//                    SliderStyle.SQUIGGLY -> stringResource(R.string.squiggly)
//                }
//            }
//        )
    }
    
    TopAppBar(
        title = { Text(stringResource(R.string.appearance)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

enum class DarkMode {
    ON, OFF, AUTO
}

enum class PlayerBackgroundStyle {
    DEFAULT, GRADIENT, BLUR
}

/**
 * NULL is used to separate enabled and disabled tabs. It should be ignored in regular use
 */
enum class NavigationTab {
    HOME, SONG, FOLDERS, ARTIST, ALBUM, PLAYLIST, NULL
}
enum class NavigationTabNew {
    HOME, LIBRARY
}

enum class LyricsPosition {
    LEFT, CENTER, RIGHT
}