package com.dd3boh.outertune.utils
import android.content.Context
import com.dd3boh.outertune.R
import com.dd3boh.outertune.db.entities.Song
import com.my.kizzy.rpc.KizzyRPC
import com.my.kizzy.rpc.RpcImage

class DiscordRPC(
    val context: Context,
    token: String,
) : KizzyRPC(token) {
    suspend fun updateSong(song: Song, currentPlaybackTimeMillis: Long = 0L) = runCatching {
        val currentTime = System.currentTimeMillis()
        val calculatedStartTime = currentTime - currentPlaybackTimeMillis

        setActivity(
            name = context.getString(R.string.app_name).removeSuffix(" Debug"),
            details = song.song.title,
            detailsUrl = "https://music.youtube.com/watch?v=${song.song.id}",
            state = song.artists.joinToString { it.name },
            largeImage = song.song.thumbnailUrl?.let { RpcImage.ExternalImage(it) },
            smallImage = song.artists.firstOrNull()?.thumbnailUrl?.let { RpcImage.ExternalImage(it) },
            largeText = song.album?.title,
            smallText = song.artists.firstOrNull()?.name,
            buttons = listOf(
                context.getString(R.string.rpc_listen_ytm) to
                        "https://music.youtube.com/watch?v=${song.song.id}",
                context.getString(R.string.rpc_visit, context.getString(R.string.app_name)) to
                        "https://github.com/OuterTune/OuterTune"
            ),
            type = Type.LISTENING,
            statusDisplayType = StatusDisplayType.STATE,
            since = currentTime,
            startTime = calculatedStartTime,
            endTime = currentTime + (song.song.duration * 1000L - currentPlaybackTimeMillis),
            applicationId = APPLICATION_ID
        )
    }
    companion object {
        private const val APPLICATION_ID = "1411019391843172514"
    }
}