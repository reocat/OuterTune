package com.dd3boh.outertune.models.spotify.liked_songs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikedSongItem(
    val type: String,
    @SerialName("id")
    val trackId: String,
    @SerialName("name")
    val trackName: String,
    @SerialName("is_local")
    val isLocal: Boolean,
    val artists:List<Artist>
)
