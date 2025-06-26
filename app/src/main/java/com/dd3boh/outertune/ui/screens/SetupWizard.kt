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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.VpnKey
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
import com.dd3boh.outertune.ui.utils.backToMain
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
                    imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
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
                    imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
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
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
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
            icon = Icons.Rounded.MusicNote
        ),
        Feature(
            title = stringResource(R.string.oobe_ad_free_exp),
            description = stringResource(R.string.oobe_ad_free_exp_description),
            icon = Icons.Rounded.Block
        ),
        Feature(
            title = stringResource(R.string.oobe_local_music_support),
            description = stringResource(R.string.oobe_local_music_support_description),
            icon = Icons.Rounded.SdCard
        ),
        Feature(
            title = stringResource(R.string.oobe_cross_platform_sync),
            description = stringResource(R.string.oobe_cross_platform_sync_description),
            icon = Icons.Rounded.Sync
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

        // Maybe add quick restore from backup here
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
            imageVector = Icons.Rounded.DarkMode,
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
                icon = { Icon(Icons.Rounded.DarkMode, null) },
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
                icon = { Icon(Icons.Rounded.Contrast, null) },
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
                icon = { Icon(Icons.Rounded.Language, null) },
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

            // Content country/region selector
            ListPreference(
                title = { Text(stringResource(R.string.content_country)) },
                icon = { Icon(Icons.Rounded.LocationOn, null) },
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
        // Title, Subtitle, and Icon
        Icon(
            imageVector = Icons.Rounded.AccountCircle,
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

        // Login/Account Info Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            PreferenceEntry(
                title = { Text(if (isLoggedIn) accountName else stringResource(R.string.login)) },
                description = if (isLoggedIn) {
                    accountEmail.takeIf { it.isNotEmpty() }
                        ?: accountChannelHandle.takeIf { it.isNotEmpty() }
                } else null,
                icon = { Icon(Icons.Rounded.Person, null) },
                onClick = { navController.navigate("login") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Card (if logged in)
        if (isLoggedIn) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.logout)) },
                    icon = { Icon(Icons.AutoMirrored.Rounded.Logout, null) },
                    onClick = { onInnerTubeCookieChange("") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Token Management Card
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
                    Icon(Icons.Rounded.VpnKey, null)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // YTM Sync Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.ytm_sync)) },
                icon = { Icon(Icons.Rounded.Lyrics, null) },
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
        // Title, Subtitle, and Icon
        Icon(
            imageVector = Icons.Rounded.LibraryMusic,
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
                icon = { Icon(Icons.Rounded.SdCard, null) },
                checked = localLibEnable,
                onCheckedChange = onLocalLibEnableChange
            )
        }

                        AnimatedVisibility(localLibEnable) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    SwitchPreference(
                                        title = { Text(stringResource(R.string.auto_scanner_title)) },
                                        description = stringResource(R.string.auto_scanner_description),
                                        icon = { Icon(Icons.Rounded.Autorenew, null) },
                                        checked = autoScan,
                                        onCheckedChange = onAutoScanChange
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    PreferenceGroupTitle(
                                        title = stringResource(R.string.grp_manual_scanner)
                                    )


                                    LocalScannerFrag()
                                }
                            }

                        }
                    }

                    // downloads
                    4 -> {
                        val downloadUtil = LocalDownloadUtil.current
                        val (downloadPath, onDownloadPathChange) = rememberPreference(DownloadPathKey, "")
                        val (scanPaths, onScanPathsChange) = rememberPreference(ScanPathsKey, defaultValue = "")

                        var showDlPathDialog: Boolean by remember {
                            mutableStateOf(false)
                        }


                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .padding(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = stringResource(R.string.oobe_downloads_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )

                        Text(
                            text = stringResource(R.string.oobe_downloads_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
                        )

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PreferenceEntry(
                                title = { Text(stringResource(R.string.dl_main_path_title)) },
                                onClick = {
                                    showDlPathDialog = true
                                },
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        InfoLabel(stringResource(R.string.dl_oobe_tooltip))


                        if (showDlPathDialog) {
                            var tempFilePath by remember {
                                mutableStateOf<Uri?>(null)
                            }
                            LaunchedEffect(downloadPath) {
                                tempFilePath = uriListFromString(downloadPath).firstOrNull()
                            }

                            ActionPromptDialog(
                                titleBar = {
                                    Text(
                                        text = stringResource(R.string.dl_main_path_title),
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                },
                                onDismiss = {
                                    showDlPathDialog = false
                                    tempFilePath = null
                                },
                                onConfirm = {
                                    tempFilePath?.let { f ->
                                        val uris = stringFromUriList(listOfNotNull(f))
                                        onDownloadPathChange(uris)
                                    }

                                    showDlPathDialog = false
                                    tempFilePath = null

                                    coroutineScope.launch {
                                        delay(1000)
                                        downloadUtil.cd()
                                    }
                                },
                                onReset = {
                                    tempFilePath = null
                                },
                                onCancel = {
                                    showDlPathDialog = false
                                    tempFilePath = null
                                },
                                isInputValid = uriListFromString(scanPaths).none {
                                    // download path cannot a scan path, or a subdir of a scan path
                                    tempFilePath.toString().length <= it.toString().length && tempFilePath.toString()
                                        .contains(it.toString())
                                }
                            ) {

                                val dirPickerLauncher = rememberLauncherForActivityResult(
                                    ActivityResultContracts.OpenDocumentTree()
                                ) { uri ->
                                    if (tempFilePath.toString() == uri.toString()) return@rememberLauncherForActivityResult
                                    if (uri?.path != null) {
                                        // Take persistable URI permission
                                        val contentResolver = context.contentResolver
                                        val takeFlags: Int =
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                        contentResolver.takePersistableUriPermission(uri, takeFlags)

                                        tempFilePath = uri
                                    }
                                }

                                val valid = uriListFromString(scanPaths).none {
                                    // download path cannot a scan path, or a subdir of a scan path
                                    tempFilePath.toString().length <= it.toString().length && tempFilePath.toString()
                                        .contains(it.toString())
                                }

                                Text(
                                    text = stringResource(R.string.dl_main_path_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Spacer(Modifier.padding(vertical = 8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            RoundedCornerShape(ThumbnailCornerRadius)
                                        )
                                        .background(if (valid) Color.Transparent else MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    tempFilePath?.let {
                                        Text(
                                            text = it.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                // add folder button
                                Column {
                                    Button(onClick = { dirPickerLauncher.launch(null) }) {
                                        Text(stringResource(R.string.scan_paths_add_folder))
                                    }

                                    InfoLabel(
                                        text = stringResource(R.string.scan_paths_tooltip),
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )

                                    if (!valid) {
                                        InfoLabel(
                                            text = stringResource(R.string.scanner_rejected_dir),
                                            isError = true,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // exit page
                    5 -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
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
                                IconLabelButton(
                                    text = "GitHub",
                                    icon = Icons.Rounded.Code,
                                    onClick = { uriHandler.openUri("https://github.com/OuterTune/OuterTune") },
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                IconLabelButton(
                                    text = "Wiki",
                                    icon = Icons.Outlined.Info,
                                    onClick = { uriHandler.openUri("https://github.com/OuterTune/OuterTune/wiki") },
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                            Text(
                                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) | ${BuildConfig.FLAVOR}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            if (oobeStatus == 0 || oobeStatus == MAX_POS) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    onClick = {
                        if (oobeStatus == 0) {
                            oobeStatus += 1
                        } else {
                            oobeStatus = OOBE_VERSION
                            navController.navigateUp()
                        }
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null
                    )
                }
            }
        }
    }
}


@Composable
private fun OobeFeatureRow(title: String, description: String?, icon: ImageVector, tint: Color) {
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            },
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
                    imageVector = icon,
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}