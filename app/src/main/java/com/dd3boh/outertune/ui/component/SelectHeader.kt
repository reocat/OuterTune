/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Deselect
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.R
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.ui.menu.SelectionMediaMetadataMenu
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ElevatedCard

@Composable
fun RowScope.SelectHeader(
    navController: NavController,
    selectedItems: List<MediaMetadata>,
    totalItemCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    menuState: MenuState,
    onDismiss: () -> Unit = {},
    onRemoveFromHistory: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val allSelected = selectedItems.size < totalItemCount
    AnimatedVisibility(
        visible = selectedItems.isNotEmpty(),
        enter = fadeIn(tween(220)) + scaleIn(tween(220)),
        exit = fadeOut(tween(180)) + scaleOut(tween(180))
    ) {
        Surface(
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(20.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp, bottom = 3.dp, start = 8.dp, end = 8.dp)
            ) {
                Text(
                    text = "${selectedItems.size}/${context.resources.getQuantityString(R.plurals.n_selected, totalItemCount, totalItemCount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f, false)
                )
                // option menu
                IconButton(
                    onClick = {
                        menuState.show {
                            SelectionMediaMetadataMenu(
                                navController = navController,
                                selection = selectedItems,
                                onDismiss = menuState::dismiss,
                                clearAction = onDeselectAll,
                                onRemoveFromHistory = onRemoveFromHistory
                            )
                        }
                    },
                    modifier = Modifier
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(tween(180)),
                        exit = scaleOut(tween(120))
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = null,
                            tint = LocalContentColor.current
                        )
                    }
                }
                // select/deselect all
                IconButton(
                    onClick = if (allSelected) onSelectAll else onDeselectAll,
                    modifier = Modifier
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(tween(180)),
                        exit = scaleOut(tween(120))
                    ) {
                        Icon(
                            imageVector = if (allSelected) Icons.Outlined.SelectAll else Icons.Outlined.Deselect,
                            contentDescription = null,
                            tint = LocalContentColor.current
                        )
                    }
                }
                // close selection mode
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(tween(180)),
                        exit = scaleOut(tween(120))
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}