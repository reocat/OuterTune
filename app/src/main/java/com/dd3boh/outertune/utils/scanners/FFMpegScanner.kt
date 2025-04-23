/*
 * Copyright (C) 2025 O​u​t​er​Tu​ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.utils.scanners

import android.util.Log
import com.dd3boh.outertune.db.entities.AlbumEntity
import com.dd3boh.outertune.db.entities.ArtistEntity
import com.dd3boh.outertune.db.entities.FormatEntity
import com.dd3boh.outertune.db.entities.GenreEntity
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.db.entities.SongEntity
import com.dd3boh.outertune.models.SongTempData
import com.dd3boh.outertune.ui.utils.ARTIST_SEPARATORS
import com.dd3boh.outertune.constants.DEBUG_SAVE_OUTPUT
import com.dd3boh.outertune.constants.EXTRACTOR_DEBUG
import com.dd3boh.outertune.ui.utils.EXTRACTOR_TAG
import wah.mikooomich.ffMetadataEx.AudioMetadata
import wah.mikooomich.ffMetadataEx.FFMpegWrapper
import java.io.File
import java.lang.Integer.parseInt
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.roundToLong

const val toSeconds = 1000 * 60 * 16.7 // convert FFmpeg duration to seconds

class FFMpegScanner() : MetadataScanner {
    // load advanced scanner libs
    init {
//        System.loadLibrary("avcodec")
//        System.loadLibrary("avdevice")
//        System.loadLibrary("avfilter")
//        System.loadLibrary("avformat")
//        System.loadLibrary("avutil")
//        System.loadLibrary("swresample")
//        System.loadLibrary("swscale")
        System.loadLibrary("ffmetaexjni")
    }

    /**
     * Given a path to a file, extract all necessary metadata
     *
     * @param path Full file path
     */
    override fun getAllMetadataFromPath(path: String): SongTempData {
        if (EXTRACTOR_DEBUG)
            Log.v(EXTRACTOR_TAG, "Starting Full Extractor session on: $path")

        val ffmpeg = FFMpegWrapper()
        val data: AudioMetadata? = ffmpeg.getFullAudioMetadata(path)

        if (data == null) {
            Log.e(EXTRACTOR_TAG, "Fatal extraction error")
            throw RuntimeException("Fatal FFmpeg scanner extraction error")
        }
        if (data.status != 0) {
            throw RuntimeException("Fatal FFmpeg scanner extraction error. Status: ${data.status}")
        }
        if (EXTRACTOR_DEBUG && DEBUG_SAVE_OUTPUT) {
            Log.v(EXTRACTOR_TAG, "Full output for: $path \n $data")
        }

        val songId = SongEntity.generateSongId()
        var rawTitle: String? = data.title
        var rawArtists: String? = data.artist
        var albumName: String? = data.album
        var genres: String? = data.genre
        var rawDate: String? = null
        var codec: String? = data.codec
        var type: String? = data.codecType
        var bitrate: Long = data.bitrate
        var sampleRate: Int = data.sampleRate
        var channels: Int = data.channels
        var duration: Long = (data.duration / toSeconds).roundToLong()

        // read extra data from FFmpeg
        data.extrasRaw.forEach {
            val tag = it.substringBefore(':')
            when (tag) {
                // why the fsck does an error here get swallowed silently????
                "DATE", "date" -> rawDate = it.substringAfter(':')
                else -> ""
            }
        }


        /**
         * These vars need a bit more parsing
         */

        val title: String =
            if (rawTitle != null && rawTitle.isBlank() == false) { // songs with no title tag
                rawTitle.trim()
            } else {
                path.substringAfterLast('/').substringBeforeLast('.')
            }

        // should never be invalid if scanner even gets here fine...
        val dateModified =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(File(path).lastModified()), ZoneOffset.UTC)
        val albumId = if (albumName != null) AlbumEntity.generateAlbumId() else null
        val mime = if (type != null && codec != null) {
            "${type.trim()}/${codec.trim()}"
        } else {
            "Unknown"
        }

        /**
         * Parse the more complicated structures
         */

        val artistList = ArrayList<ArtistEntity>()
        val genresList = ArrayList<GenreEntity>()
        var year: Int? = null
        var date: LocalDateTime? = null

        // parse album
        val albumEntity = if (albumName != null && albumId != null) AlbumEntity(
            id = albumId,
            title = albumName,
            songCount = 1,
            duration = duration.toInt(),
            isLocal = true
        ) else null

        // parse artist
        rawArtists?.split(ARTIST_SEPARATORS)?.forEach { element ->
            val artistVal = element.trim()
            artistList.add(ArtistEntity(ArtistEntity.generateArtistId(), artistVal, isLocal = true))
        }

        // parse genre
        genres?.split(";")?.forEach { element ->
            val genreVal = element.trim()
            genresList.add(GenreEntity(GenreEntity.generateGenreId(), genreVal, isLocal = true))
        }

        // parse date and year
        try {
            if (rawDate != null) {
                try {
                    date = LocalDate.parse(rawDate.substringAfter(';').trim()).atStartOfDay()
                } catch (e: Exception) {
                }

                year = date?.year ?: parseInt(rawDate.trim())
            }
        } catch (e: Exception) {
            // user error at this point. I am not parsing all the weird ways the string can come in
        }


        return SongTempData(
            Song(
                song = SongEntity(
                    id = songId,
                    title = title,
                    duration = duration.toInt(), // we use seconds for duration
                    thumbnailUrl = null,
                    albumId = albumId,
                    albumName = albumName,
                    year = year,
                    date = date,
                    dateModified = dateModified,
                    isLocal = true,
                    inLibrary = LocalDateTime.now(),
                    localPath = path
                ),
                artists = artistList,
                // album not working
                album = albumEntity,
                genre = genresList
            ),
            FormatEntity(
                id = songId,
                itag = -1,
                mimeType = mime,
                codecs = codec?.trim() ?: "Unknown",
                bitrate = bitrate.toInt(),
                sampleRate = sampleRate,
                contentLength = duration,
                loudnessDb = null,
                playbackTrackingUrl = null
            )
        )
    }

    /**
     * Given a path to a file, extract necessary metadata.
     *
     * @param file Full file path
     */
    override fun getAllMetadataFromFile(file: File): SongTempData {
        return getAllMetadataFromPath(file.path)
    }

}