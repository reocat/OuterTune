package com.dd3boh.outertune.models.spotify.liked_songs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyLikedSongsPaginatedResponse(
    @SerialName("total")
    val totalCountOfLikedSongs: Int,
    @SerialName("next")
    val nextPaginatedUrl: String? = null,
    @SerialName("items")
    val likedSongs: List<LikedSong>
)
