package com.dd3boh.outertune.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.models.SpotifyAuthResponse
import com.dd3boh.outertune.models.SpotifyUserProfile
import com.dd3boh.outertune.models.spotify.playlists.SpotifyPlaylistPaginatedResponse
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
            isRequesting = false,
            accessToken = "",
            error = false,
            exception = null,
            userName = "",
            isObtainingAccessTokenSuccessful = false,
            playlists = emptyList(),
            totalPlaylistsCount = 0,
            reachedEndForPlaylistPagination = false
        )
    )
    val selectedPlaylists = mutableStateListOf<String>()
    val isLikedSongsSelectedForImport = mutableStateOf(false)

    fun spotifyLoginAndFetchPlaylists(
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
                    isObtainingAccessTokenSuccessful = false,
                    playlists = emptyList()
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

                        Timber.tag("Outertune Log")
                            .d(importFromSpotifyScreenState.value.accessToken)

                        getUserProfile(importFromSpotifyScreenState.value.accessToken).let {
                            importFromSpotifyScreenState.value =
                                importFromSpotifyScreenState.value.copy(
                                    userName = it.displayName
                                )
                        }

                        getPlaylists(importFromSpotifyScreenState.value.accessToken).let {
                            importFromSpotifyScreenState.value =
                                importFromSpotifyScreenState.value.copy(
                                    playlists = importFromSpotifyScreenState.value.playlists + it.items,
                                    totalPlaylistsCount = importFromSpotifyScreenState.value.totalPlaylistsCount,
                                    reachedEndForPlaylistPagination = it.nextUrl == null
                                )
                        }

                    } else {
                        throw Exception("Request failed with status code : ${response.status.value}\n${response.bodyAsText()}")
                    }
                }
            } catch (e: Exception) {
                importFromSpotifyScreenState.value = importFromSpotifyScreenState.value.copy(
                    isRequesting = false,
                    error = true,
                    exception = e,
                    accessToken = "",
                    isObtainingAccessTokenSuccessful = false
                )
            }
        }
    }

    private var paginatedResultsLimit = 50
    private var playListPaginationOffset = -paginatedResultsLimit

    fun retrieveNextPageOfPlaylists() {
        viewModelScope.launch {
            importFromSpotifyScreenState.value =
                importFromSpotifyScreenState.value.copy(isRequesting = true)
            getPlaylists(importFromSpotifyScreenState.value.accessToken).let {
                importFromSpotifyScreenState.value = importFromSpotifyScreenState.value.copy(
                    playlists = importFromSpotifyScreenState.value.playlists + it.items,
                    totalPlaylistsCount = it.totalResults ?: 0,
                    reachedEndForPlaylistPagination = it.nextUrl == null,
                    isRequesting = false
                )
            }
        }
    }

    private suspend fun getPlaylists(
        authToken: String
    ): SpotifyPlaylistPaginatedResponse {
        playListPaginationOffset += paginatedResultsLimit
        return httpClient.get("https://api.spotify.com/v1/me/playlists?offset=${playListPaginationOffset}&limit=$paginatedResultsLimit") {
            bearerAuth(authToken)
        }.body<SpotifyPlaylistPaginatedResponse>()
    }

    private suspend fun getLikedSongs(authToken: String) {
        httpClient.get("https://api.spotify.com/v1/me/tracks?offset=0&limit=50") {
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