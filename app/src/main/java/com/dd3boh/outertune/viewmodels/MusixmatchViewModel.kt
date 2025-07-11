package com.dd3boh.outertune.viewmodels

import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.constants.MusixmatchCookieKey
import com.dd3boh.outertune.constants.MusixmatchLoggedInKey
import com.dd3boh.outertune.constants.MusixmatchUserTokenKey
import com.dd3boh.outertune.utils.dataStore
import com.maxrave.lyricsproviders.LyricsClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusixmatchViewModel @Inject constructor(
    private val client: LyricsClient,
    app: android.app.Application
) : ViewModel() {
    private val dataStore = app.dataStore

    fun login(userId: String, userToken: String, deviceId: String) {
        viewModelScope.launch {
            val cookie = "x-mxm-user-id=${userId.replace(":", "%3A")}; path=%2F; x-mxm-token-guid=$deviceId; mxm-encrypted-token=; AWSELB=unknown"
            dataStore.edit { settings ->
                settings[MusixmatchUserTokenKey] = userToken
                settings[MusixmatchCookieKey] = cookie
                settings[MusixmatchLoggedInKey] = true
            }
            client.musixmatchUserToken = userToken
            client.musixmatchCookie = cookie
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStore.edit { settings ->
                settings.remove(MusixmatchUserTokenKey)
                settings.remove(MusixmatchCookieKey)
                settings[MusixmatchLoggedInKey] = false
            }
            client.musixmatchUserToken = null
            client.musixmatchCookie = null
        }
    }
}