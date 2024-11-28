package com.dd3boh.outertune.ui.screens.settings.content.model

data class ImportFromSpotifyScreenState(
    val isRequesting: Boolean,
    val accessToken: String,
    val error: Boolean,
    val exception: Exception?
)
