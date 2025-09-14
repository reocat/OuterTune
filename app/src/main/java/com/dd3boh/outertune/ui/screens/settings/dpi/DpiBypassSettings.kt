package com.dd3boh.outertune.ui.screens.settings.dpi

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.constants.DpiBypassCmdArgsKey
import com.dd3boh.outertune.constants.DpiBypassModeKey
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.dpi.DpiProxyService
import com.dd3boh.outertune.ui.component.ColumnWithContentPadding
import com.dd3boh.outertune.ui.component.PreferenceEntry
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.component.button.IconButton
import com.dd3boh.outertune.ui.dialog.EditorDialog
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DpiBypassSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current

    val (mode, onModeChange) = rememberPreference(DpiBypassModeKey, true)
    var showCmdDialog by remember { mutableStateOf(false) }
    val (cmdArgs, onCmdArgsChange) = rememberPreference(DpiBypassCmdArgsKey, defaultValue = "-d1 -d3+s -s6+s -d9+s -s12+s -d15+s -s20+s -d25+s -s30+s -d35+s -r1+s -S -a1 -As -d1 -d3+s -s6+s -d9+s -s12+s -d15+s -s20+s -d25+s -s30+s -d35+s -S -a1")

    ColumnWithContentPadding(
        modifier = Modifier.fillMaxHeight(),
        columnModifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        PreferenceGroupTitle(
            title = "Configuration Mode"
        )

        SwitchPreference(
            title = { Text("Use command line settings") },
            checked = mode,
            onCheckedChange = {
                onModeChange(it)
            }
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        PreferenceEntry(
            title = { Text("UI editor") },
            description = "Configure DPI bypass settings",
            icon = { Icon(Icons.Outlined.Settings, null) },
            onClick = {
                navController.navigate("settings/dpi/config")
            },
            isEnabled = !mode,
            isFirst = true
        )
        PreferenceEntry(
            title = { Text("Command line editor") },
            description = cmdArgs,
            icon = { Icon(Icons.Outlined.Settings, null) },
            onClick = {
                showCmdDialog = true
            },
            isEnabled = mode,
            isLast = true
        )
        Spacer(modifier = Modifier.height(96.dp))
    }

    if (showCmdDialog) {
        EditorDialog(
            title = "Command Line Configuration",
            label = "CMD Args",
            initialValue = cmdArgs,
            onDone = { newValue ->
                onCmdArgsChange(newValue)
                try {
                    val restartIntent = Intent(context, DpiProxyService::class.java)
                    restartIntent.action = DpiProxyService.ACTION_RESTART
                    context.startService(restartIntent)
                    Toast.makeText(context, "DPI proxy restarted with new configuration", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to restart DPI proxy: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                showCmdDialog = false
            },
            onDismiss = { showCmdDialog = false }
        )
    }

    TopAppBar(
        title = { Text("DPI Bypass Settings") },
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