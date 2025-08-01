/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 O‌ute‌rTu‌ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens.settings

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.ui.component.ColumnWithContentPadding
import com.dd3boh.outertune.ui.component.button.IconButton
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SettingsClickToReveal
import com.dd3boh.outertune.ui.screens.settings.fragments.AccountFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.SyncAutoFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.SyncExtrasFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.SyncManualFrag
import com.dd3boh.outertune.ui.screens.settings.fragments.SyncParamsFrag
import com.dd3boh.outertune.ui.utils.backToMain

@SuppressLint("PrivateResource")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSyncSettings(
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
            title = stringResource(R.string.account)
        )

        AccountFrag(navController)

        Spacer(modifier = Modifier.height(12.dp))

        PreferenceGroupTitle(
            title = stringResource(R.string.grp_sync)
        )

        SyncAutoFrag()

        Spacer(modifier = Modifier.height(12.dp))

        SyncManualFrag()

        Spacer(modifier = Modifier.height(12.dp))

        SyncParamsFrag()

        Spacer(modifier = Modifier.height(12.dp))

        SyncExtrasFrag()

        Spacer(modifier = Modifier.height(12.dp))
        SettingsClickToReveal(stringResource(R.string.prefs_advanced)) {
            PreferenceEntry(
                title = { Text(stringResource(R.string.spot_import_title)) },
                description = null,
                icon = { Icon(painterResource(R.drawable.spotify), null) },
                onClick = {
                    navController.navigate("settings/content/import_from_spotify")
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.grp_account_sync)) },
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
