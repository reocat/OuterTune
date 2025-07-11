package com.dd3boh.outertune.lyrics

import android.content.Context
import org.akanework.gramophone.logic.utils.SemanticLyrics

interface LyricsProvider {
    val name: String
    fun isEnabled(context: Context): Boolean

    /**
     * Get lyrics for a song.
     * @return A Result containing the parsed SemanticLyrics.
     */
    suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<SemanticLyrics>

    /**
     * Get all possible lyrics versions from a provider.
     * The default implementation just calls getLyrics.
     */
    suspend fun getAllLyrics(id: String, title: String, artist: String, duration: Int, callback: (String) -> Unit) {
        getLyrics(id, title, artist, duration).onSuccess {
            callback(it.unsyncedText.joinToString("\n") { pair -> pair.first })
        }
    }
}