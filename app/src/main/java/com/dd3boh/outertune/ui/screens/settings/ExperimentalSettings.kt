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
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.DevSettingsKey
import com.dd3boh.outertune.constants.FirstSetupPassed
import com.dd3boh.outertune.constants.ScannerImpl
import com.dd3boh.outertune.constants.ScannerImplKey
import com.dd3boh.outertune.constants.SettingsTopBarHeight
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
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // state variables and such
    val (devSettings, onDevSettingsChange) = rememberPreference(DevSettingsKey, defaultValue = false)
    val (firstSetupPassed, onFirstSetupPassedChange) = rememberPreference(FirstSetupPassed, defaultValue = false)



    val (scannerImpl) = rememberEnumPreference(
        key = ScannerImplKey,
        defaultValue = ScannerImpl.TAGLIB
    )

    var nukeEnabled by remember { mutableStateOf(false) }
    var hapticsTestEnabled by remember { mutableStateOf(false) }
    var colorsTestEnabled by remember { mutableStateOf(false) }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(SettingsTopBarHeight))
        // dev settings
        SwitchPreference(
            title = { Text(stringResource(R.string.dev_settings_title)) },
            description = stringResource(R.string.dev_settings_description),
            icon = { Icon(Icons.Rounded.DeveloperMode, null) },
            checked = devSettings,
            onCheckedChange = onDevSettingsChange
        )



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