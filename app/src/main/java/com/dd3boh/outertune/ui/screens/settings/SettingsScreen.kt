package com.dd3boh.outertune.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Interests
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.LocalSnackbarHostState
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.ENABLE_UPDATE_CHECKER
import com.dd3boh.outertune.constants.LastVersionKey
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.constants.UpdateAvailableKey
import com.dd3boh.outertune.ui.component.SettingsScreenSection
import com.dd3boh.outertune.ui.component.button.IconButton
import com.dd3boh.outertune.ui.utils.Updater
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.compareVersion
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

const val SETTINGS_TAG = "Settings"

sealed class IconType {
    data class Vector(val imageVector: ImageVector) : IconType()
    data class Resource(val resId: Int) : IconType()
}

data class SettingsSection(
    val items: List<SettingsItem>
)

data class SettingsItem(
    val title: String,
    val description: String = "",
    val icon: IconType,
    val route: String,
    val iconColor: Color,
    val isSpecial: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val snackbarHostState = LocalSnackbarHostState.current
    val uriHandler = LocalUriHandler.current

    val lastVer by rememberPreference(LastVersionKey, defaultValue = "0.0.0")
    val (updateAvailable, onUpdateAvailableChange) = rememberPreference(UpdateAvailableKey, defaultValue = false)

    val settingsSections = remember {
        listOf(
            SettingsSection(
                items = listOf(
                    SettingsItem(
                        title = context.getString(R.string.grp_account_sync),
                        description = context.getString(R.string.settings_account_sync_description),
                        icon = IconType.Vector(Icons.Outlined.AccountCircle),
                        route = "settings/account_sync",
                        iconColor = Color(0xFF1976D2)
                    ),
                    SettingsItem(
                        title = context.getString(R.string.grp_library_and_content),
                        description = context.getString(R.string.settings_library_content_description),
                        icon = IconType.Vector(Icons.AutoMirrored.Outlined.LibraryBooks),
                        route = "settings/library",
                        iconColor = Color(0xFF1976D2)
                    )
                )
            ),
            SettingsSection(
                items = listOf(
                    SettingsItem(
                        title = context.getString(R.string.appearance),
                        description = context.getString(R.string.settings_appearance_description),
                        icon = IconType.Vector(Icons.Outlined.Palette),
                        route = "settings/appearance",
                        iconColor = Color(0xFF7B1FA2)
                    ),
                    SettingsItem(
                        title = context.getString(R.string.grp_interface),
                        description = context.getString(R.string.settings_interface_description),
                        icon = IconType.Vector(Icons.Outlined.Interests),
                        route = "settings/interface",
                        iconColor = Color(0xFF7B1FA2)
                    )
                )
            ),
            SettingsSection(
                items = listOf(
                    SettingsItem(
                        title = context.getString(R.string.player_and_audio),
                        description = context.getString(R.string.settings_player_audio_description),
                        icon = IconType.Vector(Icons.Outlined.PlayArrow),
                        route = "settings/player",
                        iconColor = Color(0xFF4CAF50)
                    )
                )
            ),
            SettingsSection(
                items = listOf(
                    SettingsItem(
                        title = context.getString(R.string.backup_restore),
                        description = context.getString(R.string.settings_backup_restore_description),
                        icon = IconType.Vector(Icons.Outlined.Restore),
                        route = "settings/backup_restore",
                        iconColor = Color(0xFFFF8F00)
                    ),
                    SettingsItem(
                        title = context.getString(R.string.storage),
                        description = context.getString(R.string.settings_storage_description),
                        icon = IconType.Vector(Icons.Outlined.Storage),
                        route = "settings/storage",
                        iconColor = Color(0xFFFF8F00)
                    )
                )
            ),
            SettingsSection(
                items = listOf(
                    SettingsItem(
                        title = context.getString(R.string.discord_integration),
                        description = context.getString(R.string.settings_discord_description),
                        icon = IconType.Resource(R.drawable.discord),
                        route = "settings/discord",
                        iconColor = Color(0xFF5865F2)
                    ),
                    SettingsItem(
                        title = context.getString(R.string.experimental_settings_title),
                        description = context.getString(R.string.settings_experimental_description),
                        icon = IconType.Vector(Icons.Outlined.WarningAmber),
                        route = "settings/experimental",
                        iconColor = Color(0xFFE91E63)
                    )
                )
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(settingsSections) { section ->
            SettingsScreenSection(
                section = section,
                navController = navController
            )
        }

        item {
            SettingsScreenSection(
                section = SettingsSection(
                    items = listOf(
                        SettingsItem(
                            title = stringResource(R.string.about),
                            description = stringResource(R.string.settings_about_description),
                            icon = IconType.Vector(Icons.Outlined.Info),
                            route = "settings/about",
                            iconColor = Color(0xFF9C27B0)
                        )
                    )
                ),
                navController = navController
            )
        }

        if (ENABLE_UPDATE_CHECKER) {
            item {
                SettingsScreenSection(
                    section = SettingsSection(
                        items = listOf(
                            SettingsItem(
                                title = stringResource(if (updateAvailable) R.string.new_version_available else R.string.check_for_update),
                                description = if (updateAvailable) stringResource(R.string.new_version_available_description, lastVer) else stringResource(R.string.settings_check_for_update_description),
                                icon = IconType.Vector(Icons.Outlined.Update),
                                route = "",
                                iconColor = if (updateAvailable) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                            )
                        )
                    ),
                    navController = navController,
                    showBadge = updateAvailable,
                    onUpdateClick = {
                        if (updateAvailable) {
                            uriHandler.openUri("https://github.com/OuterTune/OuterTune/releases/latest")
                        } else {
                            CoroutineScope(Dispatchers.IO).launch {
                                Updater.tryCheckUpdate(context, true)?.let {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.check_for_update),
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (compareVersion(lastVer, it) < 0) {
                                        onUpdateAvailableChange(true)
                                        Timber.tag(SETTINGS_TAG).d("Update available. UpdateAvailable set to true")
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.new_version_available),
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )
                                    } else {
                                        Timber.tag(SETTINGS_TAG).d("No new updates available")
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.no_updates_available),
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    TopAppBar(
        title = {
            Text(
                stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
        },
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