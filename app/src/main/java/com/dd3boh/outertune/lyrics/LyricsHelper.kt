package com.dd3boh.outertune.lyrics

import android.content.Context
import android.util.LruCache
import com.dd3boh.outertune.constants.LyricSourcePrefKey
import com.dd3boh.outertune.constants.LyricTrimKey
import com.dd3boh.outertune.constants.MultilineLrcKey
import com.dd3boh.outertune.db.MusicDatabase
import com.dd3boh.outertune.db.entities.LyricsEntity
import com.dd3boh.outertune.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.dd3boh.outertune.models.LyricsData
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.get
import com.dd3boh.outertune.utils.reportException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.akanework.gramophone.logic.utils.LrcUtils
import org.akanework.gramophone.logic.utils.SemanticLyrics
import org.akanework.gramophone.logic.utils.parseLrc
import timber.log.Timber
import javax.inject.Inject

// Helper function to format ULong milliseconds to [mm:ss.xx]
private fun formatTimestamp(millis: ULong): String {
    if (millis == 0uL) return "[00:00.00]"
    val totalSeconds = millis / 1000u
    val minutes = totalSeconds / 60u
    val seconds = totalSeconds % 60u
    val hundredths = (millis % 1000u) / 10u
    return "[%02d:%02d.%02d]".format(minutes.toInt(), seconds.toInt(), hundredths.toInt())
}

class LyricsHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    val database: MusicDatabase,
    private val musixmatch: MusixmatchLyricsProvider,
    private val lrcLib: LrcLibLyricsProvider,
    private val kuGou: KuGouLyricsProvider,
    private val youTube: YouTubeLyricsProvider,
    private val youTubeSubtitle: YouTubeSubtitleLyricsProvider,
    private val local: LocalLyricsProvider,
) {
    private val lyricsProviders by lazy {
        listOf(musixmatch, lrcLib, youTube, youTubeSubtitle, kuGou)
    }
    private val cache = LruCache<String, LyricsData>(MAX_CACHE_SIZE)

    suspend fun getLyrics(mediaMetadata: MediaMetadata): LyricsData? {
        Timber.d("nya~ Starting lyrics search for '${mediaMetadata.title}' (ID: ${mediaMetadata.id})")
        val trim = context.dataStore.get(LyricTrimKey, defaultValue = false)
        val multiline = context.dataStore.get(MultilineLrcKey, defaultValue = true)
        val prefLocal = context.dataStore.get(LyricSourcePrefKey, true)
        Timber.v("Parser options: trim=$trim, multiline=$multiline. Prefers local lyrics: $prefLocal")

        val cached = cache.get(mediaMetadata.id)
        if (cached != null) {
            Timber.i("OwO! Found lyrics in cache for '${mediaMetadata.title}'!")
            return cached
        }
        Timber.d("No lyrics in cache for '${mediaMetadata.title}', checking database...")

        val dbLyricsStr = database.lyrics(mediaMetadata.id).let { it.first()?.lyrics }
        val dbLyrics = dbLyricsStr?.let {
            if (it == LYRICS_NOT_FOUND) {
                Timber.d("Database says lyrics were previously not found for this track. ono")
                null
            } else {
                Timber.d("Found lyrics in database! Parsing them now, uwu.")
                parseLrc(it, trim, multiline)
            }
        }

        if (dbLyrics != null && !prefLocal) {
            Timber.i("Found and returning database lyrics for '${mediaMetadata.title}' as user doesn't prefer local first. ^w^")
            val data = LyricsData(dbLyrics, "Database")
            cache.put(mediaMetadata.id, data)
            return data
        }

        val localLyrics = getLocalLyrics(mediaMetadata, LrcUtils.LrcParserOptions(trim, multiline, "Unable to parse lyrics"))

        if (prefLocal) {
            Timber.d("User prefers local lyrics, so we check them first!")
            if (localLyrics != null) {
                Timber.i("Found local lyrics for '${mediaMetadata.title}'. Returning these! :3")
                val data = LyricsData(localLyrics, "Local")
                cache.put(mediaMetadata.id, data)
                return data
            }
            if (dbLyrics != null) {
                Timber.i("No local lyrics, but found some in the database! Returning those, nya~")
                val data = LyricsData(dbLyrics, "Database")
                cache.put(mediaMetadata.id, data)
                return data
            }
        }

        Timber.i("No local or database lyrics found (or not preferred yet). Let's ask the remote providers! OwO")
        val remoteLyricsPair = getRemoteLyrics(mediaMetadata)
        if (remoteLyricsPair != null) {
            Timber.i("Got remote lyrics! Saving to cache and database. uwu")
            cache.put(mediaMetadata.id, remoteLyricsPair)
            val remoteLyrics = remoteLyricsPair.lyrics
            val lrcString = when (remoteLyrics) {
                is SemanticLyrics.SyncedLyrics -> {
                    Timber.d("Lyrics are synced! Formatting to LRC string.")
                    remoteLyrics.text.joinToString("\n") { line -> "${formatTimestamp(line.start)}${line.text}" }
                }
                is SemanticLyrics.UnsyncedLyrics -> {
                    Timber.d("Lyrics are unsynced. Saving as plain text.")
                    remoteLyrics.unsyncedText.joinToString("\n") { line -> line.first }
                }
            }
            Timber.v("Saving lyrics to DB:\n$lrcString")
            database.query {
                upsert(LyricsEntity(id = mediaMetadata.id, lyrics = lrcString))
            }
            return remoteLyricsPair
        }

        if (!prefLocal) {
            if (localLyrics != null) {
                Timber.i("No remote lyrics found, but we found local lyrics earlier! Returning those. :3")
                val data = LyricsData(localLyrics, "Local")
                cache.put(mediaMetadata.id, data)
                return data
            }
        }

        Timber.w("Aww, no lyrics found anywhere for '${mediaMetadata.title}'. Saving 'not found' to db. ;w;")
        database.query {
            upsert(LyricsEntity(id = mediaMetadata.id, lyrics = LYRICS_NOT_FOUND))
        }

        return LyricsData(
            lyrics = LyricsEntity.uninitializedLyric,
            providerName = "None"
        )
    }

    private suspend fun getRemoteLyrics(mediaMetadata: MediaMetadata): LyricsData? {
        Timber.d("Iterating through ${lyricsProviders.size} remote providers...")
        for (provider in lyricsProviders) {
            if (provider.isEnabled(context)) {
                Timber.i("Trying provider: ${provider.name}, nya~")
                provider.getLyrics(
                    mediaMetadata.id,
                    mediaMetadata.title,
                    mediaMetadata.artists.joinToString { it.name },
                    mediaMetadata.duration
                ).onSuccess { lyrics ->
                    Timber.d("Success! Found lyrics for '${mediaMetadata.title}' with ${provider.name}. uwu")
                    Timber.v("Retrieved lyrics snippet: ${lyrics.unsyncedText.firstOrNull()?.first ?: "..."}")
                    return LyricsData(lyrics, provider.name)
                }.onFailure {
                    Timber.w("Provider ${provider.name} failed or found no lyrics. ono. Reason: ${it.message}")
                    reportException(it)
                }
            } else {
                Timber.d("Skipping disabled provider: ${provider.name}")
            }
        }
        return null
    }

    private fun getLocalLyrics(mediaMetadata: MediaMetadata, parserOptions: LrcUtils.LrcParserOptions): SemanticLyrics? {
        if (local.isEnabled(context) && mediaMetadata.localPath != null) {
            Timber.d("Attempting to find local lyrics for path: ${mediaMetadata.localPath}")
            return local.getLyricsNew(mediaMetadata.localPath, parserOptions)?.also {
                Timber.i("Found and parsed local lyrics file! Yay! ^w^")
            }
        }
        return null
    }

    /**
     * Searches all enabled providers and calls back with each result.
     * This is designed for the manual search UI.
     */
    suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        callback: (LyricsResult) -> Unit
    ) {
        Timber.d("Starting manual search for '$title'...")
        for (provider in lyricsProviders) {
            if (provider.isEnabled(context)) {
                Timber.i("Manual search: Querying ${provider.name}...")
                provider.getLyrics(id, title, artist, duration).onSuccess { semanticLyrics ->
                    val lyricText = semanticLyrics.unsyncedText.joinToString("\n") { it.first }.trim()
                    if (lyricText.isNotEmpty()) {
                        Timber.d("Manual search: Found lyrics from ${provider.name}, sending to UI. :3")
                        val result = LyricsResult(
                            providerName = provider.name,
                            lyrics = lyricText,
                            isSynced = semanticLyrics is SemanticLyrics.SyncedLyrics
                        )
                        callback(result)
                    } else {
                        Timber.d("Manual search: ${provider.name} returned empty lyrics.")
                    }
                }.onFailure {
                    Timber.w("Manual search: Provider ${provider.name} failed. Reason: ${it.message}")
                    reportException(it)
                }
            }
        }
    }

    companion object {
        private const val MAX_CACHE_SIZE = 5
    }
}