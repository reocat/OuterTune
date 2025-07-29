/*
 * Material 3-styled icon button abstraction for the player screen.
 * Chooses appropriate button type based on PlayerButtonsStyle preference so we
 * can easily switch between default (minimal) and secondary (accent/filled)
 * designs while keeping the call site concise.
 */
package com.dd3boh.outertune.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.dd3boh.outertune.constants.PlayerButtonsStyle

@Composable
fun PlayerIconButton(
    style: PlayerButtonsStyle,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    when (style) {
        PlayerButtonsStyle.SECONDARY -> FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = color
            )
        ) {
            Icon(imageVector = icon, contentDescription = null)
        }
        PlayerButtonsStyle.DEFAULT -> IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = IconButtonDefaults.iconButtonColors(contentColor = color)
        ) {
            Icon(imageVector = icon, contentDescription = null)
        }
    }
}

@Composable
fun PlayerIconButton(
    style: PlayerButtonsStyle,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    when (style) {
        PlayerButtonsStyle.SECONDARY -> FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = color
            )
        ) {
            Image(painterResource(icon), contentDescription = null)
        }
        PlayerButtonsStyle.DEFAULT -> IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            colors = IconButtonDefaults.iconButtonColors(contentColor = color)
        ) {
            Image(painterResource(icon), contentDescription = null)
        }
    }
}
