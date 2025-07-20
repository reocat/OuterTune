/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.NavigateBefore
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.AccountChannelHandleKey
import com.dd3boh.outertune.constants.AccountEmailKey
import com.dd3boh.outertune.constants.AccountNameKey
import com.dd3boh.outertune.constants.AutomaticScannerKey
import com.dd3boh.outertune.constants.ContentCountryKey
import com.dd3boh.outertune.constants.ContentLanguageKey
import com.dd3boh.outertune.constants.CountryCodeToName
import com.dd3boh.outertune.constants.DarkMode
import com.dd3boh.outertune.constants.DarkModeKey
import com.dd3boh.outertune.constants.DataSyncIdKey
import com.dd3boh.outertune.constants.InnerTubeCookieKey
import com.dd3boh.outertune.constants.LanguageCodeToName
import com.dd3boh.outertune.constants.LocalLibraryEnableKey
import com.dd3boh.outertune.constants.LyricTrimKey
import com.dd3boh.outertune.constants.OOBE_VERSION
import com.dd3boh.outertune.constants.OobeStatusKey
import com.dd3boh.outertune.constants.PureBlackKey
import com.dd3boh.outertune.constants.SYSTEM_DEFAULT
import com.dd3boh.outertune.constants.VisitorDataKey
import com.dd3boh.outertune.ui.component.EnumListPreference
import com.dd3boh.outertune.ui.component.ListPreference
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.component.TokenEditorDialog
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.utils.parseCookieString
import java.util.Locale

data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun SetupWizard(
    navController: NavController,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    val uriHandler = LocalUriHandler.current

    var oobeStatus by rememberPreference(OobeStatusKey, defaultValue = 0)

    val (contentLanguage, onContentLanguageChange) = rememberPreference(
        key = ContentLanguageKey,
        defaultValue = "system"
    )
    val (contentCountry, onContentCountryChange) = rememberPreference(key = ContentCountryKey, defaultValue = "system")

    // content prefs
    val (darkMode, onDarkModeChange) = rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)

    val (accountName, onAccountNameChange) = rememberPreference(AccountNameKey, "")
    val (accountEmail, onAccountEmailChange) = rememberPreference(AccountEmailKey, "")
    val (accountChannelHandle, onAccountChannelHandleChange) = rememberPreference(AccountChannelHandleKey, "")
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val (visitorData, onVisitorDataChange) = rememberPreference(VisitorDataKey, "")
    val (dataSyncId, onDataSyncIdChange) = rememberPreference(DataSyncIdKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }
    val (ytmSync, onYtmSyncChange) = rememberPreference(LyricTrimKey, defaultValue = true)

    // local media prefs
    val (localLibEnable, onLocalLibEnableChange) = rememberPreference(LocalLibraryEnableKey, defaultValue = true)
    val (autoScan, onAutoScanChange) = rememberPreference(AutomaticScannerKey, defaultValue = false)

    var position by remember {
        mutableIntStateOf(0)
    }

    val MAX_POS = 4

    if (position > 0) {
        BackHandler {
            position -= 1
        }
    }

    LaunchedEffect(oobeStatus) {
        if (oobeStatus >= OOBE_VERSION) {
            navController.navigateUp()
        }
    }

    val navBar = @Composable {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    if (position > 0) {
                        position -= 1
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.action_back),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateBefore,
                    contentDescription = null
                )
            }

            LinearProgressIndicator(
                progress = { position.toFloat() / MAX_POS },
                strokeCap = StrokeCap.Butt,
                drawStopIndicator = {},
                modifier = Modifier
                    .weight(1f, false)
                    .height(8.dp)
                    .padding(2.dp),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    if (position < MAX_POS) {
                        position += 1
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.action_next),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                    contentDescription = null
                )
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (position in 1..<MAX_POS) {
                Box(
                    Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                        .fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        navBar()
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(
                    PaddingValues(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        top = 0.dp,
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding()
                    )
                )
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp))

                when (position) {
                    0 -> { // landing page
                        WelcomePage(
                            onNext = { position += 1 },
                            onSkip = {
                                oobeStatus = OOBE_VERSION
                            },
                            onRestoreBackup = {
                                navController.navigate("settings/backup_restore")
                            }
                        )
                    }

                    1 -> {
                        InterfacePage(
                            navController = navController,
                            darkMode = darkMode,
                            onDarkModeChange = onDarkModeChange,
                            pureBlack = pureBlack,
                            onPureBlackChange = onPureBlackChange,
                            contentLanguage = contentLanguage,
                            onContentLanguageChange = onContentLanguageChange,
                            contentCountry = contentCountry,
                            onContentCountryChange = onContentCountryChange
                        )
                    }

                    2 -> {
                        AccountPage(
                            navController = navController,
                            isLoggedIn = isLoggedIn,
                            accountName = accountName,
                            accountEmail = accountEmail,
                            accountChannelHandle = accountChannelHandle,
                            innerTubeCookie = innerTubeCookie,
                            onInnerTubeCookieChange = onInnerTubeCookieChange,
                            ytmSync = ytmSync,
                            onYtmSyncChange = onYtmSyncChange,
                            visitorData = visitorData,
                            onVisitorDataChange = onVisitorDataChange,
                            dataSyncId = dataSyncId,
                            onDataSyncIdChange = onDataSyncIdChange,
                            onAccountNameChange = onAccountNameChange,
                            onAccountEmailChange = onAccountEmailChange,
                            onAccountChannelHandleChange = onAccountChannelHandleChange
                        )
                    }

                    3 -> {
                        LocalMediaPage(
                            navController = navController,
                            localLibEnable = localLibEnable,
                            onLocalLibEnableChange = onLocalLibEnableChange,
                            autoScan = autoScan,
                            onAutoScanChange = onAutoScanChange
                        )
                    }

                    4 -> {
                        FinalPage(
                            uriHandler = uriHandler,
                            onFinish = {
                                oobeStatus = OOBE_VERSION
                            }
                        )
                    }
                }
            }

            if (position == 0 || position == MAX_POS) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    onClick = {
                        if (position == 0) {
                            position += 1
                        } else {
                            oobeStatus = OOBE_VERSION
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomePage(
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onRestoreBackup: () -> Unit
) {
    val welcomeFeatures = listOf(
        Feature(
            title = stringResource(R.string.oobe_ytm_integration),
            description = stringResource(R.string.oobe_ytm_integration_description),
            icon = Icons.Outlined.MusicNote
        ),
        Feature(
            title = stringResource(R.string.oobe_ad_free_exp),
            description = stringResource(R.string.oobe_ad_free_exp_description),
            icon = Icons.Outlined.Block
        ),
        Feature(
            title = stringResource(R.string.oobe_local_music_support),
            description = stringResource(R.string.oobe_local_music_support_description),
            icon = Icons.Outlined.SdCard
        ),
        Feature(
            title = stringResource(R.string.oobe_cross_platform_sync),
            description = stringResource(R.string.oobe_cross_platform_sync_description),
            icon = Icons.Outlined.Sync
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.launcher_monochrome),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary, BlendMode.SrcIn),
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.surfaceColorAtElevation(
                        NavigationBarDefaults.Elevation
                    )
                )
                .clickable { }
        )

        Text(
            text = stringResource(R.string.oobe_welcome_message),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            welcomeFeatures.forEach { feature ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = feature.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
        ) {
            TextButton(
                onClick = onRestoreBackup
            ) {
                Text(
                    text = stringResource(R.string.oobe_use_backup),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            TextButton(
                onClick = onSkip
            ) {
                Text(
                    text = stringResource(R.string.action_skip),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun InterfacePage(
    navController: NavController,
    darkMode: DarkMode,
    onDarkModeChange: (DarkMode) -> Unit,
    pureBlack: Boolean,
    onPureBlackChange: (Boolean) -> Unit,
    contentLanguage: String,
    onContentLanguageChange: (String) -> Unit,
    contentCountry: String,
    onContentCountryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.DarkMode,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.grp_interface),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = stringResource(R.string.oobe_interface_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            EnumListPreference(
                title = { Text(stringResource(R.string.dark_theme)) },
                icon = { Icon(Icons.Outlined.DarkMode, null) },
                selectedValue = darkMode,
                onValueSelected = onDarkModeChange,
                valueText = {
                    when (it) {
                        DarkMode.ON -> stringResource(R.string.dark_theme_on)
                        DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                        DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
                    }
                }
            )

            SwitchPreference(
                title = { Text(stringResource(R.string.pure_black)) },
                icon = { Icon(Icons.Outlined.Contrast, null) },
                checked = pureBlack,
                onCheckedChange = onPureBlackChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            ListPreference(
                title = { Text(stringResource(R.string.content_language)) },
                icon = { Icon(Icons.Outlined.Language, null) },
                selectedValue = contentLanguage,
                values = listOf(SYSTEM_DEFAULT) + LanguageCodeToName.keys.toList(),
                valueText = {
                    LanguageCodeToName.getOrElse(it) {
                        stringResource(R.string.system_default)
                    }
                },
                onValueSelected = { newValue ->
                    val locale = Locale.getDefault()
                    val languageTag = locale.toLanguageTag().replace("-Hant", "")

                    YouTube.locale = YouTube.locale.copy(
                        hl = newValue.takeIf { it != SYSTEM_DEFAULT }
                            ?: locale.language.takeIf { it in LanguageCodeToName }
                            ?: languageTag.takeIf { it in LanguageCodeToName }
                            ?: "en"
                    )

                    onContentLanguageChange(newValue)
                }
            )

            ListPreference(
                title = { Text(stringResource(R.string.content_country)) },
                icon = { Icon(Icons.Outlined.LocationOn, null) },
                selectedValue = contentCountry,
                values = listOf(SYSTEM_DEFAULT) + CountryCodeToName.keys.toList(),
                valueText = {
                    CountryCodeToName.getOrElse(it) {
                        stringResource(R.string.system_default)
                    }
                },
                onValueSelected = { newValue ->
                    val locale = Locale.getDefault()

                    YouTube.locale = YouTube.locale.copy(
                        gl = newValue.takeIf { it != SYSTEM_DEFAULT }
                            ?: locale.country.takeIf { it in CountryCodeToName }
                            ?: "US"
                    )

                    onContentCountryChange(newValue)
                }
            )
        }
    }
}

@Composable
private fun AccountPage(
    navController: NavController,
    isLoggedIn: Boolean,
    accountName: String,
    accountEmail: String,
    accountChannelHandle: String,
    innerTubeCookie: String,
    onInnerTubeCookieChange: (String) -> Unit,
    ytmSync: Boolean,
    onYtmSyncChange: (Boolean) -> Unit,
    visitorData: String,
    onVisitorDataChange: (String) -> Unit,
    dataSyncId: String,
    onDataSyncIdChange: (String) -> Unit,
    onAccountNameChange: (String) -> Unit,
    onAccountEmailChange: (String) -> Unit,
    onAccountChannelHandleChange: (String) -> Unit
) {
    var showToken by remember { mutableStateOf(false) }
    var showTokenEditor by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.oobe_ytm_logon_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = stringResource(R.string.oobe_ytm_logon_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            PreferenceEntry(
                title = { Text(if (isLoggedIn) accountName else stringResource(R.string.login)) },
                description = if (isLoggedIn) {
                    accountEmail.takeIf { it.isNotEmpty() }
                        ?: accountChannelHandle.takeIf { it.isNotEmpty() }
                } else null,
                icon = { Icon(Icons.Outlined.Person, null) },
                onClick = { navController.navigate("login") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoggedIn) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.logout)) },
                    icon = { Icon(Icons.AutoMirrored.Outlined.Logout, null) },
                    onClick = { onInnerTubeCookieChange("") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            PreferenceEntry(
                title = {
                    if (showToken) {
                        Text(stringResource(R.string.token_shown))
                        Text(
                            text = if (isLoggedIn) innerTubeCookie else stringResource(R.string.not_logged_in),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    } else {
                        Text(stringResource(R.string.token_hidden))
                    }
                },
                onClick = {
                    if (!showToken) {
                        showToken = true
                    } else {
                        showTokenEditor = true
                    }
                },
                icon = {
                    Icon(Icons.Outlined.VpnKey, null)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.ytm_sync)) },
                icon = { Icon(Icons.Outlined.Lyrics, null) },
                checked = ytmSync,
                onCheckedChange = onYtmSyncChange,
                isEnabled = isLoggedIn
            )
        }

        if (showTokenEditor) {
            TokenEditorDialog(
                initialValue = innerTubeCookie,
                onDone = { newToken ->
                    onInnerTubeCookieChange(newToken)
                    showTokenEditor = false
                },
                onDismiss = { showTokenEditor = false },
                modifier = Modifier,
                visitorData = visitorData,
                dataSyncId = dataSyncId,
                accountName = accountName,
                accountEmail = accountEmail,
                accountChannelHandle = accountChannelHandle,
                onInnerTubeCookieChange = onInnerTubeCookieChange,
                onVisitorDataChange = onVisitorDataChange,
                onDataSyncIdChange = onDataSyncIdChange,
                onAccountNameChange = onAccountNameChange,
                onAccountEmailChange = onAccountEmailChange,
                onAccountChannelHandleChange = onAccountChannelHandleChange
            )
        }
    }
}
@Composable
private fun LocalMediaPage(
    navController: NavController,
    localLibEnable: Boolean,
    onLocalLibEnableChange: (Boolean) -> Unit,
    autoScan: Boolean,
    onAutoScanChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.LibraryMusic,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.oobe_local_media_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Text(
            text = stringResource(R.string.oobe_local_media_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.local_library_enable_title)) },
                description = stringResource(R.string.local_library_enable_description),
                icon = { Icon(Icons.Outlined.SdCard, null) },
                checked = localLibEnable,
                onCheckedChange = onLocalLibEnableChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.auto_scanner_title)) },
                description = stringResource(R.string.auto_scanner_description),
                icon = { Icon(Icons.Outlined.Autorenew, null) },
                checked = autoScan,
                onCheckedChange = onAutoScanChange,
                isEnabled = localLibEnable
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (localLibEnable) {
            ElevatedButton(
                onClick = { navController.navigate("settings/local") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(R.string.oobe_scan_for_local_music))
            }
        }
    }
}
@Composable
private fun FinalPage(
    uriHandler: UriHandler,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.oobe_complete_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Text(
            text = stringResource(R.string.oobe_complete),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            FilledTonalIconButton(
                onClick = { uriHandler.openUri("https://github.com/OuterTune/OuterTune") },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.github),
                    contentDescription = "GitHub",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Text(
            text = "${com.dd3boh.outertune.BuildConfig.VERSION_NAME} (${com.dd3boh.outertune.BuildConfig.VERSION_CODE}) | ${com.dd3boh.outertune.BuildConfig.FLAVOR}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}