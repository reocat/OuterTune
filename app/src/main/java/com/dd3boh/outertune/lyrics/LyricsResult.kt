package com.dd3boh.outertune.lyrics

/**
 * A simple data class to hold the results from a lyrics provider search.
 * This is used to pass information from the data layer to the UI.
 *
 * @param providerName The name of the service that found the lyrics (e.g., "Musixmatch").
 * @param lyrics A string preview or the full text of the lyrics found.
 * @param isSynced A boolean indicating if the lyrics contain timestamps.
 */
data class LyricsResult(
    val providerName: String,
    val lyrics: String,
    val isSynced: Boolean
)