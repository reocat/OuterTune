package com.dd3boh.outertune.lyrics

import android.content.Context
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.WatchEndpoint
import org.akanework.gramophone.logic.utils.SemanticLyrics
import org.akanework.gramophone.logic.utils.parseLrc
import timber.log.Timber

object YouTubeLyricsProvider : LyricsProvider {
    override val name = "YouTube Music"
    override fun isEnabled(context: Context) = true
    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<SemanticLyrics> = runCatching {
        Timber.d("Trying to get official lyrics from YouTube Music for ID: $id, OwO")
        val nextResult = YouTube.next(WatchEndpoint(videoId = id)).getOrThrow()

        if (nextResult.lyricsEndpoint == null) {
            Timber.w("No lyrics endpoint found on YouTube Music for '$title'. ;w;")
            throw IllegalStateException("Lyrics endpoint not found")
        }

        Timber.d("Lyrics endpoint found! Fetching lyrics now, nya~")
        val rawLyrics = YouTube.lyrics(
            endpoint = nextResult.lyricsEndpoint!!
        ).getOrThrow() ?: throw IllegalStateException("Lyrics unavailable")
        Timber.v("Got raw lyrics from YouTube Music, attempting to parse them:\n$rawLyrics")

        parseLrc(rawLyrics, trimEnabled = false, multiLineEnabled = true)?.also {
            Timber.d("Successfully parsed lyrics from YouTube Music! ^w^")
        } ?: throw IllegalStateException("Failed to parse lyrics from YouTube Music")
    }
}