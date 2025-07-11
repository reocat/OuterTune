package com.dd3boh.outertune.lyrics

import android.content.Context
import com.dd3boh.outertune.constants.EnableKugouKey
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.zionhuang.kugou.KuGou
import org.akanework.gramophone.logic.utils.SemanticLyrics
import org.akanework.gramophone.logic.utils.parseLrc
import timber.log.Timber

object KuGouLyricsProvider : LyricsProvider {
    override val name = "Kugou"
    override fun isEnabled(context: Context): Boolean {
        val enabled = context.dataStore[EnableKugouKey] ?: false
        Timber.v("Kugou provider enabled: $enabled")
        return enabled
    }

    override suspend fun getLyrics(id: String, title: String, artist: String, duration: Int): Result<SemanticLyrics> = runCatching {
        Timber.d("Searching for '$title' on Kugou, nya!")
        val rawLyrics = KuGou.getLyrics(title, artist, duration).getOrThrow()
            ?: throw IllegalStateException("Lyrics unavailable on KuGou")

        Timber.v("Got raw lyrics from Kugou, now parsing... uwu\n$rawLyrics")
        val parsed = parseLrc(rawLyrics, trimEnabled = false, multiLineEnabled = true)
        if (parsed != null) {
            Timber.d("Successfully parsed lyrics from Kugou! :3")
        }
        parsed ?: throw IllegalStateException("Failed to parse lyrics from KuGou")
    }

    override suspend fun getAllLyrics(id: String, title: String, artist: String, duration: Int, callback: (String) -> Unit) {
        Timber.d("Getting all possible Kugou lyrics for '$title'...")
        KuGou.getAllPossibleLyricsOptions(title, artist, duration) { lyrics ->
            Timber.v("Found a version from Kugou, calling back!")
            callback(lyrics)
        }
    }
}