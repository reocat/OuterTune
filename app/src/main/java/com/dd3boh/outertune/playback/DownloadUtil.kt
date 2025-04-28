package com.dd3boh.outertune.playback

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheSpan
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import com.dd3boh.outertune.constants.AudioQuality
import com.dd3boh.outertune.constants.AudioQualityKey
import com.dd3boh.outertune.constants.DownloadPathKey
import com.dd3boh.outertune.constants.LikedAutodownloadMode
import com.dd3boh.outertune.constants.allowedPath
import com.dd3boh.outertune.constants.defaultDownloadPath
import com.dd3boh.outertune.db.MusicDatabase
import com.dd3boh.outertune.db.entities.FormatEntity
import com.dd3boh.outertune.db.entities.PlaylistSong
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.db.entities.SongEntity
import com.dd3boh.outertune.di.AppModule
import com.dd3boh.outertune.di.DownloadCache
import com.dd3boh.outertune.extensions.getLikeAutoDownload
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.playback.downloadManager.DownloadDirectoryManagerOt
import com.dd3boh.outertune.playback.downloadManager.DownloadEvent
import com.dd3boh.outertune.playback.downloadManager.DownloadManagerOt
import com.dd3boh.outertune.utils.YTPlayerUtils
import com.dd3boh.outertune.utils.dataStore
import com.dd3boh.outertune.utils.enumPreference
import com.dd3boh.outertune.utils.get
import com.dd3boh.outertune.utils.reportException
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.SongItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    val database: MusicDatabase,
    val databaseProvider: DatabaseProvider,
    @DownloadCache val downloadCache: SimpleCache,
    @AppModule.PlayerCache val playerCache: SimpleCache,
) {
    val TAG = DownloadUtil::class.simpleName.toString()
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    private val audioQuality by enumPreference(context, AudioQualityKey, AudioQuality.AUTO)
    private val songUrlCache = HashMap<String, Pair<String, Long>>()

    var localMgr = DownloadDirectoryManagerOt(
        context,
        File(allowedPath + "/" + context.dataStore.get(DownloadPathKey, defaultDownloadPath) + "/")
    )
    val downloadMgr = DownloadManagerOt(localMgr)
    val downloads = MutableStateFlow<Map<String, LocalDateTime>>(emptyMap())

    var isProcessingDownloads = MutableStateFlow(false)


    fun getDownload(songId: String): Flow<Song?> {
        return database.song(songId)
    }

    fun download(songs: List<MediaMetadata>) {
        songs.forEach { song -> downloadSong(song.id, song.title) }
    }

    fun download(song: MediaMetadata) {
        downloadSong(song.id, song.title)
    }

    fun download(song: SongEntity) {
        downloadSong(song.id, song.title)
    }

    private fun downloadSong(id: String, title: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.updateDownloadStatus(id, DL_IN_PROGRESS)

            // copy directly from player cache
            val playerCacheSong = getAndDeleteFromCache(playerCache, id)
            if (playerCacheSong != null) {
                Log.d(TAG, "Song found in player cache. Copying from player cache.")
                downloadMgr.enqueue(id, playerCacheSong, displayName = title)
            }

            Log.d(TAG, "Song NOT found in player cache. Fetching.")
            songUrlCache[id]?.takeIf { it.second > System.currentTimeMillis() }?.let {
                downloadMgr.enqueue(id, it.first.toUri().toString())
                return@launch
            }

            val playbackData = runBlocking(Dispatchers.IO) {
                YTPlayerUtils.playerResponseForPlayback(
                    id,
                    audioQuality = audioQuality,
                    connectivityManager = connectivityManager,
                )
            }.getOrThrow()
            val format = playbackData.format
            database.query {
                upsert(
                    FormatEntity(
                        id = id,
                        itag = format.itag,
                        mimeType = format.mimeType.split(";")[0],
                        codecs = format.mimeType.split("codecs=")[1].removeSurrounding("\""),
                        bitrate = format.bitrate,
                        sampleRate = format.audioSampleRate,
                        contentLength = format.contentLength!!,
                        loudnessDb = playbackData.audioConfig?.loudnessDb,
                        playbackTrackingUrl = playbackData.playbackTracking?.videostatsPlaybackUrl?.baseUrl
                    )
                )
            }
            val streamUrl = playbackData.streamUrl.let {
                // Specify range to avoid YouTube's throttling
                "${it}&range=0-${format.contentLength ?: 10000000}"
            }

            songUrlCache[id] =
                streamUrl to System.currentTimeMillis() + (playbackData.streamExpiresInSeconds * 1000L)

            downloadMgr.enqueue(id, streamUrl, displayName = title)
        }

    }

    fun delete(song: PlaylistSong) {
        deleteSong(song.song.id)
    }

    fun delete(song: SongItem) {
        deleteSong(song.id)
    }

    fun delete(song: Song) {
        deleteSong(song.song.id)
    }

    fun delete(song: SongEntity) {
        deleteSong(song.id)
    }

    fun delete(songs: List<MediaMetadata>) {
        songs.forEach {
            deleteSong(it.id)
        }
    }

    fun delete(song: MediaMetadata) {
        deleteSong(song.id)
    }

    private fun deleteSong(id: String) {
        localMgr.deleteFile(id)
        downloads.update { map ->
            map.toMutableMap().apply {
                remove(id)
            }
        }

        runBlocking {
            database.song(id).first()?.song?.copy(localPath = null)
            database.updateDownloadStatus(id, null)
        }
    }

    fun autoDownloadIfLiked(songs: List<SongEntity>) {
        songs.forEach { song -> autoDownloadIfLiked(song) }
    }

    fun autoDownloadIfLiked(song: SongEntity) {
        if (!song.liked || song.dateDownload != null) {
            return
        }

        val isWifiConnected = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false

        if (
            context.getLikeAutoDownload() == LikedAutodownloadMode.ON
            || (context.getLikeAutoDownload() == LikedAutodownloadMode.WIFI_ONLY && isWifiConnected)
        ) {
            download(song)
        }
    }

    /**
     * Retrieve song from cache, and delete it from cache afterwards
     */
    fun getAndDeleteFromCache(cache: SimpleCache, mediaId: String): ByteArray? {
        val spans: Set<CacheSpan> = cache.getCachedSpans(mediaId)
        if (spans.isEmpty()) return null

        val output = ByteArrayOutputStream()
        try {
            for (span in spans) {
                val file: File? = span.file
                FileInputStream(file).use { fis ->
                    fis.copyTo(output)
                }
            }

            cache.removeResource(mediaId)
            return output.toByteArray()
        } catch (e: IOException) {
            reportException(e)
        } finally {
            output.close()
        }
        return null
    }

    /**
     * Migrated existing downloads from the download cache to the new system in external storage
     */
    fun migrateDownloads() {
        isProcessingDownloads.value = true
        try {
            // "skeleton" of old download manager to access old download data
            val dataSourceFactory = ResolvingDataSource.Factory(
                CacheDataSource.Factory()
                    .setCache(playerCache)
                    .setUpstreamDataSourceFactory(
                        OkHttpDataSource.Factory(
                            OkHttpClient.Builder()
                                .proxy(YouTube.proxy)
                                .build()
                        )
                    )
            ) { dataSpec ->
                return@Factory dataSpec
            }

            val downloadManager: DownloadManager = DownloadManager(
                context,
                databaseProvider,
                downloadCache,
                dataSourceFactory,
                Executor(Runnable::run)
            ).apply {
                maxParallelDownloads = 3
            }

            // actual migration code
            val downloadedSongs = mutableMapOf<String, Download>()
            val cursor = downloadManager.downloadIndex.getDownloads()
            while (cursor.moveToNext()) {
                downloadedSongs[cursor.download.request.id] = cursor.download
            }

            // copy all completed downloads
            val toMigrate = downloadedSongs.filter { it.value.state == Download.STATE_COMPLETED }
            toMigrate.forEach { s ->
                val songFromCache = getAndDeleteFromCache(downloadCache, s.key)
                if (songFromCache != null) {
                    downloadMgr.enqueue(
                        mediaId = s.key,
                        data = songFromCache,
                        displayName = runBlocking { database.song(s.key).first()?.title ?: "" })
                }
            }
        } catch (e: Exception) {
            reportException(e)
        } finally {
            isProcessingDownloads.value = false
        }
    }

    fun cd() {
        localMgr = DownloadDirectoryManagerOt(
            context,
            File(allowedPath + "/" + context.dataStore.get(DownloadPathKey, defaultDownloadPath) + "/")
        )
    }


    /**
     * Rescan download directory and updates songs
     */
    fun rescanDownloads() {
        isProcessingDownloads.value = true
        val dbDownloads = runBlocking(Dispatchers.IO) { database.downloadedSongs().first() }
        val result = mutableMapOf<String, LocalDateTime>()

        // remove missing files
        val missingFiles = localMgr.getMissingFiles(dbDownloads)
        missingFiles.forEach {
            runBlocking(Dispatchers.IO) { database.removeDownloadSong(it.song.id) }
        }

        // register new files
        val availableDownloads = dbDownloads.minus(missingFiles)
        availableDownloads.forEach { s ->
            result[s.song.id] = s.song.dateDownload!! // sql should cover our butts
        }
        isProcessingDownloads.value = false

        downloads.value = result
    }

    fun scanDownloads() {
        isProcessingDownloads.value = true
        runBlocking(Dispatchers.IO) { database.removeAllDownloadedSongs() }
        val result = mutableMapOf<String, LocalDateTime>()
        val timeNow = LocalDateTime.now()

        // remove missing files
        val availableFiles = localMgr.getAvailableFiles()
        availableFiles.forEach {
            runBlocking(Dispatchers.IO) { database.registerDownloadSong(it.key, timeNow, it.value) }
        }

        // pull from db again
        val dbDownloads = runBlocking(Dispatchers.IO) { database.downloadedSongs().first() }
        dbDownloads.forEach { s ->
            result[s.song.id] = timeNow
        }

        isProcessingDownloads.value = false
        downloads.value = result
    }

    init {
        rescanDownloads()

        CoroutineScope(Dispatchers.IO).launch {
            downloadMgr.events.collect { ev ->
                when (ev) {
                    is DownloadEvent.Progress -> {
                        val pct = ev.bytesRead * 100 / (if (ev.contentLength > 0) ev.contentLength else 1)
                        // update UI
                        Log.v(TAG, "DL progress: $pct")
                    }

                    is DownloadEvent.Success -> {
                        // playback from ev.file.absolutePath
                        val updateTime =
                            LocalDateTime.now().atOffset(ZoneOffset.UTC).toLocalDateTime()
                        database.registerDownloadSong(ev.mediaId, updateTime, ev.file.toString())
                    }

                    is DownloadEvent.Failure -> {
                        // show error ev.error
                        database.removeDownloadSong(ev.mediaId)
                        reportException(ev.error)
                    }
                }
            }
        }
    }

    companion object {
        val DL_IN_PROGRESS = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)
    }
}