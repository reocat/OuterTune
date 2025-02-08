/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.NoCell
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.AudioNormalizationKey
import com.dd3boh.outertune.constants.AudioOffload
import com.dd3boh.outertune.constants.AudioQuality
import com.dd3boh.outertune.constants.AudioQualityKey
import com.dd3boh.outertune.constants.AutoLoadMoreKey
import com.dd3boh.outertune.constants.KeepAliveKey
import com.dd3boh.outertune.constants.PersistentQueueKey
import com.dd3boh.outertune.constants.PlayerOnError
import com.dd3boh.outertune.constants.PlayerOnErrorActionKey
import com.dd3boh.outertune.constants.SkipOnErrorKey
import com.dd3boh.outertune.constants.SkipSilenceKey
import com.dd3boh.outertune.constants.StopMusicOnTaskClearKey
import com.dd3boh.outertune.constants.minPlaybackDurKey
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(
        key = AudioQualityKey,
        defaultValue = AudioQuality.AUTO
    )
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(key = PersistentQueueKey, defaultValue = true)
    val (skipSilence, onSkipSilenceChange) = rememberPreference(key = SkipSilenceKey, defaultValue = false)
    val (skipOnErrorKey, onSkipOnErrorChange) = rememberPreference(key = SkipOnErrorKey, defaultValue = true)
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(
        key = AudioNormalizationKey,
        defaultValue = true
    )
    val (autoLoadMore, onAutoLoadMoreChange) = rememberPreference(AutoLoadMoreKey, defaultValue = true)
    val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(
        key = StopMusicOnTaskClearKey,
        defaultValue = false
    )
    val (minPlaybackDur, onMinPlaybackDurChange) = rememberPreference(minPlaybackDurKey, defaultValue = 30)
    val (audioOffload, onAudioOffloadChange) = rememberPreference(key = AudioOffload, defaultValue = false)
    val (keepAlive, onKeepAliveChange) = rememberPreference(key = KeepAliveKey, defaultValue = false)

    var showMinPlaybackDur by remember {
        mutableStateOf(false)
    }
    var tempminPlaybackDur by remember {
        mutableIntStateOf(minPlaybackDur)
    }

    if (showMinPlaybackDur) {
        MinPlaybackDurDialog(
            initialValue = minPlaybackDur,
            onDismiss = { showMinPlaybackDur = false },
            onConfirm = {
                showMinPlaybackDur = false
                onMinPlaybackDurChange(it)
            }
        )
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top)))

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_general)
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.persistent_queue)) },
            description = stringResource(R.string.persistent_queue_desc),
            icon = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, null) },
            checked = persistentQueue,
            onCheckedChange = onPersistentQueueChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.auto_load_more)) },
            description = stringResource(R.string.auto_load_more_desc),
            icon = { Icon(Icons.Rounded.Autorenew, null) },
            checked = autoLoadMore,
            onCheckedChange = onAutoLoadMoreChange
        )
        // lyrics settings
        PreferenceEntry(
            title = { Text(stringResource(R.string.lyrics_settings_title)) },
            icon = { Icon(Icons.Rounded.Lyrics, null) },
            onClick = { navController.navigate("settings/player/lyrics") }
        )
        PreferenceEntry(
            title = { Text(stringResource(R.string.min_playback_duration)) },
            icon = { Icon(Icons.Rounded.Sync, null) },
            onClick = { showMinPlaybackDur = true }
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_audio)
        )
        EnumListPreference(
            title = { Text(stringResource(R.string.audio_quality)) },
            icon = { Icon(Icons.Rounded.GraphicEq, null) },
            selectedValue = audioQuality,
            onValueSelected = onAudioQualityChange,
            valueText = {
                when (it) {
                    AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                    AudioQuality.MAX -> stringResource(R.string.audio_quality_max)
                    AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                    AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                }
            }
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.audio_normalization)) },
            icon = { Icon(Icons.AutoMirrored.Rounded.VolumeUp, null) },
            checked = audioNormalization,
            onCheckedChange = onAudioNormalizationChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.skip_silence)) },
            icon = { Icon(painterResource(R.drawable.skip_next), null) },
            checked = skipSilence,
            onCheckedChange = onSkipSilenceChange
        )
        EnumListPreference(
            title = { Text(stringResource(R.string.on_error)) },
            icon = { Icon(Icons.Rounded.Error, null) },
            selectedValue = playerOnErrorAction,
            onValueSelected = onPlayerOnErrorAction,
            valueText = {
                when (it) {
                    PlayerOnError.PAUSE -> stringResource(R.string.pause)
                    PlayerOnError.SKIP -> stringResource(R.string.play_next)
                }
            }
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.misc)
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.stop_music_on_task_clear)) },
            icon = { Icon(Icons.Rounded.ClearAll, null) },
            checked = stopMusicOnTaskClear,
            onCheckedChange = onStopMusicOnTaskClearChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.audio_offload)) },
            description = stringResource(R.string.audio_offload_description),
            icon = { Icon(Icons.Rounded.Bolt, null) },
            checked = audioOffload,
            onCheckedChange = onAudioOffloadChange
        )
        SwitchPreference(
            title = { Text(stringResource(R.string.keep_alive_title)) },
            description = stringResource(R.string.keep_alive_description),
            icon = { Icon(Icons.Rounded.NoCell, null) },
            checked = keepAlive,
            onCheckedChange = onKeepAliveChange
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.player_and_audio)) },
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
fun MinPlaybackDurDialog(
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var currentValue by remember { mutableFloatStateOf(initialValue.toFloat()) }
    val defaultValue = 30  // From original PlayerSettings default

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.8f),
        title = {
            Text(
                text = stringResource(R.string.min_playback_duration),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.min_playback_duration_description),
                    style = MaterialTheme.typography.bodyMedium
                )

                Slider(
                    value = currentValue,
                    onValueChange = { currentValue = it },
                    valueRange = 0f..100f,
                    steps = 99,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { currentValue = (currentValue - 1).coerceAtLeast(0f) },
                        enabled = currentValue > 0f
                    ) { Text("-") }

                    Text(
                        text = "${currentValue.roundToInt()}%",
                        style = MaterialTheme.typography.titleLarge
                    )

                    FilledTonalIconButton(
                        onClick = { currentValue = (currentValue + 1).coerceAtMost(100f) },
                        enabled = currentValue < 100f
                    ) { Text("+") }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = { currentValue = defaultValue.toFloat() },
                    enabled = currentValue.roundToInt() != defaultValue
                ) {
                    Text(stringResource(R.string.reset))
                }

                Spacer(Modifier.weight(1f))

                TextButton(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }

                Button(
                    onClick = { onConfirm(currentValue.roundToInt()) },
                    enabled = currentValue.roundToInt() != initialValue
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        }
    )
}