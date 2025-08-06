/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.screens

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.NavigateBefore
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import coil3.compose.SubcomposeAsyncImage
import com.dd3boh.outertune.App.Companion.forgetAccount
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.AccountChannelHandleKey
import com.dd3boh.outertune.constants.AccountEmailKey
import com.dd3boh.outertune.constants.AccountNameKey
import com.dd3boh.outertune.constants.AccountPfpUrlKey
import com.dd3boh.outertune.constants.AutomaticScannerKey
import com.dd3boh.outertune.constants.ContentCountryKey
import com.dd3boh.outertune.constants.ContentLanguageKey
import com.dd3boh.outertune.constants.CountryCodeToName
import com.dd3boh.outertune.constants.DarkMode
import com.dd3boh.outertune.constants.DarkModeKey
import com.dd3boh.outertune.constants.DataSyncIdKey
import com.dd3boh.outertune.constants.DynamicThemeKey
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
import com.dd3boh.outertune.ui.component.PreferenceItem
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.component.TokenEditorDialog
import com.dd3boh.outertune.utils.rememberEnumPreference
import com.dd3boh.outertune.utils.rememberPreference
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.utils.parseCookieString
import kotlinx.coroutines.launch
import java.util.Locale

data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SetupWizard(
    navController: NavController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current
    val uriHandler = LocalUriHandler.current

    var oobeStatus by rememberPreference(OobeStatusKey, defaultValue = 0)

    val (contentLanguage, onContentLanguageChange) = rememberPreference(
        key = ContentLanguageKey,
        defaultValue = "system"
    )
    val (contentCountry, onContentCountryChange) = rememberPreference(
        key = ContentCountryKey,
        defaultValue = "system"
    )

    // content prefs
    val (darkMode, onDarkModeChange) = rememberEnumPreference(
        DarkModeKey,
        defaultValue = DarkMode.AUTO
    )
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(DynamicThemeKey, defaultValue = true)
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)

    val (accountName, onAccountNameChange) = rememberPreference(AccountNameKey, "")
    val (accountEmail, onAccountEmailChange) = rememberPreference(AccountEmailKey, "")
    val (accountChannelHandle, onAccountChannelHandleChange) = rememberPreference(
        AccountChannelHandleKey,
        ""
    )
    val (innerTubeCookie, onInnerTubeCookieChange) = rememberPreference(InnerTubeCookieKey, "")
    val (visitorData, onVisitorDataChange) = rememberPreference(VisitorDataKey, "")
    val (dataSyncId, onDataSyncIdChange) = rememberPreference(DataSyncIdKey, "")
    val (ytmSync, onYtmSyncChange) = rememberPreference(LyricTrimKey, defaultValue = true)

    // local media prefs
    val (localLibEnable, onLocalLibEnableChange) = rememberPreference(
        LocalLibraryEnableKey,
        defaultValue = true
    )
    val (autoScan, onAutoScanChange) = rememberPreference(
        AutomaticScannerKey,
        defaultValue = false
    )

    val pagerState = rememberPagerState(pageCount = { 5 })

    if (pagerState.currentPage > 0) {
        BackHandler {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage - 1)
            }
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
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                enabled = pagerState.currentPage > 0
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateBefore,
                    contentDescription = null
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.action_back),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            LinearProgressIndicator(
                progress = { (pagerState.currentPage + 1).toFloat() / pagerState.pageCount },
                strokeCap = StrokeCap.Butt,
                drawStopIndicator = {},
                modifier = Modifier
                    .weight(1f, false)
                    .height(8.dp)
                    .padding(2.dp),
            )

            TextButton(
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                enabled = pagerState.currentPage < pagerState.pageCount - 1
            ) {
                Text(
                    text = stringResource(R.string.action_next),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.NavigateNext,
                    contentDescription = null
                )
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (pagerState.currentPage in 1..<pagerState.pageCount - 1) {
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                                slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                    },
                    label = "page"
                ) { targetPage ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            when (targetPage) {
                                0 -> { // landing page
                                    WelcomePage(
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
                                        darkMode = darkMode,
                                        onDarkModeChange = onDarkModeChange,
                                        dynamicTheme = dynamicTheme,
                                        onDynamicThemeChange = onDynamicThemeChange,
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
                                        context = context,
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
                                        uriHandler = uriHandler
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (pagerState.currentPage == 0 || pagerState.currentPage == pagerState.pageCount - 1) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage == 0) {
                                pagerState.animateScrollToPage(1)
                            } else {
                                oobeStatus = OOBE_VERSION
                            }
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
private fun WizardPage(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.5f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            content()
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun WelcomePage(
    onSkip: () -> Unit,
    onRestoreBackup: () -> Unit,
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
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.5f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(R.drawable.launcher_monochrome),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary, BlendMode.SrcIn),
                modifier = Modifier
                    .size(100.dp)
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
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            welcomeFeatures.forEachIndexed { index, feature ->
                PreferenceItem(
                    title = { Text(feature.title, fontWeight = FontWeight.SemiBold) },
                    description = feature.description,
                    icon = {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    isFirst = index == 0,
                    isLast = index == welcomeFeatures.lastIndex,
                    isMiddle = index > 0 && index < welcomeFeatures.lastIndex
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = onRestoreBackup,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.oobe_use_backup),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f)
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
    darkMode: DarkMode,
    onDarkModeChange: (DarkMode) -> Unit,
    dynamicTheme: Boolean,
    onDynamicThemeChange: (Boolean) -> Unit,
    pureBlack: Boolean,
    onPureBlackChange: (Boolean) -> Unit,
    contentLanguage: String,
    onContentLanguageChange: (String) -> Unit,
    contentCountry: String,
    onContentCountryChange: (String) -> Unit,
) {
    WizardPage(
        title = stringResource(R.string.grp_interface),
        subtitle = stringResource(R.string.oobe_interface_subtitle),
        icon = Icons.Outlined.DarkMode
    ) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.enable_dynamic_theme)) },
                icon = { Icon(Icons.Outlined.Palette, null) },
                checked = dynamicTheme,
                onCheckedChange = onDynamicThemeChange,
                isFirst = true
            )
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
                },
                isMiddle = true
            )

            SwitchPreference(
                title = { Text(stringResource(R.string.pure_black)) },
                icon = { Icon(Icons.Outlined.Contrast, null) },
                checked = pureBlack,
                onCheckedChange = onPureBlackChange,
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Content Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
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
                },
                isFirst = true
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
                },
                isLast = true
            )
        }
    }
}

@Composable
private fun AccountPage(
    navController: NavController,
    context: Context,
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
    onAccountChannelHandleChange: (String) -> Unit,
) {
    var showToken by remember { mutableStateOf(false) }
    var showTokenEditor by remember { mutableStateOf(false) }
    val isLoggedIn by remember(innerTubeCookie) {
        mutableStateOf("SAPISID" in parseCookieString(innerTubeCookie))
    }
    val (accountPfpUrl, onAccountPfpUrlChange) = rememberPreference(AccountPfpUrlKey, "")

    WizardPage(
        title = stringResource(R.string.oobe_ytm_logon_title),
        subtitle = stringResource(R.string.oobe_ytm_logon_subtitle),
        icon = Icons.Outlined.AccountCircle
    ) {
        Text(
            text = "YouTube Music Integration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
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
                description = stringResource(R.string.token_description),
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
        }
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isLoggedIn) "Account Connected" else "Account Optional",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isLoggedIn) {
                            "You can now sync your playlists, liked songs, and preferences with YouTube Music."
                        } else {
                            "Connecting your account enables playlist sync, recommendations, and personalized features."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sync & Features",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.ytm_sync)) },
                description = stringResource(R.string.ytm_sync_description),
                icon = { Icon(Icons.Outlined.Lyrics, null) },
                checked = ytmSync,
                onCheckedChange = onYtmSyncChange,
                isEnabled = isLoggedIn
            )
        }
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

@Composable
private fun LocalMediaPage(
    navController: NavController,
    localLibEnable: Boolean,
    onLocalLibEnableChange: (Boolean) -> Unit,
    autoScan: Boolean,
    onAutoScanChange: (Boolean) -> Unit,
) {
    WizardPage(
        title = stringResource(R.string.oobe_local_media_title),
        subtitle = stringResource(R.string.oobe_local_media_subtitle),
        icon = Icons.Outlined.LibraryMusic
    ) {
        Text(
            text = "Local Library Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SwitchPreference(
                title = { Text(stringResource(R.string.local_library_enable_title)) },
                description = stringResource(R.string.local_library_enable_description),
                icon = { Icon(Icons.Outlined.SdCard, null) },
                checked = localLibEnable,
                onCheckedChange = onLocalLibEnableChange,
                isFirst = true
            )
            SwitchPreference(
                title = { Text(stringResource(R.string.auto_scanner_title)) },
                description = stringResource(R.string.auto_scanner_description),
                icon = { Icon(Icons.Outlined.Autorenew, null) },
                checked = autoScan,
                onCheckedChange = onAutoScanChange,
                isEnabled = localLibEnable,
                isLast = true
            )
        }

        if (localLibEnable) {
            Spacer(modifier = Modifier.height(12.dp))

            ElevatedButton(
                onClick = { navController.navigate("settings/local") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.oobe_scan_for_local_music),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FinalPage(
    uriHandler: UriHandler,
) {
    WizardPage(
        title = stringResource(R.string.oobe_complete_title),
        subtitle = stringResource(R.string.oobe_complete_subtitle),
        icon = Icons.Outlined.Check
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.oobe_ready_to_rock),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.oobe_ready_to_rock_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.support_and_info),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            PreferenceEntry(
                title = { Text(stringResource(R.string.star_on_github), fontWeight = FontWeight.Medium) },
                description = stringResource(R.string.star_on_github_desc),
                onClick = { uriHandler.openUri("https://github.com/OuterTune/OuterTune") },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.github),
                        contentDescription = "GitHub",
                        modifier = Modifier.size(24.dp)
                    )
                },
                isFirst = true
            )

            PreferenceEntry(
                title = { Text(stringResource(R.string.report_issue), fontWeight = FontWeight.Medium) },
                description = stringResource(R.string.report_issue_desc),
                onClick = { uriHandler.openUri("https://github.com/OuterTune/OuterTune/issues") },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.BugReport,
                        contentDescription = "Report Issue",
                        modifier = Modifier.size(24.dp)
                    )
                },
                isMiddle = true
            )

            val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
            val versionInfo = "${com.dd3boh.outertune.BuildConfig.VERSION_NAME} " +
                    "(${com.dd3boh.outertune.BuildConfig.VERSION_CODE})"
            val fullDescription = "$versionInfo\n$abi"

            PreferenceItem(
                title = { Text(stringResource(R.string.app_version), fontWeight = FontWeight.Medium) },
                description = fullDescription,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "App Version",
                        modifier = Modifier.size(24.dp)
                    )
                },
                isLast = true
            )
        }
    }
}