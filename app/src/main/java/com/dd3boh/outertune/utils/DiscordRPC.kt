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

        // Debug logging
        println("=== Discord RPC Song Update ===")
        println("Song: ${song.song.title}")
        println("Song Thumbnail URL: ${song.song.thumbnailUrl}")
        println("Artist: ${song.artists.firstOrNull()?.name}")
        println("Artist Thumbnail URL: ${song.artists.firstOrNull()?.thumbnailUrl}")
        println("===============================")

        // Determine the best image to use
        val largeImage = when {
            !song.song.thumbnailUrl.isNullOrBlank() -> RpcImage.ExternalImage(song.song.thumbnailUrl)
            else -> RpcImage.FallbackImage
        }
        
        val smallImage = song.artists.firstOrNull()?.thumbnailUrl?.let { url ->
            if (url.isNotBlank()) RpcImage.ExternalImage(url) else null
        }

        setActivity(
            name = context.getString(R.string.app_name).removeSuffix(" Debug"),
            details = song.song.title,
            detailsUrl = "https://music.youtube.com/watch?v=${song.song.id}",
            state = song.artists.joinToString { it.name },
            largeImage = largeImage,
            smallImage = smallImage,
            largeText = song.album?.title,
            smallText = song.artists.firstOrNull()?.name,
            buttons = listOf(
                context.getString(R.string.rpc_listen_ytm) to
                        "https://music.youtube.com/watch?v=${song.song.id}",
                context.getString(R.string.rpc_visit, context.getString(R.string.app_name)) to
                        "https://github.com/OuterTune/OuterTune"
            ),
            type = Type.LISTENING,
            statusDisplayType = KizzyRPC.StatusDisplayType.STATE,
            since = currentTime,
            startTime = calculatedStartTime,
            endTime = currentTime + (song.song.duration * 1000L - currentPlaybackTimeMillis),
            applicationId = APPLICATION_ID
        )
    }
    companion object {
        private const val APPLICATION_ID = "1271273225120125040"
    }
}