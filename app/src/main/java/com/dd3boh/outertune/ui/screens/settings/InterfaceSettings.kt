/*
 * Copyright (C) 2025 O‌ute‌rTu‌ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */
package com.dd3boh.outertune.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.ui.component.ColumnWithContentPadding
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.screens.settings.fragments.GridCellsSizeFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.LocalizationFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.SliderStyleFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.SwipeGesturesFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.TabArrangementFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.TabExtrasFrag
import com.dd3boh.outertune.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterfaceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {

    ColumnWithContentPadding(
        modifier = Modifier.fillMaxHeight(),
        columnModifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        PreferenceGroupTitle(
            title = stringResource(R.string.grp_layout)
        )

        GridCellsSizeFrag()
        Spacer(modifier = Modifier.height(12.dp))
        TabArrangementFrag()
        Spacer(modifier = Modifier.height(12.dp))

        TabExtrasFrag()
        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_behavior)
        )

        SwipeGesturesFrag()
        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_localization)
        )
        LocalizationFrag()
        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroupTitle(
            title = stringResource(R.string.misc)
        )
        SliderStyleFrag()
        Spacer(modifier = Modifier.height(12.dp))
    }


    TopAppBar(
        title = { Text(stringResource(R.string.grp_interface)) },
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