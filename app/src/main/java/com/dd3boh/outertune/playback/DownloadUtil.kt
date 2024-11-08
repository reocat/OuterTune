package com.dd3boh.outertune.playback

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.media3.common.PlaybackException
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.dd3boh.outertune.constants.AudioQuality
import com.dd3boh.outertune.constants.AudioQualityKey
import com.dd3boh.outertune.db.MusicDatabase
import com.dd3boh.outertune.db.entities.FormatEntity
import com.dd3boh.outertune.di.DownloadCache
import com.dd3boh.outertune.models.MediaMetadata
import com.dd3boh.outertune.utils.enumPreference
import com.zionhuang.innertube.YouTube
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadUtil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MusicDatabase,
    private val databaseProvider: DatabaseProvider,
    @DownloadCache private val downloadCache: SimpleCache,
) {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    private val audioQuality by enumPreference(context, AudioQualityKey, AudioQuality.AUTO)
    private val songUrlCache = HashMap<String, Pair<String, Long>>()

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private val dataSourceFactory = ResolvingDataSource.Factory(
        OkHttpDataSource.Factory(
            OkHttpClient.Builder()
                .proxy(YouTube.proxy)
                .build()
        )
    ) { dataSpec ->
        val mediaId = dataSpec.key ?: error("No media id")

        // Check if this is a downloaded song first
        val download = downloadManager.downloadIndex.getDownload(mediaId)
        if (download?.state == Download.STATE_COMPLETED) {
            return@Factory dataSpec
        }

        // Handle local songs
        if (mediaId.startsWith("LA")) {
            throw PlaybackException(
                "Local songs are non-downloadable",
                null,
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
            )
        }

        // Check network availability for non-downloaded songs
        if (!isNetworkAvailable()) {
            throw PlaybackException(
                "No network connection available and song not downloaded",
                null,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
            )
        }

        // Check URL cache
        songUrlCache[mediaId]?.takeIf { it.second > System.currentTimeMillis() }?.let {
            return@Factory dataSpec.withUri(it.first.toUri())
        }

        try {
            val playedFormat = runBlocking(Dispatchers.IO) { database.format(mediaId).first() }
            val playerResponse = runBlocking(Dispatchers.IO) {
                YouTube.player(mediaId, registerPlayback = false)
            }.getOrThrow()

            if (playerResponse.playabilityStatus.status != "OK") {
                throw PlaybackException(
                    playerResponse.playabilityStatus.reason,
                    null,
                    PlaybackException.ERROR_CODE_REMOTE_ERROR
                )
            }

            val format = (if (playedFormat != null) {
                playerResponse.streamingData?.adaptiveFormats?.find { it.itag == playedFormat.itag }
            } else {
                playerResponse.streamingData?.adaptiveFormats
                    ?.filter { it.isAudio }
                    ?.maxByOrNull {
                        it.bitrate * when (audioQuality) {
                            AudioQuality.AUTO -> if (connectivityManager.isActiveNetworkMetered) -1 else 1
                            AudioQuality.HIGH -> 1
                            AudioQuality.LOW -> -1
                        } + (if (it.mimeType.startsWith("audio/webm")) 10240 else 0)
                    }
            } ?: throw PlaybackException(
                "No suitable format found",
                null,
                PlaybackException.ERROR_CODE_DECODER_INIT_FAILED
            )).let {
                it.copy(url = "${it.url}&range=0-${it.contentLength ?: 10000000}")
            }

            // Update format in database
            database.query {
                upsert(
                    FormatEntity(
                        id = mediaId,
                        itag = format.itag,
                        mimeType = format.mimeType.split(";")[0],
                        codecs = format.mimeType.split("codecs=")[1].removeSurrounding("\""),
                        bitrate = format.bitrate,
                        sampleRate = format.audioSampleRate,
                        contentLength = format.contentLength!!,
                        loudnessDb = playerResponse.playerConfig?.audioConfig?.loudnessDb,
                        playbackUrl = playerResponse.playbackTracking?.videostatsPlaybackUrl?.baseUrl
                    )
                )
            }

            songUrlCache[mediaId] = format.url!! to (System.currentTimeMillis() + 3600000) // 1 hour cache
            dataSpec.withUri(format.url!!.toUri())
        } catch (e: Exception) {
            throw when (e) {
                is PlaybackException -> e
                else -> PlaybackException(
                    "Error preparing media: ${e.message}",
                    e,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED
                )
            }
        }
    }

    // Rest of the code remains the same...
    // (keeping the existing download management code)

    val downloadNotificationHelper = DownloadNotificationHelper(context, ExoDownloadService.CHANNEL_ID)
    
    val downloadManager: DownloadManager = DownloadManager(
        context,
        databaseProvider,
        downloadCache,
        dataSourceFactory,
        Executor(Runnable::run)
    ).apply {
        maxParallelDownloads = 3
        addListener(
            ExoDownloadService.TerminalStateNotificationHelper(
                context = context,
                notificationHelper = downloadNotificationHelper,
                nextNotificationId = ExoDownloadService.NOTIFICATION_ID + 1
            )
        )
    }

    val downloads = MutableStateFlow<Map<String, Download>>(emptyMap())

    fun getDownload(songId: String): Flow<Download?> = downloads.map { it[songId] }

    fun download(songs: List<MediaMetadata>, context: Context) {
        if (!isNetworkAvailable()) return
        songs.forEach { song -> download(song, context) }
    }

    fun download(song: MediaMetadata, context: Context) {
        if (!isNetworkAvailable()) return
        
        val downloadRequest = DownloadRequest.Builder(song.id, song.id.toUri())
            .setCustomCacheKey(song.id)
            .setData(song.title.toByteArray())
            .build()
        
        DownloadService.sendAddDownload(
            context,
            ExoDownloadService::class.java,
            downloadRequest,
            false
        )
    }

    fun resumeDownloadsOnStart(context: Context) {
        DownloadService.sendResumeDownloads(
            context,
            ExoDownloadService::class.java,
            false
        )
    }

    init {
        val result = mutableMapOf<String, Download>()
        val cursor = downloadManager.downloadIndex.getDownloads()
        while (cursor.moveToNext()) {
            result[cursor.download.request.id] = cursor.download
        }
        downloads.value = result
        
        downloadManager.addListener(
            object : DownloadManager.Listener {
                override fun onDownloadChanged(
                    downloadManager: DownloadManager,
                    download: Download,
                    finalException: Exception?
                ) {
                    downloads.update { map ->
                        map.toMutableMap().apply {
                            set(download.request.id, download)
                        }
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        if (download.state == Download.STATE_COMPLETED) {
                            val updateTime = Instant.ofEpochMilli(download.updateTimeMs)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDateTime()
                            database.updateDownloadStatus(download.request.id, updateTime)
                        } else {
                            database.updateDownloadStatus(download.request.id, null)
                        }
                    }
                }
            }
        )
    }
}
