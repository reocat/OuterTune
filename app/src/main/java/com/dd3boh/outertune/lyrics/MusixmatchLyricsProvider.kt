package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.outertune.constants.MusixmatchLoggedInKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.maxrave.lyricsproviders.LyricsClient
import org.akanework.gramophone.logic.utils.SemanticLyrics
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusixmatchLyricsProvider @Inject constructor(
    private val client: LyricsClient
) : LyricsProvider {
    override val name = "Musixmatch"

    override fun isEnabled(context: Context): Boolean {
        val enabled = context.dataStore.get(MusixmatchLoggedInKey, false)
        Timber.v("Musixmatch provider enabled: $enabled")
        return enabled
    }

    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<SemanticLyrics> = runCatching {
        Timber.d("Hewwo! Trying to get lyrics for '$title' from Musixmatch, nya!")
        val userToken = client.musixmatchUserToken
            ?: throw IllegalStateException("Musixmatch user token not found. Please log in. ;w;")

        Timber.d("Musixmatch token found, proceeding with search. uwu")
        val lyricsResult = client.macroSearch(
            q_track = title,
            q_artist = artist,
            duration = duration,
            userToken = userToken
        ).getOrThrow()

        val semanticLyrics = convertToSemanticLyrics(lyricsResult?.second)
        if (semanticLyrics != null) {
            Timber.d("Yay! Musixmatch found lyrics for '$title'! :3")
        } else {
            Timber.d("Aww, Musixmatch couldn't find lyrics for '$title'.")
        }
        semanticLyrics ?: throw IllegalStateException("Lyrics not found on Musixmatch.")
    }
}