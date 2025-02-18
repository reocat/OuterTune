/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.utils.scanners

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.dd3boh.outertune.MainActivity
import com.dd3boh.outertune.db.entities.AlbumEntity
import com.dd3boh.outertune.db.entities.ArtistEntity
import com.dd3boh.outertune.db.entities.FormatEntity
import com.dd3boh.outertune.db.entities.GenreEntity
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.db.entities.SongEntity
import com.dd3boh.outertune.models.SongTempData
import com.dd3boh.outertune.ui.utils.ARTIST_SEPARATORS
import com.dd3boh.outertune.ui.utils.DEBUG_SAVE_OUTPUT
import com.dd3boh.outertune.ui.utils.EXTRACTOR_DEBUG
import com.dd3boh.outertune.ui.utils.EXTRACTOR_TAG
import com.dd3boh.outertune.utils.reportException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import timber.log.Timber
import java.io.File
import java.lang.Long.parseLong
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.roundToLong

const val toSeconds = 1000 * 60 * 16.7 // convert FFmpeg duration to seconds

class FFMpegScanner(context: Context) : MetadataScanner {
    val ctx = context

    /**
     * Given a path to a file, extract all necessary metadata
     *
     * @param path Full file path
     */
    override fun getAllMetadataFromPath(path: String): SongTempData {
        if (EXTRACTOR_DEBUG)
            Timber.tag(EXTRACTOR_TAG).d("Starting Full Extractor session on: $path")

        var data: String = ""
        val mutex = Mutex(true)
        val intent = Intent("wah.mikooomich.ffMetadataEx.ACTION_EXTRACT_METADATA").apply {
            putExtra("filePath", path)
        }

        try {
            (ctx as MainActivity).activityLauncher.launchActivityForResult(intent) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val metadata = result.data?.getStringExtra("rawExtractorData")
                    if (metadata != null) {
                        data = metadata
                        mutex.unlock()
                    } else {
                        data = "No metadata received"
                    }
                } else {
                    data = "Metadata extraction failed"
                }
            }
        } catch (e: ActivityNotFoundException) {
            throw ScannerCriticalFailureException("ffMetaDataEx extractor app not found: ${e.message}")
        }

        // wait until scanner finishes
        runBlocking {
            var delays = 0

            // TODO: make this less cursed
            while (mutex.isLocked) {
                delay(100)
                delays++
                if (delays > 100) {
                    reportException(Exception("Took too long to extract metadata from ffMetadataEx. Bailing. $path"))
                    mutex.unlock()
                }
            }
        }

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
            !rawTitle.isNullOrBlank() -> rawTitle!!.trim()
            else -> path.substringAfterLast('/').substringBeforeLast('.')
        }

        val duration: Long = try {
            (parseLong(rawDuration?.trim() ?: "0") / toSeconds).roundToLong()
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
                title = albumName!!,
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
                    LocalDate.parse(rawDate!!.substringAfter(';').trim()).atStartOfDay()
                } catch (e: Exception) {
                    null
                }
                
                // If date parsing failed, try to parse year
                year = parsedDate?.year ?: rawDate!!.trim().toIntOrNull()
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

    /**
     * Given a path to a file, extract necessary metadata. For fields FFmpeg is
     * unable to extract, use the provided FormatEntity data.
     *
     * @param file Full file path
     */
    override fun getAllMetadataFromFile(file: File): SongTempData {
        return getAllMetadataFromPath(file.path)
    }

}