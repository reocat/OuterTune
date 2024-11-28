package com.dd3boh.outertune.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.models.SpotifyAuthResponse
import com.dd3boh.outertune.ui.screens.settings.content.model.ImportFromSpotifyScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.headers
import io.ktor.http.isSuccess
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportFromSpotifyViewModel @Inject constructor(private val httpClient: HttpClient) :
    ViewModel() {
    val importFromSpotifyScreenState = mutableStateOf(
        ImportFromSpotifyScreenState(
            isRequesting = false, accessToken = "", error = false, exception = null
        )
    )

    fun loginWithSpotifyCredentials(clientId: String, clientSecret: String) {
        viewModelScope.launch {
            try {
                importFromSpotifyScreenState.value = importFromSpotifyScreenState.value.copy(
                    isRequesting = true, error = false, exception = null, accessToken = ""
                )
                getSpotifyAccessTokenDataResponse(clientId, clientSecret).let { response ->
                    if (response.status.isSuccess()) {
                        importFromSpotifyScreenState.value =
                            importFromSpotifyScreenState.value.copy(
                                accessToken = response.body<SpotifyAuthResponse>().accessToken,
                                isRequesting = false
                            )
                    } else {
                        throw Exception("Request failed with status code : ${response.status.value}\n${response.bodyAsText()}")
                    }
                }
            } catch (e: Exception) {
                importFromSpotifyScreenState.value = importFromSpotifyScreenState.value.copy(
                    isRequesting = false, error = true, exception = e, accessToken = ""
                )
            }
        }
    }

    private suspend fun getSpotifyAccessTokenDataResponse(
        clientId: String, clientSecret: String
    ): HttpResponse {
        return httpClient.post(urlString = "https://accounts.spotify.com/api/token") {
            headers {
                append(
                    HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString()
                )
            }
            setBody(
                FormDataContent(Parameters.build {
                    append("grant_type", "client_credentials")
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                })
            )
        }
    }
}