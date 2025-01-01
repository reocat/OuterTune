package com.dd3boh.outertune.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.LocalSyncUtils
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.DevSettingsKey
import com.dd3boh.outertune.constants.FirstSetupPassed
import com.dd3boh.outertune.ui.component.IconButton
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.rememberPreference
import com.dd3boh.outertune.utils.scanners.LocalMediaScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val syncUtils = LocalSyncUtils.current
    val coroutineScope = rememberCoroutineScope()

    // state variables and such
    val (devSettings, onDevSettingsChange) = rememberPreference(DevSettingsKey, defaultValue = false)
    val (firstSetupPassed, onFirstSetupPassedChange) = rememberPreference(FirstSetupPassed, defaultValue = false)

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .verticalScroll(rememberScrollState())
    ) {

        // dev settings
        SwitchPreference(
            title = { Text(stringResource(R.string.dev_settings_title)) },
            description = stringResource(R.string.dev_settings_description),
            icon = { Icon(Icons.Rounded.DeveloperMode, null) },
            checked = devSettings,
            onCheckedChange = onDevSettingsChange
        )

        PreferenceEntry(
            title = { Text("Trigger manual sync") },
            icon = { Icon(Icons.Rounded.Sync, null) },
            onClick = {
                Toast.makeText(context, "Syncing with YouTube account...", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch(Dispatchers.Main) {
                        syncUtils.syncAll()
                        Toast.makeText(context, "Sync complete", Toast.LENGTH_SHORT).show()
                    }
            }
        )

        if (devSettings) {
            PreferenceGroupTitle(
                title = stringResource(R.string.settings_debug)
            )
            PreferenceEntry(
                title = { Text("DEBUG: Nuke local lib") },
                icon = { Icon(Icons.Rounded.Backup, null) },
                onClick = {
                    Toast.makeText(context, "Nuking local files from database...", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch(Dispatchers.IO) {
                        Timber.tag("Settings").d("Nuke database status:  ${database.nukeLocalData()}")
                    }
                }
            )
            PreferenceEntry(
                title = { Text("DEBUG: Force local to remote artist migration NOW") },
                icon = { Icon(Icons.Rounded.Backup, null) },
                onClick = {
                    Toast.makeText(context, "Starting migration...", Toast.LENGTH_SHORT).show()
                    coroutineScope.launch(Dispatchers.IO) {
                        val scanner = LocalMediaScanner.getScanner()
                        Timber.tag("Settings").d("Force Migrating local artists to YTM (MANUAL TRIGGERED)")
                        scanner.localToRemoteArtist(database)
                    }
                }
            )


            PreferenceEntry(
                title = { Text("Enter configurator") },
                icon = { Icon(Icons.Rounded.ConfirmationNumber, null) },
                onClick = {
                    onFirstSetupPassedChange(false)
                    runBlocking { // hax. page loads before pref updates
                        delay(500)
                    }
                    navController.navigate("setup_wizard")
                }
            )



            Text("Material colours test")

            Column {
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.primary)) {
                    Text("Primary", color = MaterialTheme.colorScheme.onPrimary)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.secondary)) {
                    Text("Secondary", color = MaterialTheme.colorScheme.onSecondary)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.tertiary)) {
                    Text("Tertiary", color = MaterialTheme.colorScheme.onTertiary)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.surface)) {
                    Text("Surface", color = MaterialTheme.colorScheme.onSurface)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.inverseSurface)) {
                    Text("Inverse Surface", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Text("Surface Variant", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.surfaceBright)) {
                    Text("Surface Bright", color = MaterialTheme.colorScheme.onSurface)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.surfaceTint)) {
                    Text("Surface Tint", color = MaterialTheme.colorScheme.onSurface)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.surfaceDim)) {
                    Text("Surface Dim", color = MaterialTheme.colorScheme.onSurface)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
                    Text("Surface Container Highest", color = MaterialTheme.colorScheme.onSurface)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    Text("Surface Container High", color = MaterialTheme.colorScheme.onSurface)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Text("Surface Container Low", color = MaterialTheme.colorScheme.onSurface)
                }
                Row(Modifier.padding(10.dp).background(MaterialTheme.colorScheme.errorContainer)) {
                    Text("Error Container", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }




    TopAppBar(
        title = { Text(stringResource(R.string.experimental_settings_title)) },
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
        scrollBehavior = scrollBehavior
    )
}
