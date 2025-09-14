package com.dd3boh.outertune.ui.screens.settings.dpi

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dd3boh.outertune.constants.DpiBypassCmdArgsKey
import com.dd3boh.outertune.constants.DpiBypassEnabledKey
import com.dd3boh.outertune.constants.TopBarInsets
import com.dd3boh.outertune.dpi.DpiProxyService
import com.dd3boh.outertune.ui.component.ColumnWithContentPadding
import com.dd3boh.outertune.ui.component.PreferenceGroupTitle
import com.dd3boh.outertune.ui.component.SwitchPreference
import com.dd3boh.outertune.ui.component.button.IconButton
import com.dd3boh.outertune.ui.utils.backToMain
import com.dd3boh.outertune.utils.rememberPreference

enum class DesyncMethod {
    None, Split, Disorder, Fake, OOB, DISOOB;

    companion object {
        fun fromName(name: String): DesyncMethod = when (name.lowercase()) {
            "none" -> None
            "split" -> Split
            "disorder" -> Disorder
            "fake" -> Fake
            "oob" -> OOB
            "disoob" -> DISOOB
            else -> OOB
        }
    }

    fun toName(): String = name.lowercase()
}

data class DpiSettings(
    val maxConnections: Int = 512,
    val bufferSize: Int = 16384,
    val desyncHttp: Boolean = true,
    val desyncHttps: Boolean = true,
    val desyncUdp: Boolean = true,
    val noDomain: Boolean = false,
    val tcpFastOpen: Boolean = false,
    val dropSack: Boolean = false,
    val hostMixedCase: Boolean = false,
    val hostRemoveSpaces: Boolean = false,
    val splitPosition: Int = 1,
    val splitAtHost: Boolean = false,
    val desyncMethod: DesyncMethod = DesyncMethod.OOB,
    val fakeTtl: Int = 8,
    val fakeSni: String = "www.iana.org",
    val domainMixedCase: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DpiBypassUIEditor(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val (cmdArgs, setCmdArgs) = rememberPreference(
        key = DpiBypassCmdArgsKey,
        defaultValue = "-d1 -d3+s -s6+s -d9+s -s12+s -d15+s -s20+s -d25+s -s30+s -d35+s -r1+s -S -a1 -As -d1 -d3+s -s6+s -d9+s -s12+s -d15+s -s20+s -d25+s -s30+s -d35+s -S -a1"
    )

    var settings by remember { mutableStateOf(DpiSettings()) }

    LaunchedEffect(Unit) {
        settings = parseDpiSettings(cmdArgs)
    }

    ColumnWithContentPadding(
        modifier = Modifier.fillMaxHeight(),
        columnModifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        OutlinedTextField(
            value = settings.maxConnections.toString(),
            onValueChange = { newValue ->
                val intValue = newValue.toIntOrNull()
                if (intValue != null && intValue > 0) {
                    settings = settings.copy(maxConnections = intValue)
                }
            },
            label = { Text("Max Connections") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = settings.bufferSize.toString(),
            onValueChange = { newValue ->
                val intValue = newValue.toIntOrNull()
                if (intValue != null && intValue > 0) {
                    settings = settings.copy(bufferSize = intValue)
                }
            },
            label = { Text("Buffer Size") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Desync Method
        PreferenceGroupTitle(title = "Desync Method")

        OutlinedTextField(
            value = settings.desyncMethod.toName(),
            onValueChange = { newValue ->
                try {
                    val method = DesyncMethod.fromName(newValue)
                    settings = settings.copy(desyncMethod = method)
                } catch (e: IllegalArgumentException) {
                }
            },
            label = { Text("Desync Method (e.g., oob, split, fake)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Protocol Settings
        PreferenceGroupTitle(title = "Protocol Settings")

        SwitchPreference(
            title = { Text("Desync HTTP") },
            checked = settings.desyncHttp,
            onCheckedChange = {
                settings = settings.copy(desyncHttp = it)
            }
        )

        SwitchPreference(
            title = { Text("Desync HTTPS") },
            checked = settings.desyncHttps,
            onCheckedChange = {
                settings = settings.copy(desyncHttps = it)
            }
        )

        SwitchPreference(
            title = { Text("Desync UDP") },
            checked = settings.desyncUdp,
            onCheckedChange = {
                settings = settings.copy(desyncUdp = it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced Settings
        PreferenceGroupTitle(title = "Advanced Settings")

        SwitchPreference(
            title = { Text("No Domain") },
            checked = settings.noDomain,
            onCheckedChange = {
                settings = settings.copy(noDomain = it)
            }
        )

        SwitchPreference(
            title = { Text("TCP Fast Open") },
            checked = settings.tcpFastOpen,
            onCheckedChange = {
                settings = settings.copy(tcpFastOpen = it)
            }
        )

        SwitchPreference(
            title = { Text("Drop SACK") },
            checked = settings.dropSack,
            onCheckedChange = {
                settings = settings.copy(dropSack = it)
            }
        )

        SwitchPreference(
            title = { Text("Host Mixed Case") },
            checked = settings.hostMixedCase,
            onCheckedChange = {
                settings = settings.copy(hostMixedCase = it)
            }
        )

        SwitchPreference(
            title = { Text("Host Remove Spaces") },
            checked = settings.hostRemoveSpaces,
            onCheckedChange = {
                settings = settings.copy(hostRemoveSpaces = it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Split Settings
        PreferenceGroupTitle(title = "Split Settings")

        OutlinedTextField(
            value = settings.splitPosition.toString(),
            onValueChange = { newValue ->
                val intValue = newValue.toIntOrNull()
                if (intValue != null && intValue >= 0) {
                    settings = settings.copy(splitPosition = intValue)
                }
            },
            label = { Text("Split Position") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        SwitchPreference(
            title = { Text("Split At Host") },
            checked = settings.splitAtHost,
            onCheckedChange = {
                settings = settings.copy(splitAtHost = it)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fake Settings
        PreferenceGroupTitle(title = "Fake Settings")

        OutlinedTextField(
            value = settings.fakeTtl.toString(),
            onValueChange = { newValue ->
                val intValue = newValue.toIntOrNull()
                if (intValue != null && intValue >= 1 && intValue <= 255) {
                    settings = settings.copy(fakeTtl = intValue)
                }
            },
            label = { Text("Fake TTL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = settings.fakeSni,
            onValueChange = { newValue ->
                settings = settings.copy(fakeSni = newValue)
            },
            label = { Text("Fake SNI") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(96.dp))
    }

    TopAppBar(
        title = { Text("DPI Bypass Configuration") },
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
        actions = {
            IconButton(onClick = {
                val newCmdArgs = generateDpiCmdArgs(settings)
                setCmdArgs(newCmdArgs)
                try {
                    val restartIntent = Intent(context, DpiProxyService::class.java)
                    restartIntent.action = DpiProxyService.ACTION_RESTART
                    context.startService(restartIntent)
                    Toast.makeText(context, "DPI proxy restarted with new configuration", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to restart DPI proxy: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                navController.navigateUp()
            }) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = "Save"
                )
            }
        },
        windowInsets = TopBarInsets,
        scrollBehavior = scrollBehavior
    )
}

private fun generateDpiCmdArgs(settings: DpiSettings): String {
    val args = mutableListOf<String>()

    // Basic connection settings (excluding IP/port, handled separately)
    if (settings.maxConnections != 512) args.add("-c${settings.maxConnections}")
    if (settings.bufferSize != 16384) args.add("-b${settings.bufferSize}")

    // Protocol settings
    val desyncHttp = settings.desyncHttp
    val desyncHttps = settings.desyncHttps
    val protocols = buildList {
        if (desyncHttps) add("t")
        if (desyncHttp) add("h")
    }

    // Assuming no host filtering for simplicity; add -K if protocols enabled
    if (protocols.isNotEmpty()) {
        args.add("-K${protocols.joinToString(",")}")
    }

    // TTL settings (assuming defaultTtl=0)
    if (settings.noDomain) args.add("-N")

    // Desync method
    addDesyncArgs(args, settings)

    // HTTP modifications
    addHttpModificationArgs(args, settings)

    // Other flags
    if (settings.tcpFastOpen) args.add("-F")
    if (settings.dropSack) args.add("-Y")
    args.add("-An")

    // UDP settings
    if (settings.desyncUdp) {
        args.add("-Ku")
        // Assuming default fake count=1, no -a
        args.add("-An")
    }

    return args.joinToString(" ")
}

private fun addDesyncArgs(args: MutableList<String>, settings: DpiSettings) {
    val splitPosition = settings.splitPosition
    if (splitPosition <= 0) return

    val desyncMethod = settings.desyncMethod
    val splitAtHost = settings.splitAtHost

    val posArg = if (splitAtHost) "$splitPosition+h" else splitPosition.toString()
    val option = when (desyncMethod) {
        DesyncMethod.Split -> "-s"
        DesyncMethod.Disorder -> "-d"
        DesyncMethod.OOB -> "-o"
        DesyncMethod.DISOOB -> "-q"
        DesyncMethod.Fake -> "-f"
        DesyncMethod.None -> return
    }
    args.add("$option$posArg")

    // Fake method specific settings
    if (desyncMethod == DesyncMethod.Fake) {
        val fakeTtl = settings.fakeTtl
        val fakeSni = settings.fakeSni
        // Assuming default offset=0
        if (fakeTtl > 0) args.add("-t$fakeTtl")
        if (fakeSni.isNotEmpty()) args.add("-n$fakeSni")
    }

    // OOB method specific settings
    if (desyncMethod in listOf(DesyncMethod.OOB, DesyncMethod.DISOOB)) {
        // Assuming default OOB char 'a' (ASCII 97)
        args.add("-e97")
    }
}

private fun addHttpModificationArgs(args: MutableList<String>, settings: DpiSettings) {
    val hostMixedCase = settings.hostMixedCase
    val domainMixedCase = settings.domainMixedCase
    val hostRemoveSpaces = settings.hostRemoveSpaces

    val modFlags = buildList {
        if (hostMixedCase) add("h")
        if (domainMixedCase) add("d")
        if (hostRemoveSpaces) add("r")
    }
    if (modFlags.isNotEmpty()) {
        args.add("-M${modFlags.joinToString(",")}")
    }
}

private fun parseDpiSettings(cmd: String): DpiSettings {
    val tokens = shellSplit(cmd)
    var currentSettings = DpiSettings()

    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        when {
            token.startsWith("-c") -> {
                val value = token.substring(2).toIntOrNull() ?: 512
                currentSettings = currentSettings.copy(maxConnections = value)
            }
            token.startsWith("-b") -> {
                val value = token.substring(2).toIntOrNull() ?: 16384
                currentSettings = currentSettings.copy(bufferSize = value)
            }
            token == "-Ku" -> {
                currentSettings = currentSettings.copy(desyncUdp = true)
            }
            token.startsWith("-K") -> {
                val protos = token.substring(2).lowercase()
                var http = currentSettings.desyncHttp
                var https = currentSettings.desyncHttps
                if (protos.contains("h")) http = true
                if (protos.contains("t")) https = true
                currentSettings = currentSettings.copy(desyncHttp = http, desyncHttps = https)
            }
            token == "-N" -> currentSettings = currentSettings.copy(noDomain = true)
            token == "-F" -> currentSettings = currentSettings.copy(tcpFastOpen = true)
            token == "-Y" -> currentSettings = currentSettings.copy(dropSack = true)
            token.startsWith("-M") -> {
                val mods = token.substring(2).lowercase()
                var hostMixed = currentSettings.hostMixedCase
                var domainMixed = currentSettings.domainMixedCase
                var hostRemove = currentSettings.hostRemoveSpaces
                if (mods.contains("h")) hostMixed = true
                if (mods.contains("d")) domainMixed = true
                if (mods.contains("r")) hostRemove = true
                currentSettings = currentSettings.copy(
                    hostMixedCase = hostMixed,
                    domainMixedCase = domainMixed,
                    hostRemoveSpaces = hostRemove
                )
            }
            token.startsWith("-s") -> {
                val posStr = token.substring(2)
                val pos = posStr.replace("+h", "").toIntOrNull() ?: 1
                val atHost = posStr.contains("+h")
                currentSettings = currentSettings.copy(
                    desyncMethod = DesyncMethod.Split,
                    splitPosition = pos,
                    splitAtHost = atHost
                )
            }
            token.startsWith("-d") -> {
                val posStr = token.substring(2)
                val pos = posStr.replace("+h", "").toIntOrNull() ?: 1
                val atHost = posStr.contains("+h")
                currentSettings = currentSettings.copy(
                    desyncMethod = DesyncMethod.Disorder,
                    splitPosition = pos,
                    splitAtHost = atHost
                )
            }
            token.startsWith("-o") -> {
                val posStr = token.substring(2)
                val pos = posStr.replace("+h", "").toIntOrNull() ?: 1
                val atHost = posStr.contains("+h")
                currentSettings = currentSettings.copy(
                    desyncMethod = DesyncMethod.OOB,
                    splitPosition = pos,
                    splitAtHost = atHost
                )
            }
            token.startsWith("-q") -> {
                val posStr = token.substring(2)
                val pos = posStr.replace("+h", "").toIntOrNull() ?: 1
                val atHost = posStr.contains("+h")
                currentSettings = currentSettings.copy(
                    desyncMethod = DesyncMethod.DISOOB,
                    splitPosition = pos,
                    splitAtHost = atHost
                )
            }
            token.startsWith("-f") -> {
                val posStr = token.substring(2)
                val pos = posStr.replace("+h", "").toIntOrNull() ?: 1
                val atHost = posStr.contains("+h")
                currentSettings = currentSettings.copy(
                    desyncMethod = DesyncMethod.Fake,
                    splitPosition = pos,
                    splitAtHost = atHost
                )
            }
            token.startsWith("-t") -> {
                val value = token.substring(2).toIntOrNull() ?: 8
                currentSettings = currentSettings.copy(fakeTtl = value)
            }
            token.startsWith("-n") -> {
                currentSettings = currentSettings.copy(fakeSni = token.substring(2))
            }
            // Ignore others (e.g., -An, -e, -g, -r, -H, etc.)
        }
        i++
    }

    return currentSettings
}

private fun shellSplit(cmd: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var escapeNext = false

    for (char in cmd) {
        when {
            escapeNext -> {
                current.append(char)
                escapeNext = false
            }
            char == '\\' -> escapeNext = true
            char == '"' -> inQuotes = !inQuotes
            char == ' ' && !inQuotes -> {
                if (current.isNotEmpty()) {
                    result.add(current.toString())
                    current.clear()
                }
            }
            else -> current.append(char)
        }
    }

    if (current.isNotEmpty()) {
        result.add(current.toString())
    }

    return result.distinct()
}