package com.dd3boh.outertune.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Immutable
enum class RecentActivityType {
    PLAYLIST, ALBUM, ARTIST
}

@Entity
@Immutable
data class RecentActivityItem(
    @PrimaryKey val id: String,
    val title: String,
    val thumbnail: String?,
    val explicit: Boolean,
    val shareLink: String,
    val type: RecentActivityType,
    val playlistId: String?,
    val radioPlaylistId: String?,
    val shufflePlaylistId: String?
)
