package com.dd3boh.outertune.models.spotify.liked_songs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikedSong(
    @SerialName("track")
    val likedSongItem: LikedSongItem
)
