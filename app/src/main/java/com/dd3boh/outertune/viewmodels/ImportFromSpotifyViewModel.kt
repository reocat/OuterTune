package com.dd3boh.outertune.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.models.SpotifyAuthResponse
import com.dd3boh.outertune.models.SpotifyUserProfile
import com.dd3boh.outertune.ui.screens.settings.content.model.ImportFromSpotifyScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportFromSpotifyViewModel @Inject constructor(private val httpClient: HttpClient) :
    ViewModel() {
    val importFromSpotifyScreenState = mutableStateOf(
        ImportFromSpotifyScreenState(
            isRequesting = false, accessToken = "", error = false, exception = null,
            userName = "", isObtainingAccessTokenSuccessful = false
        )
    )

    fun loginWithSpotifyCredentials(
        clientId: String, clientSecret: String, authorizationCode: String
    ) {
        viewModelScope.launch {
            try {
                importFromSpotifyScreenState.value = importFromSpotifyScreenState.value.copy(
                    isRequesting = true,
                    error = false,
                    exception = null,
                    accessToken = "",
                    userName = "",
                    isObtainingAccessTokenSuccessful = false
                )
                getSpotifyAccessTokenDataResponse(
                    clientId = clientId,
                    clientSecret = clientSecret,
                    authorizationCode = authorizationCode
                ).let { response ->
                    if (response.status.isSuccess()) {
                        importFromSpotifyScreenState.value =
                            importFromSpotifyScreenState.value.copy(
                                accessToken = response.body<SpotifyAuthResponse>().accessToken,
                                isRequesting = false,
                                isObtainingAccessTokenSuccessful = true
                            )
                        getUserProfile(importFromSpotifyScreenState.value.accessToken).let {
                            importFromSpotifyScreenState.value =
                                importFromSpotifyScreenState.value.copy(
                                    userName = it.displayName
                                )
                        }
                    } else {
                        throw Exception("Request failed with status code : ${response.status.value}\n${response.bodyAsText()}")
                    }
                }
            } catch (e: Exception) {
                importFromSpotifyScreenState.value = importFromSpotifyScreenState.value.copy(
                    isRequesting = false, error = true, exception = e, accessToken = "",
                    isObtainingAccessTokenSuccessful = false
                )
            }
        }
    }

    private suspend fun getLikedSongs(authToken: String) {
        httpClient.get("https://api.spotify.com/v1/me/tracks") {
            bearerAuth(authToken)
        }.bodyAsText()
    }

    private suspend fun getUserProfile(authToken: String): SpotifyUserProfile {
        return httpClient.get("https://api.spotify.com/v1/me") {
            bearerAuth(authToken)
        }.body<SpotifyUserProfile>()
    }

    private suspend fun getSpotifyAccessTokenDataResponse(
        authorizationCode: String, clientId: String, clientSecret: String
    ): HttpResponse {
        return httpClient.post(urlString = "https://accounts.spotify.com/api/token") {
            basicAuth(clientId, clientSecret)
            setBody(
                FormDataContent(Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", authorizationCode)
                    append("redirect_uri", "http://localhost:45454")
                })
            )
        }
    }
}