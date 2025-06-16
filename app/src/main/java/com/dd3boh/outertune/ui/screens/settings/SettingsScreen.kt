package com.dd3boh.outertune.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.ENABLE_UPDATE_CHECKER
import com.dd3boh.outertune.constants.LastVersionKey
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.constants.UpdateAvailableKey
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.utils.Updater
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.compareVersion
import com.dd3boh.outertune.utils.rememberPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

const val SETTINGS_TAG = "Settings"

sealed class IconType {
    data class Vector(val imageVector: ImageVector) : IconType()
    data class Resource(val resId: Int) : IconType()
}

data class SettingsSection(
    val icon: ImageVector,
    val containerColor: Color,
    val iconColor: Color,
    val items: List<SettingsItem>
)

data class SettingsItem(
    val title: String,
    val icon: IconType,
    val route: String,
    val isSpecial: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val lastVer by rememberPreference(LastVersionKey, defaultValue = "0.0.0")
    val (updateAvailable, onUpdateAvailableChange) = rememberPreference(UpdateAvailableKey, defaultValue = false)

    val settingsSections = remember {
        listOf(
            SettingsSection(
                icon = Icons.Rounded.AccountCircle,
                containerColor = Color(0xFF6750A4),
                iconColor = Color.White,
                items = listOf(
                    SettingsItem(context.getString(R.string.grp_account_sync), IconType.Vector(Icons.Rounded.AccountCircle), "settings/account_sync"),
                    SettingsItem(context.getString(R.string.grp_library_and_content), IconType.Vector(Icons.AutoMirrored.Rounded.LibraryBooks), "settings/library")
                )
            ),
            SettingsSection(
                icon = Icons.Rounded.Palette,
                containerColor = Color(0xFF00A9FF),
                iconColor = Color.White,
                items = listOf(
                    SettingsItem(context.getString(R.string.appearance), IconType.Vector(Icons.Rounded.Palette), "settings/appearance"),
                    SettingsItem(context.getString(R.string.grp_interface), IconType.Vector(Icons.Rounded.Interests), "settings/interface")
                )
            ),
            SettingsSection(
                icon = Icons.Rounded.PlayArrow,
                containerColor = Color(0xFF4CAF50),
                iconColor = Color.White,
                items = listOf(
                    SettingsItem(context.getString(R.string.player_and_audio), IconType.Vector(Icons.Rounded.PlayArrow), "settings/player")
                )
            ),
            SettingsSection(
                icon = Icons.Rounded.Storage,
                containerColor = Color(0xFFFF9800),
                iconColor = Color.White,
                items = listOf(
                    SettingsItem(context.getString(R.string.backup_restore), IconType.Vector(Icons.Rounded.Restore), "settings/backup_restore"),
                    SettingsItem(context.getString(R.string.storage), IconType.Vector(Icons.Rounded.Storage), "settings/storage")
                )
            ),
            SettingsSection(
                icon = Icons.Rounded.WarningAmber,
                containerColor = Color(0xFFE91E63),
                iconColor = Color.White,
                items = listOf(
                    SettingsItem(context.getString(R.string.discord_integration), IconType.Resource(R.drawable.discord), "settings/discord"),
                    SettingsItem(context.getString(R.string.experimental_settings_title), IconType.Vector(Icons.Rounded.WarningAmber), "settings/experimental")
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        settingsSections.forEach { section ->
            SettingsSection(
                section = section,
                navController = navController
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
            ) {
                PreferenceEntry(
                    title = {
                        Text(
                            stringResource(R.string.about),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color(0xFF9C27B0),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Info,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    onClick = { navController.navigate("settings/about") }
                )

                if (ENABLE_UPDATE_CHECKER) {
                    PreferenceEntry(
                        title = {
                            Text(
                                text = stringResource(if (updateAvailable) R.string.new_version_available else R.string.check_for_update),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (updateAvailable) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 16.sp,
                                color = if (updateAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        description = if (updateAvailable) lastVer else stringResource(R.string.no_updates_available),
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (updateAvailable) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (updateAvailable) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.secondary,
                                            RoundedCornerShape(16.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Update,
                                        null,
                                        tint = if (updateAvailable) MaterialTheme.colorScheme.onError
                                        else MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            if (updateAvailable) {
                                uriHandler.openUri("https://github.com/OuterTune/OuterTune/releases/latest")
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    Updater.tryCheckUpdate(context, true)?.let {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.check_for_update),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        if (compareVersion(lastVer, it) < 0) {
                                            onUpdateAvailableChange(true)
                                            Timber.tag(SETTINGS_TAG).d("Update available. UpdateAvailable set to true")
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.new_version_available),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Timber.tag(SETTINGS_TAG).d("No new updates available")
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.no_updates_available),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    TopAppBar(
        title = {
            Text(
                stringResource(R.string.settings)
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
fun SettingsSection(
    section: SettingsSection,
    navController: NavController
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        section.items.forEach { item ->
            PreferenceEntry(
                title = {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                section.containerColor,
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (val icon = item.icon) {
                            is IconType.Vector -> Icon(
                                imageVector = icon.imageVector,
                                contentDescription = null,
                                tint = section.iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                            is IconType.Resource -> Icon(
                                painter = painterResource(icon.resId),
                                contentDescription = null,
                                tint = section.iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}