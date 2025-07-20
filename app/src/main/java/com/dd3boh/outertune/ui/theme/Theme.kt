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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

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
        val isGrayscale = themeColor.isGrayscale()
        val paletteStyle = if (isGrayscale) PaletteStyle.Neutral else PaletteStyle.Vibrant

        rememberDynamicColorScheme(
            seedColor = themeColor,
            isDark = darkTheme,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
            style = paletteStyle
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
        .maximumColorCount(24)
        .generate()

    val swatch = palette.vibrantSwatch
        ?: palette.mutedSwatch
        ?: palette.dominantSwatch
        ?: return DefaultThemeColor

    val hsl = swatch.hsl
    val saturation = hsl[1]
    val lightness = hsl[2]

    val isGrayscale = when {
        lightness < 0.2f -> saturation < 0.03f
        lightness > 0.8f -> saturation < 0.03f
        else -> saturation < 0.08f
    }

    if (isGrayscale) {
        val adjustedLightness = lightness.coerceIn(0.3f, 0.7f)
        val newHsl = floatArrayOf(0f, 0f, adjustedLightness)
        return Color(ColorUtils.HSLToColor(newHsl))
    }
    return Color(swatch.rgb)
}

fun Color.isGrayscale(saturationThreshold: Float = 0.08f, lightnessRange: Float = 0.15f): Boolean {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)

    val saturation = hsl[1]
    val lightness = hsl[2]

    if (saturation > saturationThreshold) return false

    return when {
        lightness < 0.2f -> saturation < 0.03f
        lightness > 0.8f -> saturation < 0.03f
        else -> saturation < saturationThreshold
    }
}

data class ThemeColorInfo(
    val color: Color,
    val isGrayscale: Boolean,
    val suggestedPaletteStyle: PaletteStyle
)

fun Bitmap.extractGradientColors(): List<Color> {
    val palette = Palette.from(this).maximumColorCount(32).generate()

    val dominantSwatch = palette.dominantSwatch
    val hsl = dominantSwatch?.hsl
    val isGrayscale = hsl != null && hsl[1] < 0.1f

    if (isGrayscale) {
        val baseLightness = dominantSwatch?.hsl?.get(2) ?: 0.5f
        val topColor = Color(ColorUtils.HSLToColor(floatArrayOf(0f, 0f, (baseLightness + 0.2f).coerceAtMost(0.9f))))
        val middleColor = Color(ColorUtils.HSLToColor(floatArrayOf(0f, 0f, baseLightness.coerceIn(0.2f, 0.8f))))
        val bottomColor = Color(ColorUtils.HSLToColor(floatArrayOf(0f, 0f, (baseLightness - 0.2f).coerceAtLeast(0.1f))))
        return listOf(topColor, middleColor, bottomColor)
    }

    val vibrant = palette.vibrantSwatch?.rgb?.let { Color(it) }
    val darkVibrant = palette.darkVibrantSwatch?.rgb?.let { Color(it) }
    val lightVibrant = palette.lightVibrantSwatch?.rgb?.let { Color(it) }
    val dominant = palette.dominantSwatch?.rgb?.let { Color(it) }

    val colors = mutableListOf<Color>()

    val topColor = vibrant ?: lightVibrant ?: dominant ?: Color(0xFF8B7ED8)
    colors.add(topColor)

    val bottomColor = darkVibrant ?: dominant?.darken(0.4f) ?: topColor.darken(0.5f)
    colors.add(bottomColor)

    val middleColor = palette.mutedSwatch?.rgb?.let { Color(it) } ?: topColor.darken(0.2f)
    colors.add(1, middleColor)

    while(colors.size < 3) {
        colors.add(colors.last().darken(0.2f))
    }

    return colors.distinct().take(3)
}

fun Color.darken(factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[2] *= (1f - factor)
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