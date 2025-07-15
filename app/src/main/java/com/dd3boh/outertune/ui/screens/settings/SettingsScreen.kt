package com.dd3boh.outertune.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalSnackbarHostState
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.ENABLE_UPDATE_CHECKER
import com.dd3boh.outertune.constants.LastVersionKey
import com.dd3boh.outertune.constants.SNACKBAR_VERY_SHORT
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.constants.UpdateAvailableKey
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.utils.Updater
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.compareVersion
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                        description = "YouTube Music, sync preferences",
                        icon = IconType.Vector(Icons.Rounded.AccountCircle),
                        route = "settings/account_sync",
                        iconColor = Color(0xFF1976D2)
                    ),
                    SettingsItem(
                        title = context.getString(R.string.grp_library_and_content),
                        description = "Downloads, playlists, recommendations",
                        icon = IconType.Vector(Icons.AutoMirrored.Rounded.LibraryBooks),
                        route = "settings/library",
                        iconColor = Color(0xFF1976D2)
                    )
                )
            ),
            SettingsSection(
                items = listOf(
                    SettingsItem(
                        title = context.getString(R.string.appearance),
                        description = "Theme, colors, dark mode",
                        icon = IconType.Vector(Icons.Rounded.Palette),
                        route = "settings/appearance",
                        iconColor = Color(0xFF7B1FA2)
                    ),
                    SettingsItem(
                        title = context.getString(R.string.grp_interface),
                        description = "Layout, navigation, gestures",
                        icon = IconType.Vector(Icons.Rounded.Interests),
                        route = "settings/interface",
                        iconColor = Color(0xFF7B1FA2)
                    )
                )
            ),
            SettingsSection(
                items = listOf(
                    SettingsItem(
                        title = context.getString(R.string.player_and_audio),
                        description = "Playback, quality, equalizer",
                        icon = IconType.Vector(Icons.Rounded.PlayArrow),
                        route = "settings/player",
                        iconColor = Color(0xFF4CAF50)
                    )
                )
            ),
            SettingsSection(
                items = listOf(
                    SettingsItem(
                        title = context.getString(R.string.backup_restore),
                        description = "Import, export, sync data",
                        icon = IconType.Vector(Icons.Rounded.Restore),
                        route = "settings/backup_restore",
                        iconColor = Color(0xFFFF8F00)
                    ),
                    SettingsItem(
                        title = context.getString(R.string.storage),
                        description = "Cache, downloads, space usage",
                        icon = IconType.Vector(Icons.Rounded.Storage),
                        route = "settings/storage",
                        iconColor = Color(0xFFFF8F00)
                    )
                )
            ),
            SettingsSection(
                items = listOf(
                    SettingsItem(
                        title = context.getString(R.string.discord_integration),
                        description = "Rich presence, status updates",
                        icon = IconType.Resource(R.drawable.discord),
                        route = "settings/discord",
                        iconColor = Color(0xFF5865F2)
                    ),
                    SettingsItem(
                        title = context.getString(R.string.experimental_settings_title),
                        description = "Beta features, advanced options",
                        icon = IconType.Vector(Icons.Rounded.WarningAmber),
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
            ModernSettingsSection(
                section = section,
                navController = navController
            )
        }

        item {
            ModernSettingsSection(
                section = SettingsSection(
                    items = listOf(
                        SettingsItem(
                            title = stringResource(R.string.about),
                            description = "Version, licenses, support",
                            icon = IconType.Vector(Icons.Rounded.Info),
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
                ModernSettingsSection(
                    section = SettingsSection(
                        items = listOf(
                            SettingsItem(
                                title = stringResource(if (updateAvailable) R.string.new_version_available else R.string.check_for_update),
                                description = if (updateAvailable) "Version $lastVer available" else stringResource(R.string.no_updates_available),
                                icon = IconType.Vector(Icons.Rounded.Update),
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
            Spacer(modifier = Modifier.height(80.dp))
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
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
        },
        windowInsets = TopBarInsets,
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun ModernSettingsSection(
    section: SettingsSection,
    navController: NavController,
    showBadge: Boolean = false,
    onUpdateClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        section.items.forEachIndexed { index, item ->
            val isFirst = index == 0
            val isLast = index == section.items.lastIndex
            val isSingle = section.items.size == 1

            ModernSettingsItem(
                item = item,
                onClick = {
                    if (onUpdateClick != null && item.route.isEmpty()) {
                        onUpdateClick()
                    } else {
                        navController.navigate(item.route)
                    }
                },
                showBadge = showBadge && item.icon is IconType.Vector && item.icon.imageVector == Icons.Rounded.Update,
                isFirst = isFirst,
                isLast = isLast,
                isSingle = isSingle
            )
        }
    }
}

@Composable
fun ModernSettingsItem(
    item: SettingsItem,
    onClick: () -> Unit,
    showBadge: Boolean = false,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    isSingle: Boolean = false
) {
    val cardShape = when {
        isSingle -> RoundedCornerShape(12.dp)
        isFirst -> RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = 2.dp,
            bottomEnd = 2.dp
        )
        isLast -> RoundedCornerShape(
            topStart = 2.dp,
            topEnd = 2.dp,
            bottomStart = 12.dp,
            bottomEnd = 12.dp
        )
        else -> RoundedCornerShape(2.dp)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BadgedBox(
                badge = {
                    if (showBadge) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                }
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = item.iconColor.copy(alpha = 0.12f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        when (val icon = item.icon) {
                            is IconType.Vector -> Icon(
                                imageVector = icon.imageVector,
                                contentDescription = null,
                                tint = item.iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            is IconType.Resource -> Icon(
                                painter = painterResource(icon.resId),
                                contentDescription = null,
                                tint = item.iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.description.isNotEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}