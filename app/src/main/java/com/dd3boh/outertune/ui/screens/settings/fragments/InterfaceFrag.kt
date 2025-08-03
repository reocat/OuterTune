package com.dd3boh.outertune.ui.screens.settings.fragments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.Tab
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.ContentCountryKey
import com.dd3boh.outertune.constants.ContentLanguageKey
import com.dd3boh.outertune.constants.CountryCodeToName
import com.dd3boh.outertune.constants.DEFAULT_ENABLED_FILTERS
import com.dd3boh.outertune.constants.DEFAULT_ENABLED_TABS
import com.dd3boh.outertune.constants.DefaultOpenTabKey
import com.dd3boh.outertune.constants.EnabledFiltersKey
import com.dd3boh.outertune.constants.EnabledTabsKey
import com.dd3boh.outertune.constants.GridCellSize
import com.dd3boh.outertune.constants.GridCellSizeKey
import com.dd3boh.outertune.constants.LanguageCodeToName
import com.dd3boh.outertune.constants.ListItemHeight
import com.dd3boh.outertune.constants.SYSTEM_DEFAULT
import com.dd3boh.outertune.constants.ScrollToCurrentSongKey
import com.dd3boh.outertune.constants.SliderStyle
import com.dd3boh.outertune.constants.SliderStyleKey
import com.dd3boh.outertune.constants.SwipeSensitivityKey
import com.dd3boh.outertune.constants.SwipeToQueueKey
import com.dd3boh.outertune.constants.SwipeToSkip
import com.dd3boh.outertune.constants.ThumbnailCornerRadius
import com.dd3boh.outertune.extensions.move
import com.dd3boh.outertune.ui.component.ActionPromptDialog
import com.dd3boh.outertune.ui.component.DefaultDialog
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.InfoLabel
import com.dd3boh.outertune.ui.component.ListPreference
import com.dd3boh.outertune.ui.component.PlayerSliderTrack
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.SliderDialog
import com.dd3boh.outertune.ui.component.SquigglySlider
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.screens.Screens
import com.dd3boh.outertune.ui.screens.Screens.LibraryFilter
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import com.zionhuang.innertube.YouTube
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GridCellsSizeFrag() {
    val (gridCellSize, onGridCellSizeChange) = rememberEnumPreference(GridCellSizeKey, defaultValue = GridCellSize.SMALL)
    EnumListPreference(
        title = { Text(stringResource(R.string.grid_cell_size)) },
        icon = { Icon(Icons.Outlined.GridView,null) },
        selectedValue = gridCellSize,
        onValueSelected = onGridCellSizeChange,
        valueText = {
            when (it) {
                GridCellSize.SMALL -> stringResource(R.string.small)
                GridCellSize.BIG -> stringResource(R.string.big)
            }
        },
    )
}

@Composable
fun ColumnScope.TabArrangementFrag() {
    val (enabledTabs, onEnabledTabsChange) = rememberPreference(EnabledTabsKey, defaultValue = DEFAULT_ENABLED_TABS)
    val (enabledFilters, onEnabledFiltersChange) = rememberPreference(
        EnabledFiltersKey,
        defaultValue = DEFAULT_ENABLED_FILTERS
    )
    val (defaultOpenTab, onDefaultOpenTabChange) = rememberPreference(DefaultOpenTabKey, defaultValue = "home")

    var dragInfo by remember {
        mutableStateOf<Pair<Int, Int>?>(null)
    }

    /**
     * ---------------------------
     * Configurable tabs
     * ---------------------------
     */

    var showTabArrangement by rememberSaveable {
        mutableStateOf(false)
    }
    val mutableTabs = remember { mutableStateListOf<Pair<Screens, Boolean>>() }
    val lazyTabsListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyTabsListState,
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

            val enabled = Screens.getScreens(enabledTabs)
            addAll(enabled.map { it to true })
            addAll(
                Screens.getAllScreens().filterNot { it in enabled }.map { it to false }
            )
        }
    }

    LaunchedEffect(showTabArrangement, enabledTabs) {
        updateTabs()
    }


    /**
     * ---------------------------
     * Configurable filters
     * ---------------------------
     */


    var showFilterArrangement by rememberSaveable {
        mutableStateOf(false)
    }
    val mutableFilters = remember { mutableStateListOf<Pair<LibraryFilter, Boolean>>() }
    val lazyFiltersListState = rememberLazyListState()
    val filtersReorderableState = rememberReorderableLazyListState(
        lazyListState = lazyFiltersListState,
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
        mutableFilters.move(from.index, to.index)
    }

    fun updateFilters() {
        mutableFilters.apply {
            clear()

            val enabled = Screens.getFilters(enabledFilters)
            addAll(enabled.map { it to true })
            addAll(
                LibraryFilter.entries.filterNot { it in enabled }.map { it to false }
                    .filterNot { it.first == LibraryFilter.ALL })
        }
    }

    LaunchedEffect(showFilterArrangement, enabledFilters) {
        updateFilters()
    }


    PreferenceEntry(
        title = { Text(stringResource(R.string.tab_arrangement)) },
        icon = { Icon(Icons.Outlined.Reorder, null) },
        onClick = {
            showTabArrangement = true
        },
        isFirst = true
    )
    PreferenceEntry(
        title = { Text(stringResource(R.string.filter_arrangement)) },
        icon = { Icon(Icons.Outlined.Reorder, null) },
        onClick = {
            showFilterArrangement = true
        },
        isLast = true
    )


    /**
     * ---------------------------
     * Dialogs
     * ---------------------------
     */


    if (showTabArrangement) {
        ActionPromptDialog(
            title = stringResource(R.string.tab_arrangement),
            onDismiss = { showTabArrangement = false },
            onConfirm = {
                var encoded = Screens.encodeScreens(mutableTabs.filter { it.second }.map { it.first })

                // reset defaultOpenTab if it got disabled
                if (Screens.getScreens(encoded).find { it.route == defaultOpenTab } == null)
                    onDefaultOpenTabChange(Screens.Home.route)

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
                state = lazyTabsListState,
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
                        val isHome = tab.first == Screens.Home
                        fun onChecked() {
                            if (!isHome) {
                                mutableTabs[mutableTabs.indexOf(tab)] = tab.copy(second = !tab.second)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isHome) { onChecked() }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = tab.second,
                                enabled = !isHome,
                                onCheckedChange = { onChecked() }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(tab.first.titleId),
                                modifier = Modifier.weight(1f),
                                color = if (isHome)
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Outlined.DragHandle,
                                contentDescription = null,
                                modifier = Modifier.draggableHandle()
                            )
                        }
                    }
                }
            }

            InfoLabel(stringResource(R.string.tab_arrangement_home_required))
        }
    }

    if (showFilterArrangement) {
        ActionPromptDialog(
            title = stringResource(R.string.filter_arrangement),
            onDismiss = { showFilterArrangement = false },
            onConfirm = {
                val encoded = Screens.encodeFilters(mutableFilters.filter { it.second }.map { it.first })

                onEnabledFiltersChange(encoded)
                showFilterArrangement = false
            },
            onReset = {
                onEnabledFiltersChange(DEFAULT_ENABLED_FILTERS)
                updateFilters()
            },
            onCancel = {
                showFilterArrangement = false
            }
        ) {
            // tabs list
            LazyColumn(
                state = lazyFiltersListState,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        RoundedCornerShape(ThumbnailCornerRadius)
                    )
            ) {
                itemsIndexed(
                    items = mutableFilters,
                    key = { _, item -> item.hashCode() }
                ) { index, filter ->
                    ReorderableItem(
                        state = filtersReorderableState,
                        key = filter.hashCode()
                    ) {
                        fun onChecked() {
                            mutableFilters[mutableFilters.indexOf(filter)] = filter.copy(second = !filter.second)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChecked() }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = filter.second,
                                onCheckedChange = { onChecked() }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (filter.first) {
                                    LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                                    LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                                    LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                                    LibraryFilter.SONGS -> stringResource(R.string.songs)
                                    LibraryFilter.FOLDERS -> stringResource(R.string.folders)
                                    LibraryFilter.ALL -> stringResource(R.string.tab_arrangement_disable_tip)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Outlined.DragHandle,
                                contentDescription = null,
                                modifier = Modifier.draggableHandle()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.TabExtrasFrag() {
    val enabledTabs by rememberPreference(EnabledTabsKey, defaultValue = DEFAULT_ENABLED_TABS)
    val (defaultOpenTab, onDefaultOpenTabChange) = rememberPreference(DefaultOpenTabKey, defaultValue = "home")

    ListPreference(
        title = { Text(stringResource(R.string.default_open_tab)) },
        icon = { Icon(Icons.Outlined.Tab, null) },
        selectedValue = Screens.getAllScreens().find { it.route == defaultOpenTab } ?: Screens.Home,
        onValueSelected = { screen ->
            onDefaultOpenTabChange(screen.route)
        },
        values = Screens.getAllScreens().filter { Screens.getScreens(enabledTabs).contains(it) },
        valueText = { stringResource(it.titleId) }
    )
}

@Composable
fun ColumnScope.SwipeGesturesFrag() {
    val (swipeToSkip, onSwipeToSkipChange) = rememberPreference(SwipeToSkip, defaultValue = true)
    val (swipe2Queue, onSwipe2QueueChange) = rememberPreference(SwipeToQueueKey, defaultValue = true)
    val (swipeSensitivity, onSwipeSensitivityChange) = rememberPreference(
        SwipeSensitivityKey,
        defaultValue = 0.73f
    )
    val (scrollToCurrentSong, onScrollToCurrentSongChange) = rememberPreference(ScrollToCurrentSongKey, defaultValue = false)

    SwitchPreference(
        title = { Text(stringResource(R.string.swipe2Queue)) },
        description = stringResource(R.string.swipe2Queue_description),
        icon = { Icon(Icons.AutoMirrored.Outlined.PlaylistAdd, null) },
        checked = swipe2Queue,
        onCheckedChange = onSwipe2QueueChange,
        isFirst = true
    )

    SwitchPreference(
        title = { Text(stringResource(R.string.swipe_to_skip_title)) },
        description = stringResource(R.string.swipe_to_skip_description),
        icon = { Icon(Icons.Outlined.Swipe, null) },
        checked = swipeToSkip,
        onCheckedChange = onSwipeToSkipChange,
        isMiddle = true
    )

    AnimatedVisibility(swipeToSkip) {
        Column {
            var showSensitivityDialog by rememberSaveable { mutableStateOf(false) }

            if (showSensitivityDialog) {
                SliderDialog(
                    title = stringResource(R.string.swipe_sensitivity),
                    initialValue = (swipeSensitivity * 100).roundToInt(),
                    defaultValue = 73,
                    valueRange = 0f..100f,
                    valueSuffix = "%",
                    onDismiss = { showSensitivityDialog = false },
                    onConfirm = { newValue ->
                        onSwipeSensitivityChange(newValue / 100f)
                        showSensitivityDialog = false
                    },
                    onReset = {
                        onSwipeSensitivityChange(0.73f)
                    }
                )
            }

            PreferenceEntry(
                title = { Text(stringResource(R.string.swipe_sensitivity)) },
                description = stringResource(R.string.sensitivity_percentage, (swipeSensitivity * 100).roundToInt()),
                icon = { Icon(Icons.Outlined.Tune, null) },
                onClick = { showSensitivityDialog = true },
                isMiddle = true
            )
        }
    }

    SwitchPreference(
        title = { Text(stringResource(R.string.queue_scroll_to_current_song_title)) },
        description = stringResource(R.string.queue_scroll_to_current_song_description),
        icon = { Icon(Icons.AutoMirrored.Outlined.ListAlt, null) },
        checked = scrollToCurrentSong,
        onCheckedChange = onScrollToCurrentSongChange,
        isLast = true
    )
}

@Composable
fun ColumnScope.LocalizationFrag() {
    val (contentLanguage, onContentLanguageChange) = rememberPreference(
        key = ContentLanguageKey,
        defaultValue = "system"
    )
    val (contentCountry, onContentCountryChange) = rememberPreference(key = ContentCountryKey, defaultValue = "system")

    ListPreference(
        title = { Text(stringResource(R.string.content_language)) },
        icon = { Icon(Icons.Outlined.Language, null) },
        selectedValue = contentLanguage,
        values = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
        valueText = {
            LanguageCodeToName.getOrElse(it) {
                stringResource(R.string.system_default)
            }
        },
        onValueSelected = { newValue ->
            val locale = Locale.getDefault()
            val languageTag = locale.toLanguageTag().replace("-Hant", "")

            YouTube.locale = YouTube.locale.copy(
                hl = newValue.takeIf { it != SYSTEM_DEFAULT }
                    ?: locale.language.takeIf { it in LanguageCodeToName }
                    ?: languageTag.takeIf { it in LanguageCodeToName }
                    ?: "en"
            )
            onContentLanguageChange(newValue)
        },
        isFirst = true
    )
    ListPreference(
        title = { Text(stringResource(R.string.content_country)) },
        icon = { Icon(Icons.Outlined.LocationOn, null) },
        selectedValue = contentCountry,
        values = listOf(SYSTEM_DEFAULT) + CountryCodeToName.keys.toList(),
        valueText = {
            CountryCodeToName.getOrElse(it) {
                stringResource(R.string.system_default)
            }
        },
        onValueSelected = { newValue ->
            val locale = Locale.getDefault()

            YouTube.locale = YouTube.locale.copy(
                gl = newValue.takeIf { it != SYSTEM_DEFAULT }
                    ?: locale.country.takeIf { it in CountryCodeToName }
                    ?: "US"
            )
            onContentCountryChange(newValue)
        },
        isLast = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderStyleFrag() {
    val (sliderStyle, onSliderStyleChange) = rememberEnumPreference(SliderStyleKey, defaultValue = SliderStyle.DEFAULT)
    var showSliderOptionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    PreferenceEntry(
        title = { Text(stringResource(R.string.player_slider_style)) },
        description = when (sliderStyle) {
            SliderStyle.DEFAULT -> stringResource(R.string.default_)
            SliderStyle.SQUIGGLY -> stringResource(R.string.squiggly)
            SliderStyle.SLIM -> stringResource(R.string.slim)
        },
        icon = { Icon(Icons.Outlined.Tune, null) },
        onClick = {
            showSliderOptionDialog = true
        }
    )

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
                        .border(
                            1.dp,
                            if (sliderStyle == SliderStyle.DEFAULT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
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
                        .border(
                            1.dp,
                            if (sliderStyle == SliderStyle.SQUIGGLY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (sliderStyle == SliderStyle.SLIM) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onSliderStyleChange(SliderStyle.SLIM)
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
                        thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                        track = { sliderState ->
                            PlayerSliderTrack(
                                sliderState = sliderState,
                                colors = SliderDefaults.colors()
                            )
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
                        text = stringResource(R.string.slim),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}