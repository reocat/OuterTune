/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */
package com.dd3boh.outertune.ui.screens.settings.fragments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import com.dd3boh.outertune.App.Companion.forgetAccount
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.AccountChannelHandleKey
import com.dd3boh.outertune.constants.AccountEmailKey
import com.dd3boh.outertune.constants.AccountNameKey
import com.dd3boh.outertune.constants.AccountPfpUrlKey
import com.dd3boh.outertune.constants.DataSyncIdKey
import com.dd3boh.outertune.constants.InnerTubeCookieKey
import com.dd3boh.outertune.constants.UseLoginForBrowse
import com.dd3boh.outertune.constants.VisitorDataKey
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.dialog.EditorDialog
import com.dd3boh.outertune.ui.dialog.InfoLabel
import com.dd3boh.outertune.utils.rememberPreference
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.utils.parseCookieString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.AccountFrag(navController: NavController) {
    val context = LocalContext.current

    val (accountName, onAccountNameChange) = rememberPreference(AccountNameKey, "")
    val (accountEmail, onAccountEmailChange) = rememberPreference(AccountEmailKey, "")
    val (accountChannelHandle, onAccountChannelHandleChange) = rememberPreference(AccountChannelHandleKey, "")
    val (accountPfpUrl, onAccountPfpUrlChange) = rememberPreference(AccountPfpUrlKey, "")
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val (visitorData, onVisitorDataChange) = rememberPreference(VisitorDataKey, "")
    val (dataSyncId, onDataSyncIdChange) = rememberPreference(DataSyncIdKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val (useLoginForBrowse, onUseLoginForBrowseChange) = rememberPreference(UseLoginForBrowse, true)

    // temp vars
    var showToken: Boolean by remember {
        mutableStateOf(false)
    }
    var showTokenEditor by remember {
        mutableStateOf(false)
    }

    PreferenceEntry(
        title = {
            Text(
                text = if (isLoggedIn) accountName.takeIf { it.isNotEmpty() }
                    ?: stringResource(R.string.account_connected)
                else stringResource(R.string.login),
                fontWeight = FontWeight.Medium
            )
        },
        description = if (isLoggedIn) {
            accountEmail.takeIf { it.isNotEmpty() }
                ?: accountChannelHandle.takeIf { it.isNotEmpty() }
                ?: "Connected to YouTube Music"
        } else {
            stringResource(R.string.login_required_description)
        },
        onClick = {
            if (isLoggedIn) {
                onInnerTubeCookieChange("")
            } else {
                navController.navigate("login")
            }
        },
        icon = {
            if (isLoggedIn && accountPfpUrl.isNotEmpty()) {
                SubcomposeAsyncImage(
                    model = accountPfpUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    loading = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    error = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = if (isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            FilledTonalIconButton(
                onClick = {
                    if (isLoggedIn) {
                        forgetAccount(context)
                    } else {
                        navController.navigate("login")
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isLoggedIn) Icons.AutoMirrored.Outlined.Logout else Icons.AutoMirrored.Outlined.Login,
                    contentDescription = if (isLoggedIn) stringResource(R.string.logout) else stringResource(R.string.login),
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        isFirst = true
    )

    PreferenceEntry(
        title = {
            if (showToken) {
                Column {
                    Text(stringResource(R.string.token_shown), fontWeight = FontWeight.Medium)
                    Text(
                        text = innerTubeCookie.takeIf { it.isNotEmpty() } ?: "No token set",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Light,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(stringResource(R.string.token_hidden), fontWeight = FontWeight.Medium)
            }
        },
        description = if (!showToken) stringResource(R.string.token_description) else null,
        onClick = {
            if (!showToken) {
                showToken = true
            } else {
                showTokenEditor = true
            }
        },
        icon = {
            Icon(Icons.Outlined.Key, null)
        },
        trailingContent = {
            FilledTonalIconButton(
                onClick = { showTokenEditor = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "Edit token",
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        isLast = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    SwitchPreference(
        title = { Text(stringResource(R.string.use_login_for_browse)) },
        description = stringResource(R.string.use_login_for_browse_desc),
        icon = { Icon(Icons.Outlined.Person, null) },
        checked = useLoginForBrowse,
        onCheckedChange = {
            YouTube.useLoginForBrowse = it
            onUseLoginForBrowseChange(it)
        }
    )


    /**
     * ---------------------------
     * Dialogs
     * ---------------------------
     */


    if (showTokenEditor) {
        val initialText = buildString {
            append("***INNERTUBE COOKIE*** =$innerTubeCookie\n\n")
            append("***VISITOR DATA*** =$visitorData\n\n")
            append("***DATASYNC ID*** =$dataSyncId\n\n")
            append("***ACCOUNT NAME*** =$accountName\n\n")
            append("***ACCOUNT EMAIL*** =$accountEmail\n\n")
            append("***ACCOUNT CHANNEL HANDLE*** =$accountChannelHandle")
        }

        fun processAndSaveToken(data: String) {
            data.split("\n").forEach {
                when {
                    it.startsWith("***INNERTUBE COOKIE*** =") ->
                        onInnerTubeCookieChange(it.substringAfter("***INNERTUBE COOKIE*** ="))
                    it.startsWith("***VISITOR DATA*** =") ->
                        onVisitorDataChange(it.substringAfter("***VISITOR DATA*** ="))
                    it.startsWith("***DATASYNC ID*** =") ->
                        onDataSyncIdChange(it.substringAfter("***DATASYNC ID*** ="))
                    it.startsWith("***ACCOUNT NAME*** =") ->
                        onAccountNameChange(it.substringAfter("***ACCOUNT NAME*** ="))
                    it.startsWith("***ACCOUNT EMAIL*** =") ->
                        onAccountEmailChange(it.substringAfter("***ACCOUNT EMAIL*** ="))
                    it.startsWith("***ACCOUNT CHANNEL HANDLE*** =") ->
                        onAccountChannelHandleChange(it.substringAfter("***ACCOUNT CHANNEL HANDLE*** ="))
                }
            }
            showTokenEditor = false
        }

        EditorDialog(
            title = stringResource(R.string.edit_token),
            label = stringResource(R.string.token),
            initialValue = initialText,
            onDone = {
                processAndSaveToken(it)
            },
            onDismiss = { showTokenEditor = false },
            validation = {
                val cookieLine = it.split("\n").find { line ->
                    line.startsWith("***INNERTUBE COOKIE*** =")
                }?.substringAfter("***INNERTUBE COOKIE*** =") ?: ""
                cookieLine.isNotEmpty() && "SAPISID" in parseCookieString(cookieLine)
            },
            errorMessage = stringResource(R.string.invalid_token),
            content = {
                Spacer(modifier = Modifier.height(8.dp))
                InfoLabel(text = stringResource(R.string.token_adv_login_description))
            }
        )
    }
}