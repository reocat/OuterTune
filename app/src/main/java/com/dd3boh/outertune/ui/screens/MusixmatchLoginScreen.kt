package com.dd3boh.outertune.ui.screens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.dd3boh.outertune.constants.MusixmatchLoggedInKey
import com.dd3boh.outertune.utils.rememberPreference
import com.dd3boh.outertune.viewmodels.MusixmatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusixmatchLoginScreen(
    navController: NavController,
    viewModel: MusixmatchViewModel = hiltViewModel(),
) {
    var debugInfo by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var userToken by remember { mutableStateOf("") }
    var deviceId by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val (loggedIn, onLoggedInChange) = rememberPreference(MusixmatchLoggedInKey, false)
    LaunchedEffect(debugInfo) {
        if (debugInfo.isNotEmpty()) {
            val userIdRegex = """\[UserId\]:\s*([a-zA-Z0-9]+:[a-f0-9]+)""".toRegex()
            userId = userIdRegex.find(debugInfo)?.groupValues?.getOrNull(1) ?: ""

            val userTokenRegex = """\[UserToken\]:\s*([a-f0-9]+)""".toRegex()
            userToken = userTokenRegex.find(debugInfo)?.groupValues?.getOrNull(1) ?: ""

            val deviceIdRegex = """\[DeviceId\]:\s*([a-f0-9]+)""".toRegex()
            deviceId = deviceIdRegex.find(debugInfo)?.groupValues?.getOrNull(1) ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log in to Musixmatch") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (loggedIn) {
                Text(
                    "You are logged in to Musixmatch.",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.logout()
                        navController.popBackStack()
                    }
                ) {
                    Text("Log Out")
                }
            } else {
                Text("Follow these instructions:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "1. In the Musixmatch app, go to Settings > Get Help.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("2. Tap on 'Copy debug info'.", style = MaterialTheme.typography.bodyMedium)
                Text("3. Paste the copied info here.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = debugInfo,
                    onValueChange = { debugInfo = it },
                    label = { Text("Paste Debug Info Here") },
                    maxLines = 4,
                    isError = debugInfo.isNotEmpty() && (userId.isEmpty() || userToken.isEmpty() || deviceId.isEmpty())
                )
                Spacer(Modifier.height(16.dp))

                if (userId.isNotEmpty() && userToken.isNotEmpty() && deviceId.isNotEmpty()) {
                    Text("User ID: $userId", style = MaterialTheme.typography.labelSmall)
                    Text("User Token: $userToken", style = MaterialTheme.typography.labelSmall)
                    Text("Device ID: $deviceId", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(16.dp))
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = userId.isNotEmpty() && userToken.isNotEmpty() && deviceId.isNotEmpty(),
                    onClick = {
                        viewModel.login(userId, userToken, deviceId)
                        navController.popBackStack()
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}