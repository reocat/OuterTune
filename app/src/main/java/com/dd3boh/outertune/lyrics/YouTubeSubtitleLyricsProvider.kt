package com.dd3boh.outertune.lyrics

import android.content.Context
import com.zionhuang.innertube.YouTube
import org.akanework.gramophone.logic.utils.LrcUtils
import org.akanework.gramophone.logic.utils.SemanticLyrics
import timber.log.Timber

object YouTubeSubtitleLyricsProvider : LyricsProvider {
    override val name = "YouTube Subtitle"
    override fun isEnabled(context: Context) = true
    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<SemanticLyrics> = runCatching {
        Timber.d("Let's see if there are subtitle-based lyrics on YouTube for ID: $id, uwu")
        val rawSrt = YouTube.transcript(id).getOrThrow()
            ?: throw IllegalStateException("Subtitle transcript unavailable")

        Timber.v("Got raw SRT from YouTube, let's parse it! :3\n$rawSrt")
        val parserOptions = LrcUtils.LrcParserOptions(trim = true, multiLine = false, errorText = "Failed to parse SRT")
        LrcUtils.parseLyrics(rawSrt, parserOptions, LrcUtils.LyricFormat.SRT)?.also {
            Timber.d("Successfully parsed SRT subtitles from YouTube! OwO")
        } ?: throw IllegalStateException("Failed to parse SRT subtitles from YouTube")
    }
}