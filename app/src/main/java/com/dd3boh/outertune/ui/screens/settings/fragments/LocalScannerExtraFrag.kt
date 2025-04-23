/*
 * Copyright (C) 2025 O‌ute‌rTu‌ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */
package com.dd3boh.outertune.ui.screens.settings.fragments

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.ScannerImpl
import com.dd3boh.outertune.constants.ScannerImplKey
import com.dd3boh.outertune.constants.ScannerMatchCriteria
import com.dd3boh.outertune.constants.ScannerSensitivityKey
import com.dd3boh.outertune.constants.ScannerStrictExtKey
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.LocalScannerExtraFrag() {
    val context = LocalContext.current

    val (scannerSensitivity, onScannerSensitivityChange) = rememberEnumPreference(
        key = ScannerSensitivityKey,
        defaultValue = ScannerMatchCriteria.LEVEL_2
    )
    val (scannerImpl, onScannerImplChange) = rememberEnumPreference(
        key = ScannerImplKey,
        defaultValue = ScannerImpl.TAGLIB
    )
    val (strictExtensions, onStrictExtensionsChange) = rememberPreference(ScannerStrictExtKey, defaultValue = false)


    // scanner sensitivity
    EnumListPreference(
        title = { Text(stringResource(R.string.scanner_sensitivity_title)) },
        icon = { Icon(Icons.Rounded.GraphicEq, null) },
        selectedValue = scannerSensitivity,
        onValueSelected = onScannerSensitivityChange,
        valueText = {
            when (it) {
                ScannerMatchCriteria.LEVEL_1 -> stringResource(R.string.scanner_sensitivity_L1)
                ScannerMatchCriteria.LEVEL_2 -> stringResource(R.string.scanner_sensitivity_L2)
                ScannerMatchCriteria.LEVEL_3 -> stringResource(R.string.scanner_sensitivity_L3)
            }
        }
    )
    // strict file ext
    SwitchPreference(
        title = { Text(stringResource(R.string.scanner_strict_file_name_title)) },
        description = stringResource(R.string.scanner_strict_file_name_description),
        icon = { Icon(Icons.Rounded.TextFields, null) },
        checked = strictExtensions,
        onCheckedChange = onStrictExtensionsChange
    )
    // scanner type
//    EnumListPreference(
//        title = { Text(stringResource(R.string.scanner_type_title)) },
//        icon = { Icon(Icons.Rounded.Speed, null) },
//        selectedValue = scannerImpl,
//        onValueSelected = {
//            if (it == ScannerImpl.FFMPEG_EXT && isFFmpegInstalled) {
//                onScannerImplChange(it)
//            } else {
//                Toast.makeText(context, context.getString(R.string.scanner_missing_ffmpeg), Toast.LENGTH_LONG)
//                    .show()
//                // Explicitly revert to TagLib if FFmpeg is not available
//                onScannerImplChange(ScannerImpl.TAGLIB)
//            }
//        },
//        valueText = {
//            when (it) {
//                ScannerImpl.TAGLIB -> stringResource(R.string.scanner_type_taglib)
//                ScannerImpl.FFMPEG_EXT -> stringResource(R.string.scanner_type_ffmpeg_ext)
//            }
//        },
//        values = ScannerImpl.entries,
//        disabled = { it == ScannerImpl.FFMPEG_EXT && !isFFmpegInstalled }
//    )
}