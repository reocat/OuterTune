/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 */

package com.dd3boh.outertune.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.MonetAccentColorKey
import com.dd3boh.outertune.constants.MonetChromaFactorKey
import com.dd3boh.outertune.constants.MonetCustomColorEnabledKey
import com.dd3boh.outertune.constants.MonetGrayscaleKey
import com.dd3boh.outertune.constants.MonetLuminanceFactorKey
import com.dd3boh.outertune.constants.MonetStyle
import com.dd3boh.outertune.constants.MonetThemeStyleKey
import com.dd3boh.outertune.constants.MonetTintBackgroundKey
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.ui.component.ColumnWithContentPadding
import com.dd3boh.outertune.ui.component.EditTextPreference
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.theme.DefaultThemeColor
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import com.materialkolor.PaletteStyle

fun MonetStyle.toMaterialKolorPaletteStyle(): PaletteStyle {
    return when (this) {
        MonetStyle.TONAL_SPOT -> PaletteStyle.TonalSpot
        MonetStyle.VIBRANT -> PaletteStyle.Vibrant
        MonetStyle.EXPRESSIVE -> PaletteStyle.Expressive
        MonetStyle.RAINBOW -> PaletteStyle.Rainbow
        MonetStyle.FRUIT_SALAD -> PaletteStyle.FruitSalad
        MonetStyle.MONOCHROMATIC -> PaletteStyle.Monochrome
    }
}

@Composable
private fun PreferenceSlider(
    title: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit,
    range: ClosedFloatingPointRange<Float>,
    valueSuffix: String = "",
    valueMultiplier: Float = 1f,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isMiddle: Boolean = false
) {
    val cardShape = when {
        isFirst -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
        isLast -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
        isMiddle -> RoundedCornerShape(2.dp)
        else -> RoundedCornerShape(12.dp)
    }

    Column {
        Card(
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${(value * valueMultiplier).toInt()}$valueSuffix",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = onReset,
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = "Reset $title",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = range
                )
            }
        }
        if (!isLast && (isFirst || isMiddle)) {
            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonetSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (themeStyle, onThemeStyleChange) = rememberEnumPreference(
        MonetThemeStyleKey,
        defaultValue = MonetStyle.TONAL_SPOT
    )
    val (luminance, onLuminanceChange) = rememberPreference(MonetLuminanceFactorKey, defaultValue = 0f)
    val (chroma, onChromaChange) = rememberPreference(MonetChromaFactorKey, defaultValue = 0f)
    val (useVibrantForGrayscale, onUseVibrantForGrayscaleChange) = rememberPreference(
        MonetGrayscaleKey,
        defaultValue = false
    )
    val (customColorEnabled, onCustomColorEnabledChange) = rememberPreference(
        MonetCustomColorEnabledKey,
        defaultValue = false
    )
    val defaultColorHex = String.format("#%06X", (0xFFFFFF and DefaultThemeColor.toArgb()))
    val (accentColor, onAccentColorChange) = rememberPreference(MonetAccentColorKey, defaultValue = defaultColorHex)
    val (tintBackground, onTintBackgroundChange) = rememberPreference(MonetTintBackgroundKey, defaultValue = false)

    var tempAccentColor by remember(accentColor) { mutableStateOf(accentColor) }
    val isHexValid by remember(tempAccentColor) {
        derivedStateOf {
            tempAccentColor.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))
        }
    }

    LaunchedEffect(customColorEnabled) {
        if (!customColorEnabled) {
            onAccentColorChange(defaultColorHex)
        }
    }

    ColumnWithContentPadding(
        modifier = Modifier.fillMaxHeight(),
        columnModifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        PreferenceGroupTitle(title = stringResource(R.string.color_source))

        EnumListPreference(
            title = { Text(stringResource(R.string.theme_style)) },
            selectedValue = themeStyle,
            onValueSelected = onThemeStyleChange,
            valueText = {
                when (it) {
                    MonetStyle.TONAL_SPOT -> "Tonal Spot"
                    MonetStyle.VIBRANT -> "Vibrant"
                    MonetStyle.EXPRESSIVE -> "Expressive"
                    MonetStyle.RAINBOW -> "Rainbow"
                    MonetStyle.FRUIT_SALAD -> "Fruit Salad"
                    MonetStyle.MONOCHROMATIC -> "Monochromatic"
                }
            },
            isFirst = true
        )

        SwitchPreference(
            title = { Text("Custom accent color") },
            checked = customColorEnabled,
            onCheckedChange = onCustomColorEnabledChange,
            isMiddle = customColorEnabled,
            isLast = !customColorEnabled,
        )

        if (customColorEnabled) {
            EditTextPreference(
                title = { Text("Hex color") },
                value = accentColor,
                isInputValid = { it.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$")) },
                onValueChange = {
                    onAccentColorChange(it)
                },
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroupTitle(title = "Color tuning")

        PreferenceSlider(
            title = "Luminance",
            description = "Higher values produce brighter colors",
            value = luminance,
            onValueChange = onLuminanceChange,
            onReset = { onLuminanceChange(0f) },
            range = -0.5f..0.5f,
            valueSuffix = "%",
            valueMultiplier = 100f,
            isFirst = true
        )
        PreferenceSlider(
            title = "Chroma factor",
            description = "Higher values produce stronger colors",
            value = chroma,
            onValueChange = onChromaChange,
            onReset = { onChromaChange(0f) },
            range = -0.5f..0.5f,
            valueSuffix = "%",
            valueMultiplier = 100f,
            isLast = true
        )


        Spacer(modifier = Modifier.height(12.dp))
        PreferenceGroupTitle(title = "Behavior")

        SwitchPreference(
            title = { Text("Tint background") },
            description = "Tints background surfaces with the accent color",
            checked = tintBackground,
            onCheckedChange = onTintBackgroundChange,
            isFirst = true,
        )

        SwitchPreference(
            title = { Text("Use vibrant colors for grayscale thumbnails") },
            checked = useVibrantForGrayscale,
            onCheckedChange = onUseVibrantForGrayscaleChange,
            isLast = true
        )
    }

    TopAppBar(
        title = { Text("Monet settings") },
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
        windowInsets = TopBarInsets,
        scrollBehavior = scrollBehavior
    )
}