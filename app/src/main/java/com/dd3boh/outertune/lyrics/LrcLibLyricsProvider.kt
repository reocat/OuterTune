package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.outertune.constants.EnableLrcLibKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.maxrave.lyricsproviders.LyricsClient
import org.akanework.gramophone.logic.utils.SemanticLyrics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LrcLibLyricsProvider @Inject constructor(
    private val client: LyricsClient
) : LyricsProvider {
    override val name = "LrcLib"

    override fun isEnabled(context: Context): Boolean {
        val enabled = context.dataStore[EnableLrcLibKey] ?: true
        Timber.v("LrcLib provider enabled: $enabled")
        return enabled
    }

    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<SemanticLyrics> = runCatching {
        Timber.d("Hiii! Let's see if LrcLib has lyrics for '$title', uwu~")
        val lyricsResult = client.getLrclibLyrics(
            q_track = title,
            q_artist = artist,
            duration = duration
        ).getOrThrow()

        val semanticLyrics = convertToSemanticLyrics(lyricsResult)
        if (semanticLyrics != null) {
            Timber.d("LrcLib found lyrics for '$title'! Yay! ^w^")
        } else {
            Timber.d("LrcLib didn't find lyrics for '$title'. ono")
        }
        semanticLyrics ?: throw IllegalStateException("Lyrics not found on LrcLib.")
    }
}