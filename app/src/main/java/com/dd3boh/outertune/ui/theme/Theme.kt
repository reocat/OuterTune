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
import androidx.compose.material3.MaterialTheme
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
import androidx.palette.graphics.Target
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import kotlin.math.abs

val DefaultThemeColor = Color(0xFFED5564)

@Composable
fun OuterTuneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = remember(darkTheme, pureBlack, themeColor) {
        if (themeColor == DefaultThemeColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context).pureBlack(pureBlack)
            else dynamicLightColorScheme(context)
        } else {
            // Using MaterialKolor with parameters configured to closely match SchemeTonalSpot
            dynamicColorScheme(
                primary = themeColor,
                isDark = darkTheme,
                isAmoled = darkTheme && pureBlack,
                secondary = null,  // Let it be derived from primary as in original
                tertiary = null,   // Let it be derived from primary as in original
                neutral = null,    // Let it be derived from primary as in original
                neutralVariant = null, // Let it be derived from primary as in original
                error = null,      // Use default error color
                style = PaletteStyle.TonalSpot, // Same as original SchemeTonalSpot
                contrastLevel = Contrast.Default.value, // Standard contrast
                isExtendedFidelity = true, // Enable for closer match to Material You
                // Apply pureBlack modification directly if needed
                modifyColorScheme = if (darkTheme && pureBlack) {
                    { scheme ->
                        scheme.copy(
                            surface = Color.Black,
                            background = Color.Black
                        )
                    }
                } else null
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

fun Bitmap.extractThemeColor(): Color {
    // Attempt to replicate Score.score algorithm from Google's material-color-utilities
    val palette = Palette.from(this)
        .maximumColorCount(8)
        .generate()

    // First try to get vibrant colors that might be visually appealing
    var selectedColor = palette.vibrantSwatch?.rgb
        ?: palette.lightVibrantSwatch?.rgb
        ?: palette.darkVibrantSwatch?.rgb

    // If no vibrant color found, fall back to population-based scoring like the original
    if (selectedColor == null) {
        val colorsToPopulation = palette.swatches.associate { it.rgb to it.population }
        selectedColor = colorsToPopulation.entries
            .sortedByDescending { it.value }
            .firstOrNull()?.key
    }

    return if (selectedColor != null) Color(selectedColor) else DefaultThemeColor
}

fun Bitmap.extractGradientColors(): List<Color> {
    // Create a palette with more colors and use specific targets to get varied colors
    val palette = Palette.Builder(this)
        .maximumColorCount(24)  // Increased from 16 for more variety
        .addTarget(Target.VIBRANT)
        .addTarget(Target.LIGHT_VIBRANT)
        .addTarget(Target.DARK_VIBRANT)
        .addTarget(Target.MUTED)
        .addTarget(Target.LIGHT_MUTED)
        .addTarget(Target.DARK_MUTED)
        .generate()

    // Extract all swatches
    val swatches = palette.swatches

    // If we don't have enough colors, try to get more from the bitmap
    if (swatches.size < 2) {
        return fallbackGradientColors()
    }

    // Try to find two colors that are visually distinct for a better gradient
    val distinctColors = findDistinctColors(swatches.map { Color(it.rgb) })

    return distinctColors ?: fallbackGradientColors()
}

// Helper function to find visually distinct colors for gradient
private fun findDistinctColors(colors: List<Color>): List<Color>? {
    if (colors.size < 2) return null

    // Sort by luminance first to get a range from light to dark
    val sortedByLuminance = colors.sortedByDescending { it.luminance() }

    // For a good gradient, we want colors that are different enough
    // Try to find a pair with good color and luminance difference
    for (i in 0 until sortedByLuminance.size - 1) {
        for (j in i + 1 until sortedByLuminance.size) {
            val color1 = sortedByLuminance[i]
            val color2 = sortedByLuminance[j]

            // Check if colors are distinct enough
            if (areColorsDistinct(color1, color2)) {
                return listOf(color1, color2)
            }
        }
    }

    // If no distinct enough pair found, just return the brightest and darkest
    return listOf(sortedByLuminance.first(), sortedByLuminance.last())
}

// Helper function to determine if two colors are visually distinct
private fun areColorsDistinct(color1: Color, color2: Color): Boolean {
    // Convert to HSL for better comparison
    val hsv1 = FloatArray(3)
    val hsv2 = FloatArray(3)

    android.graphics.Color.colorToHSV(color1.toArgb(), hsv1)
    android.graphics.Color.colorToHSV(color2.toArgb(), hsv2)

    // Check hue difference (circular distance)
    val hueDiff = Math.min(abs(hsv1[0] - hsv2[0]), 360 - abs(hsv1[0] - hsv2[0]))

    // Check saturation and value difference
    val satDiff = abs(hsv1[1] - hsv2[1])
    val valDiff = abs(hsv1[2] - hsv2[2])

    // Luminance difference
    val lumDiff = abs(color1.luminance() - color2.luminance())

    // Colors are distinct if they differ significantly in hue OR
    // they differ in both saturation and luminance
    return (hueDiff > 30) ||
            (satDiff > 0.4 && lumDiff > 0.2) ||
            (lumDiff > 0.5)
}

// Fallback gradient colors
private fun fallbackGradientColors(): List<Color> {
    return listOf(Color(0xFF595959), Color(0xFF0D0D0D))
}

// This function is redundant since we're using modifyColorScheme in dynamicColorScheme,
// but keeping it for compatibility with existing code
fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}