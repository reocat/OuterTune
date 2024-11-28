package com.dd3boh.outertune.ui.screens.settings.content

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dd3boh.outertune.viewmodels.ImportFromSpotifyViewModel

@Composable
fun ImportFromSpotifyScreen(navController: NavController) {
    val importFromSpotifyViewModel: ImportFromSpotifyViewModel = hiltViewModel()
    val importFromSpotifyScreenState = importFromSpotifyViewModel.importFromSpotifyScreenState
    val spotifyClientId = rememberSaveable {
        mutableStateOf("")
    }
    val spotifyClientSecret = rememberSaveable {
        mutableStateOf("")
    }
    val spotifyAuthorizationCode = rememberSaveable {
        mutableStateOf("")
    }
    val textFieldPaddingValues = remember {
        PaddingValues(start = 15.dp, end = 15.dp, top = 7.5.dp, bottom = 7.5.dp)
    }
    val localClipBoardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val localUriHandler = LocalUriHandler.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = if (importFromSpotifyScreenState.value.isObtainingAccessTokenSuccessful) Alignment.TopCenter else Alignment.BottomCenter
    ) {
        if (importFromSpotifyScreenState.value.accessToken.isNotBlank() && importFromSpotifyScreenState.value.isObtainingAccessTokenSuccessful) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))
                Text(
                    text = "Logged in as ${importFromSpotifyScreenState.value.userName}. Now, select the items you want to import:",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 15.dp, bottom = 7.5.dp)
                )
            }
            return
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .animateContentSize()
        ) {
            if (importFromSpotifyScreenState.value.error) {
                val isStackTraceVisible = rememberSaveable {
                    mutableStateOf(false)
                }
                Text(
                    text = importFromSpotifyScreenState.value.exception?.message
                        ?: "Well, that’s embarrassing. We have no clue what happened either.",
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                    color = MaterialTheme.colorScheme.error
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isStackTraceVisible.value = isStackTraceVisible.value.not()
                        }
                        .padding(start = 15.dp, end = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Stacktrace", fontWeight = FontWeight.Bold)
                        importFromSpotifyScreenState.value.exception?.stackTrace?.joinToString()
                            ?.let {
                                IconButton(onClick = {
                                    localClipBoardManager.setText(AnnotatedString(text = it))
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.ContentCopy,
                                        contentDescription = null
                                    )
                                }
                            }
                    }
                    IconButton(onClick = {
                        isStackTraceVisible.value = isStackTraceVisible.value.not()
                    }) {
                        Icon(
                            imageVector = if (isStackTraceVisible.value) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = null
                        )
                    }
                }
                if (isStackTraceVisible.value) {
                    Text(
                        text = importFromSpotifyScreenState.value.exception?.stackTrace?.joinToString()
                            ?: "Something went wrong. We’re just as confused as you are.",
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp)
                    )
                }
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                )
            }
            Text(
                text = "Login with Spotify API Credentials",
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 15.dp, bottom = 7.5.dp)
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(textFieldPaddingValues),
                value = spotifyClientId.value,
                onValueChange = {
                    spotifyClientId.value = it
                },
                label = {
                    Text(text = "Client ID")
                },
                readOnly = importFromSpotifyScreenState.value.isRequesting
            )
            Button(
                onClick = {
                    localUriHandler.openUri("https://accounts.spotify.com/authorize?client_id=${spotifyClientId.value}&response_type=code&redirect_uri=http://localhost:45454&scope=user-library-read")
                }, modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp)
            ) {
                Text(text = "Authorize")
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 15.dp, top = 7.5.dp, bottom = 7.5.dp)
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(textFieldPaddingValues),
                value = spotifyClientSecret.value,
                onValueChange = {
                    spotifyClientSecret.value = it
                },
                label = {
                    Text(text = "Client secret")
                },
                readOnly = importFromSpotifyScreenState.value.isRequesting
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(textFieldPaddingValues),
                value = spotifyAuthorizationCode.value,
                onValueChange = {
                    spotifyAuthorizationCode.value = it
                },
                label = {
                    Text(text = "Authorization Code")
                },
                readOnly = importFromSpotifyScreenState.value.isRequesting
            )
            if (importFromSpotifyScreenState.value.isRequesting.not()) {
                Button(
                    onClick = {
                        importFromSpotifyViewModel.loginWithSpotifyCredentials(
                            clientId = spotifyClientId.value.trim(),
                            clientSecret = spotifyClientSecret.value.trim(),
                            authorizationCode = spotifyAuthorizationCode.value.trim()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, bottom = 15.dp, end = 15.dp)
                ) {
                    Text(text = "Authenticate")
                }
            } else {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, bottom = 30.dp, end = 15.dp, top = 7.5.dp)
                )
            }
        }
    }
}