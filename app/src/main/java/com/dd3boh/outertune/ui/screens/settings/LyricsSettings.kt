/*
 * Copyright (C) 2025 O‌ute‌rTu‌ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens.settings


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.SettingsTopBarHeight
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.screens.settings.fragments.LyricFormatFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.LyricParserFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.LyricSourceFrag
import com.dd3boh.outertune.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(SettingsTopBarHeight))
        PreferenceGroupTitle(
            title = stringResource(R.string.grp_lyrics_source)
        )
        LyricSourceFrag()

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_lyrics_parser)
        )
        LyricParserFrag()

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_lyrics_format)
        )
        LyricFormatFrag()
    }


    TopAppBar(
        title = { Text(stringResource(R.string.lyrics_settings_title)) },
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

enum class LyricsPosition {
    LEFT, CENTER, RIGHT
}

@Composable
fun FontSizeDialog(
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    onReset: () -> Unit
) {
    var currentValue by remember { mutableFloatStateOf(initialValue.toFloat()) }
    val defaultValue = 20f

    val previewOpacity by animateFloatAsState(
        targetValue = if (currentValue == initialValue.toFloat()) 0.6f else 1f,
        label = "preview opacity"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.8f),
        title = {
            Text(
                text = stringResource(R.string.lyrics_font_Size),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Preview Text",
                    fontSize = currentValue.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(previewOpacity)
                )

                Slider(
                    value = currentValue,
                    onValueChange = { currentValue = it },
                    valueRange = 8f..32f,
                    steps = 23,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = { if (currentValue > 8f) currentValue-- },
                        enabled = currentValue > 8f
                    ) {
                        Text("-", style = MaterialTheme.typography.titleLarge)
                    }

                    Text(
                        text = "${currentValue.roundToInt()} sp",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    FilledTonalIconButton(
                        onClick = { if (currentValue < 32f) currentValue++ },
                        enabled = currentValue < 32f
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                TextButton(
                    onClick = {
                        currentValue = defaultValue
                        onReset()
                    },
                    enabled = currentValue.roundToInt() != defaultValue.toInt()
                ) {
                    Text(stringResource(R.string.reset))
                }

                Spacer(modifier = Modifier.weight(1f))

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