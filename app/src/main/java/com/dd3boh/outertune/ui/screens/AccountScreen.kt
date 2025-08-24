/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalMenuState
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.AccountNameKey
import com.dd3boh.outertune.constants.GridThumbnailHeight
import com.dd3boh.outertune.constants.InnerTubeCookieKey
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.ui.component.ChipsRow
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.button.IconButton
import com.dd3boh.outertune.ui.component.items.YouTubeGridItem
import com.dd3boh.outertune.ui.component.shimmer.GridItemPlaceHolder
import com.dd3boh.outertune.ui.component.shimmer.ShimmerHost
import com.dd3boh.outertune.ui.menu.YouTubeAlbumMenu
import com.dd3boh.outertune.ui.menu.YouTubeArtistMenu
import com.dd3boh.outertune.ui.menu.YouTubePlaylistMenu
import com.dd3boh.outertune.ui.screens.settings.fragments.AccountFrag
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.rememberPreference
import com.dd3boh.outertune.viewmodels.AccountContentType
import com.dd3boh.outertune.viewmodels.AccountViewModel
import com.zionhuang.innertube.utils.parseCookieString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current

    val coroutineScope = rememberCoroutineScope()

    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val accountName by rememberPreference(AccountNameKey, stringResource(R.string.not_logged_in))

    val playlists by viewModel.playlists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val selectedContentType by viewModel.selectedContentType.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = accountName,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isLoggedIn) {
                            IconButton(
                                onClick = {
                                    navController.navigate("settings/account_sync")
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                windowInsets = TopBarInsets,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val density = LocalDensity.current
        val playerAwarePaddingValues = LocalPlayerAwareWindowInsets.current.asPaddingValues(density)

        val startPadding = playerAwarePaddingValues.calculateStartPadding(layoutDirection)
        val endPadding = playerAwarePaddingValues.calculateEndPadding(layoutDirection)
        val bottomPadding = playerAwarePaddingValues.calculateBottomPadding()

        val gridContentPadding = PaddingValues(
            start = startPadding,
            end = endPadding,
            bottom = bottomPadding,
            top = 0.dp
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
            contentPadding = gridContentPadding, // Use the new padding
            modifier = Modifier.padding(paddingValues) // Positions the grid below the TopAppBar
        ) {
            if (!isLoggedIn) {
                item {
                    Column {
                        PreferenceGroupTitle(
                            title = stringResource(R.string.account)
                        )
                        AccountFrag(navController)
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                ChipsRow(
                    chips = listOf(
                        AccountContentType.PLAYLISTS to stringResource(R.string.filter_playlists),
                        AccountContentType.ALBUMS to stringResource(R.string.filter_albums),
                        AccountContentType.ARTISTS to stringResource(R.string.filter_artists),
                    ),
                    currentValue = selectedContentType,
                    onValueUpdate = { viewModel.setSelectedContentType(it) },
                )
            }

            when (selectedContentType) {
                AccountContentType.PLAYLISTS -> {
                    items(
                        items = playlists.orEmpty().distinctBy { it.id },
                        key = { it.id },
                    ) { item ->
                        YouTubeGridItem(
                            item = item,
                            fillMaxWidth = true,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        navController.navigate("online_playlist/${item.id}")
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubePlaylistMenu(
                                                playlist = item,
                                                coroutineScope = coroutineScope,
                                                onDismiss = menuState::dismiss,
                                                navController = navController
                                            )
                                        }
                                    },
                                ),
                        )
                    }

                    if (playlists == null) {
                        items(8) {
                            ShimmerHost {
                                GridItemPlaceHolder(fillMaxWidth = true)
                            }
                        }
                    }
                }

                AccountContentType.ALBUMS -> {
                    items(
                        items = albums.orEmpty().distinctBy { it.id },
                        key = { it.id }
                    ) { item ->
                        YouTubeGridItem(
                            item = item,
                            fillMaxWidth = true,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        navController.navigate("album/${item.id}")
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubeAlbumMenu(
                                                albumItem = item,
                                                navController = navController,
                                                onDismiss = menuState::dismiss
                                            )
                                        }
                                    }
                                )
                        )
                    }

                    if (albums == null) {
                        items(8) {
                            ShimmerHost {
                                GridItemPlaceHolder(fillMaxWidth = true)
                            }
                        }
                    }
                }

                AccountContentType.ARTISTS -> {
                    items(
                        items = artists.orEmpty().distinctBy { it.id },
                        key = { it.id }
                    ) { item ->
                        YouTubeGridItem(
                            item = item,
                            fillMaxWidth = true,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        navController.navigate("artist/${item.id}")
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubeArtistMenu(
                                                artist = item,
                                                onDismiss = menuState::dismiss
                                            )
                                        }
                                    }
                                )
                        )
                    }

                    if (isLoggedIn && (artists == null && isLoading < 3)) {
                        items(8) {
                            ShimmerHost {
                                GridItemPlaceHolder(fillMaxWidth = true)
                            }
                        }
                    }
                }
            }
        }
    }
}
