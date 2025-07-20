/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */
package com.dd3boh.outertune.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.score.Score

val DefaultThemeColor = Color(0xFFED5564)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OuterTuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val useSystemDynamicColor = (themeColor == DefaultThemeColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    val baseColorScheme = if (useSystemDynamicColor) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberDynamicColorScheme(
            seedColor = themeColor,
            isDark = darkTheme,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            style = PaletteStyle.TonalSpot
        )
    }

    val colorScheme = remember(baseColorScheme, pureBlack, darkTheme) {
        if (darkTheme && pureBlack) {
            baseColorScheme.pureBlack(true)
        } else {
            baseColorScheme
        }
    }

    // Use the defined M3 Expressive Typography
    // TODO: Define M3 Expressive Shapes instance if needed
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Use the defined AppTypography
        // shapes = MaterialTheme.shapes, // Placeholder - Needs update (Shapes not used in original)
        content = content
    )
}

fun Bitmap.extractThemeColor(): Color {
    val palette = Palette.from(this)
        .maximumColorCount(32)
        .generate()

    val colorsToPopulation = palette.swatches.associate { it.rgb to it.population }

    if (colorsToPopulation.isEmpty()) {
        return DefaultThemeColor
    }

    val rankedColors = Score.score(colorsToPopulation)
    return Color(rankedColors.firstOrNull() ?: DefaultThemeColor.toArgb())
}

fun Bitmap.extractGradientColors(): List<Color> {
    val palette = Palette.from(this).maximumColorCount(32).generate()

    val vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) }
    val darkVibrant = palette.darkVibrantSwatch?.rgb?.let { Color(it) }
    val lightVibrant = palette.lightVibrantSwatch?.rgb?.let { Color(it) }
    val muted = palette.mutedSwatch?.rgb?.let { Color(it) }
    val darkMuted = palette.darkMutedSwatch?.rgb?.let { Color(it) }
    val lightMuted = palette.lightMutedSwatch?.rgb?.let { Color(it) }
    val dominant = palette.dominantSwatch?.rgb?.let { Color(it) }

    val candidates = listOfNotNull(vibrant, darkVibrant, lightVibrant, dominant, muted, lightMuted, darkMuted)
        .distinctBy { it.toArgb() }
        .filter { it.luminance() > 0.05f }

    return if (candidates.size >= 2) {
        val sortedByLuminance = candidates.sortedByDescending { it.luminance() }
        val lightColor = sortedByLuminance.first()
        val darkColor = sortedByLuminance.last()

        if (lightColor.luminance() - darkColor.luminance() > 0.15f) {
            listOf(lightColor, darkColor, darkColor.darken(0.2f))
        } else {
            val enhancedLight = lightColor.lighten(0.2f)
            val enhancedDark = darkColor.darken(0.4f)
            listOf(enhancedLight, lightColor, enhancedDark)
        }
    } else if (candidates.isNotEmpty()) {
        val primary = candidates[0]
        val secondary = if (primary.luminance() > 0.5f) primary.darken(0.4f) else primary.lighten(0.3f)
        val tertiary = if (primary.luminance() > 0.5f) primary.darken(0.6f) else primary.darken(0.3f)
        listOf(primary, secondary, tertiary)
    } else {
        listOf(Color(0xFF8B7ED8), Color(0xFF6B5B95), Color(0xFF2E2440))
    }
}

fun Color.darken(factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[2] *= (1f - factor) // Decrease brightness
    return Color(android.graphics.Color.HSVToColor(hsv))
}

fun Color.lighten(factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[2] = (hsv[2] + factor).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}