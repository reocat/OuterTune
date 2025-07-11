package com.dd3boh.outertune.lyrics

import android.content.Context
import org.akanework.gramophone.logic.utils.LrcUtils
import org.akanework.gramophone.logic.utils.LrcUtils.loadAndParseLyricsFile
import org.akanework.gramophone.logic.utils.SemanticLyrics
import timber.log.Timber
import java.io.File

object LocalLyricsProvider : LyricsProvider {
    override val name = "Local LRC"
    override fun isEnabled(context: Context) = true

    /**
     * This function is adapted to the interface design.
     * The `title` parameter is assumed to be the file path to the song.
     * The lrc file should be in the same directory.
     */
    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<SemanticLyrics> = runCatching {
        Timber.d("Searching for local lyrics using song file path: $title")
        val parserOptions = LrcUtils.LrcParserOptions(trim = true, multiLine = true, errorText = "Failed to parse local lyrics")
        val songFile = File(title)
        loadAndParseLyricsFile(songFile, parserOptions)
            ?: throw IllegalStateException("Local lyrics not found or failed to parse for: $title")
    }

    /**
     * This is the preferred method for getting local lyrics, called by LyricsHelper.
     */
    fun getLyricsNew(
        path: String,
        parserOptions: LrcUtils.LrcParserOptions
    ): SemanticLyrics? {
        Timber.d("Looking for .lrc file next to: $path")
        val result = loadAndParseLyricsFile(File(path), parserOptions)
        if (result != null) {
            Timber.d("Found a local .lrc file! OwO")
        } else {
            Timber.d("No local .lrc file found at that path. ;w;")
        }
        return result
    }
}