package com.dd3boh.outertune.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets

/**
 * Safely applies window insets padding to prevent crashes during node detachment.
 * This is particularly useful in Crossfade animations where nodes might be detached
 * while window insets are still being processed.
 */
@Composable
fun Modifier.safeWindowInsetsPadding(
    windowInsets: WindowInsets? = null
): Modifier {
    val safeWindowInsets by remember {
        derivedStateOf {
            try {
                windowInsets ?: LocalPlayerAwareWindowInsets.current
            } catch (e: Exception) {
                null
            }
        }
    }
    
    return this.then(
        safeWindowInsets?.let { insets ->
            Modifier.windowInsetsPadding(insets)
        } ?: Modifier
    )
}

/**
 * Safely gets window insets to prevent crashes during node detachment.
 * This is useful for contentPadding and other scenarios where you need the insets values.
 */
@Composable
fun safeWindowInsets(
    windowInsets: WindowInsets? = null
): WindowInsets {
    return remember {
        derivedStateOf {
            try {
                windowInsets ?: LocalPlayerAwareWindowInsets.current
            } catch (e: Exception) {
                WindowInsets(0.dp)
            }
        }
    }.value
}