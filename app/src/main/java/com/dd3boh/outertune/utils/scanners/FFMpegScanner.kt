package com.dd3boh.outertune.utils.scanners

import com.dd3boh.ffMetadataEx.FFMpegWrapper
import com.dd3boh.outertune.db.entities.AlbumEntity
import com.dd3boh.outertune.db.entities.ArtistEntity
import com.dd3boh.outertune.db.entities.FormatEntity
import com.dd3boh.outertune.db.entities.GenreEntity
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.db.entities.SongEntity
import com.dd3boh.outertune.models.SongTempData
import com.dd3boh.outertune.ui.utils.ARTIST_SEPARATORS
import timber.log.Timber
import java.io.File
import java.lang.Integer.parseInt
import java.lang.Long.parseLong
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.roundToLong

const val EXTRACTOR_DEBUG = false
const val DEBUG_SAVE_OUTPUT = false // ignored (will be false) when EXTRACTOR_DEBUG IS false
const val EXTRACTOR_TAG = "FFMpegExtractor"
const val toSeconds = 1000 * 60 * 16.7 // convert FFmpeg duration to seconds

class FFMpegScanner : MetadataScanner {
    // load advanced scanner libs
    init {
        System.loadLibrary("avcodec")
        System.loadLibrary("avdevice")
        System.loadLibrary("ffmetaexjni")
        System.loadLibrary("avfilter")
        System.loadLibrary("avformat")
        System.loadLibrary("avutil")
        System.loadLibrary("swresample")
        System.loadLibrary("swscale")
    }

    /**
     * Given a path to a file, extract all necessary metadata
     *
     * @param path Full file path
     */
    override fun getAllMetadata(path: String): SongTempData {
        if (EXTRACTOR_DEBUG)
            Timber.tag(EXTRACTOR_TAG).d("Starting Full Extractor session on: $path")
        val ffmpeg = FFMpegWrapper()
        val data = ffmpeg.getFullAudioMetadata(path)

        if (EXTRACTOR_DEBUG && DEBUG_SAVE_OUTPUT) {
            Timber.tag(EXTRACTOR_TAG).d("Full output for: $path \n $data")
        }

        val songId = SongEntity.generateSongId()
        var rawTitle: String? = null
        var artists: String? = null
        var albumName: String? = null
        var genres: String? = null
        var rawDate: String? = null
        var codec: String? = null
        var type: String? = null
        var bitrate: String? = null
        var sampleRate: String? = null
        var channels: String? = null
        var rawDuration: String? = null
        var replayGain: Double? = null

        // read data from FFmpeg
        data.lines().forEach {
            val tag = it.substringBefore(':')
            when (tag) {
                "ARTISTS", "ARTIST", "artist" -> artists = it.substringAfter(':')
                "ALBUM", "album" -> albumName = it.substringAfter(':')
                "TITLE", "title" -> rawTitle = it.substringAfter(':')
                "GENRE", "genre" -> genres = it.substringAfter(':')
                "DATE", "date" -> rawDate = it.substringAfter(':')
                "codec" -> codec = it.substringAfter(':')
                "type" -> type = it.substringAfter(':')
                "bitrate" -> bitrate = it.substringAfter(':')
                "sampleRate" -> sampleRate = it.substringAfter(':')
                "channels" -> channels = it.substringAfter(':')
                "duration" -> rawDuration = it.substringAfter(':')
                else -> ""
            }
        }

        // Fix for title parsing
        val title = when {
            !rawTitle.isNullOrBlank() -> rawTitle.trim()
            else -> path.substringAfterLast('/').substringBeforeLast('.')
        }

        val duration: Long = try {
            (parseLong(rawDuration?.trim()) / toSeconds).roundToLong()
        } catch (e: Exception) {
            -1L
        }

        val dateModified = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(File(path).lastModified()),
            ZoneOffset.UTC
        )
        val albumId = if (!albumName.isNullOrBlank()) AlbumEntity.generateAlbumId() else null
        val mime = when {
            !type.isNullOrBlank() && !codec.isNullOrBlank() -> "${type!!.trim()}/${codec!!.trim()}"
            else -> "Unknown"
        }

        val artistList = ArrayList<ArtistEntity>()
        val genresList = ArrayList<GenreEntity>()
        var year: Int? = null
        var parsedDate: LocalDateTime? = null

        // Parse album
        val albumEntity = if (!albumName.isNullOrBlank() && albumId != null) {
            AlbumEntity(
                id = albumId,
                title = albumName,
                songCount = 1,
                duration = duration.toInt()
            )
        } else null

        // Parse artists
        artists?.split(ARTIST_SEPARATORS)?.forEach { element ->
            val artistVal = element.trim()
            if (artistVal.isNotBlank()) {
                artistList.add(ArtistEntity(ArtistEntity.generateArtistId(), artistVal, isLocal = true))
            }
        }

        // Parse genres
        genres?.split(";")?.forEach { element ->
            val genreVal = element.trim()
            if (genreVal.isNotBlank()) {
                genresList.add(GenreEntity(GenreEntity.generateGenreId(), genreVal, isLocal = true))
            }
        }

        // Parse date and year
        if (!rawDate.isNullOrBlank()) {
            try {
                // Try to parse as full date first
                parsedDate = try {
                    LocalDate.parse(rawDate.substringAfter(';').trim()).atStartOfDay()
                } catch (e: Exception) {
                    null
                }
                
                // If date parsing failed, try to parse year
                year = parsedDate?.year ?: rawDate.trim().toIntOrNull()
            } catch (e: Exception) {
                // Invalid date format, both attempts failed
            }
        }

        return SongTempData(
            Song(
                song = SongEntity(
                    id = songId,
                    title = title,
                    duration = duration.toInt(),
                    thumbnailUrl = path,
                    albumId = albumId,
                    albumName = albumName,
                    year = year,
                    date = parsedDate,
                    dateModified = dateModified,
                    isLocal = true,
                    inLibrary = LocalDateTime.now(),
                    localPath = path
                ),
                artists = artistList,
                album = albumEntity,
                genre = genresList
            ),
            FormatEntity(
                id = songId,
                itag = -1,
                mimeType = mime,
                codecs = codec?.trim() ?: "Unknown",
                bitrate = bitrate?.trim()?.toIntOrNull() ?: -1,
                sampleRate = sampleRate?.trim()?.toIntOrNull() ?: -1,
                contentLength = duration,
                loudnessDb = replayGain,
                playbackUrl = null
            )
        )
    }
}