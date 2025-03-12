/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.LocalSyncUtils
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.DevSettingsKey
import com.dd3boh.outertune.constants.FirstSetupPassed
import com.dd3boh.outertune.constants.ScannerImpl
import com.dd3boh.outertune.constants.ScannerImplKey
import com.dd3boh.outertune.constants.SwipeToSkip
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import com.dd3boh.outertune.utils.scanners.LocalMediaScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val syncUtils = LocalSyncUtils.current
    val coroutineScope = rememberCoroutineScope()

    // state variables and such
    val (swipeToSkip, onSwipeToSkipChange) = rememberPreference(SwipeToSkip, defaultValue = true)
    val (devSettings, onDevSettingsChange) = rememberPreference(DevSettingsKey, defaultValue = false)
    val (firstSetupPassed, onFirstSetupPassedChange) = rememberPreference(FirstSetupPassed, defaultValue = false)

    val isSyncingRemotePlaylists by syncUtils.isSyncingRemotePlaylists.collectAsState()
    val isSyncingRemoteAlbums by syncUtils.isSyncingRemoteAlbums.collectAsState()
    val isSyncingRemoteArtists by syncUtils.isSyncingRemoteArtists.collectAsState()
    val isSyncingRemoteSongs by syncUtils.isSyncingRemoteSongs.collectAsState()
    val isSyncingRemoteLikedSongs by syncUtils.isSyncingRemoteLikedSongs.collectAsState()

    val (scannerImpl) = rememberEnumPreference(
        key = ScannerImplKey,
        defaultValue = ScannerImpl.TAGLIB
    )

    var nukeEnabled by remember { mutableStateOf(false) }
    var hapticsTestEnabled by remember { mutableStateOf(false) }
    var colorsTestEnabled by remember { mutableStateOf(false) }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {
        SwitchPreference(
            title = { Text(stringResource(R.string.swipe_to_skip_title)) },
            description = stringResource(R.string.swipe_to_skip_description),
            icon = { Icon(Icons.Rounded.Swipe, null) },
            checked = swipeToSkip,
            onCheckedChange = onSwipeToSkipChange
        )

        // dev settings
        SwitchPreference(
            title = { Text(stringResource(R.string.dev_settings_title)) },
            description = stringResource(R.string.dev_settings_description),
            icon = { Icon(Icons.Rounded.DeveloperMode, null) },
            checked = devSettings,
            onCheckedChange = onDevSettingsChange
        )

        // TODO: move to home screen as button?
        // TODO: rename scanner_manual_btn to sync_manual_btn
        PreferenceEntry(
            title = { Text(stringResource(R.string.scanner_manual_btn)) },
            icon = { Icon(Icons.Rounded.Sync, null) },
            onClick = {
                Toast.makeText(context, context.getString(R.string.sync_progress_active), Toast.LENGTH_SHORT).show()
                coroutineScope.launch(Dispatchers.Main) {
                    syncUtils.syncAll()
                    Toast.makeText(context, context.getString(R.string.sync_progress_success), Toast.LENGTH_SHORT).show()
                }
            }
        )

        SyncProgressItem(stringResource(R.string.songs), isSyncingRemoteSongs)
        SyncProgressItem(stringResource(R.string.liked_songs), isSyncingRemoteLikedSongs)
        SyncProgressItem(stringResource(R.string.artists), isSyncingRemoteArtists)
        SyncProgressItem(stringResource(R.string.albums), isSyncingRemoteAlbums)
        SyncProgressItem(stringResource(R.string.playlists), isSyncingRemotePlaylists)

        if (devSettings) {
            PreferenceGroupTitle(
                title = stringResource(R.string.settings_debug)
            )
            PreferenceEntry(
                title = { Text("DEBUG: Force local to remote artist migration NOW") },
                icon = { Icon(Icons.Rounded.Backup, null) },
                onClick = {
                    Toast.makeText(context, context.getString(R.string.scanner_ytm_link_start), Toast.LENGTH_SHORT).show()
                    coroutineScope.launch(Dispatchers.IO) {
                        val scanner = LocalMediaScanner.getScanner(context, ScannerImpl.TAGLIB)
                        Timber.tag("Settings").d("Force Migrating local artists to YTM (MANUAL TRIGGERED)")
                        scanner.localToRemoteArtist(database)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, context.getString(R.string.scanner_ytm_link_success), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            PreferenceEntry(
                title = { Text("Enter configurator") },
                icon = { Icon(Icons.Rounded.ConfirmationNumber, null) },
                onClick = {
                    onFirstSetupPassedChange(false)
                    runBlocking { // hax. page loads before pref updates
                        delay(500)
                    }
                    navController.navigate("setup_wizard")
                }
            )

            // nukes
            PreferenceEntry(
                title = { Text("Tap to show nuke options") },
                icon = { Icon(Icons.Rounded.ErrorOutline, null) },
                onClick = {
                    nukeEnabled = !nukeEnabled
                }
            )

            if (nukeEnabled) {
                PreferenceEntry(
                    title = { Text("DEBUG: Nuke local lib") },
                    icon = { Icon(Icons.Rounded.ErrorOutline, null) },
                    onClick = {
                        Toast.makeText(context, "Nuking local files from database...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch(Dispatchers.IO) {
                            Timber.tag("Settings").d("Nuke database status:  ${database.nukeLocalData()}")
                        }
                    }
                )
                PreferenceEntry(
                    title = { Text("DEBUG: Nuke local artists") },
                    icon = { Icon(Icons.Rounded.WarningAmber, null) },
                    onClick = {
                        Toast.makeText(context, "Nuking local artists from database...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch(Dispatchers.IO) {
                            Timber.tag("Settings").d("Nuke database status:  ${database.nukeLocalArtists()}")
                        }
                    }
                )
                PreferenceEntry(
                    title = { Text("DEBUG: Nuke dangling format entities") },
                    icon = { Icon(Icons.Rounded.WarningAmber, null) },
                    onClick = {
                        Toast.makeText(context, "Nuking dangling format entities from database...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch(Dispatchers.IO) {
                            Timber.tag("Settings").d("Nuke database status:  ${database.nukeDanglingFormatEntities()}")
                        }
                    }
                )
                PreferenceEntry(
                    title = { Text("DEBUG: Nuke db lyrics") },
                    icon = { Icon(Icons.Rounded.WarningAmber, null) },
                    onClick = {
                        Toast.makeText(context, "Nuking lyrics from database...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch(Dispatchers.IO) {
                            Timber.tag("Settings").d("Nuke database status:  ${database.nukeLocalLyrics()}")
                        }
                    }
                )
                PreferenceEntry(
                    title = { Text("DEBUG: Nuke remote playlists") },
                    icon = { Icon(Icons.Rounded.WarningAmber, null) },
                    onClick = {
                        Toast.makeText(context, "Nuking remote playlists from database...", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch(Dispatchers.IO) {
                            Timber.tag("Settings").d("Nuke database status:  ${database.nukeRemotePlaylists()}")
                        }
                    }
                )
            }
            PreferenceEntry(
                title = { Text("Haptics test") },
                icon = { Icon(Icons.Rounded.Vibration, null) },
                onClick = {
                    hapticsTestEnabled = !hapticsTestEnabled
                }
            )
            PreferenceEntry(
                title = { Text("Material colors test") },
                icon = { Icon(Icons.Rounded.Palette, null) },
                onClick = {
                    colorsTestEnabled = !colorsTestEnabled
                }
            )
            if (hapticsTestEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                HapticsTestSection()
            }
            if (colorsTestEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                MaterialColorsTestSection()
            }
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.experimental_settings_title)) },
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

@Composable
fun SyncProgressItem(text: String, isSyncing: Boolean) {
    if (isSyncing) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
            Text(text)
        }
    }
}

@Composable
fun MaterialColorsTestSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Material Colors Test",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
            ColorRow("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            ColorRow("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
            ColorRow("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
            ColorRow("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
            ColorRow("Inverse Surface", MaterialTheme.colorScheme.inverseSurface, MaterialTheme.colorScheme.onSurfaceVariant)
            ColorRow("Surface Variant", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
            ColorRow("Surface Bright", MaterialTheme.colorScheme.surfaceBright, MaterialTheme.colorScheme.onSurface)
            ColorRow("Surface Tint", MaterialTheme.colorScheme.surfaceTint, MaterialTheme.colorScheme.onSurface)
            ColorRow("Surface Dim", MaterialTheme.colorScheme.surfaceDim, MaterialTheme.colorScheme.onSurface)
            ColorRow("Surface Container Highest", MaterialTheme.colorScheme.surfaceContainerHighest, MaterialTheme.colorScheme.onSurface)
            ColorRow("Surface Container High", MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurface)
            ColorRow("Surface Container Low", MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.onSurface)
            ColorRow("Error Container", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
fun ColorRow(label: String, backgroundColor: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(backgroundColor)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun HapticsTestSection() {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Haptics Test",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            hapticFeedbackTypes.forEach { hapticType ->
                HapticFeedbackItem(
                    name = hapticType.name,
                    onClick = {
                        haptic.performHapticFeedback(hapticType.type)
                    }
                )
            }
        }
    }
}

@Composable
fun HapticFeedbackItem(
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Vibration,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

data class HapticFeedbackTypeItem(
    val name: String,
    val type: HapticFeedbackType
)

val hapticFeedbackTypes = listOf(
    HapticFeedbackTypeItem("LongPress", HapticFeedbackType.LongPress),
    HapticFeedbackTypeItem("TextHandle", HapticFeedbackType.TextHandleMove),
    HapticFeedbackTypeItem("VirtualKey", HapticFeedbackType.VirtualKey),
    HapticFeedbackTypeItem("GestureEnd", HapticFeedbackType.GestureEnd),
    HapticFeedbackTypeItem("Threshold", HapticFeedbackType.GestureThresholdActivate),
    HapticFeedbackTypeItem("Tick", HapticFeedbackType.SegmentTick),
    HapticFeedbackTypeItem("FrequentTick", HapticFeedbackType.SegmentFrequentTick),
    HapticFeedbackTypeItem("ContextClick", HapticFeedbackType.ContextClick),
    HapticFeedbackTypeItem("Confirm", HapticFeedbackType.Confirm),
    HapticFeedbackTypeItem("Reject", HapticFeedbackType.Reject),
    HapticFeedbackTypeItem("ToggleOn", HapticFeedbackType.ToggleOn),
    HapticFeedbackTypeItem("ToggleOff", HapticFeedbackType.ToggleOff)
)