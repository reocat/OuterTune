/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.utils.scanners

import android.media.MediaMetadataRetriever
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
import com.dd3boh.outertune.constants.SCANNER_DEBUG
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

class MediaMetadataScanner : MetadataScanner {

    /**
     * Given a path to a file, extract necessary metadata.
     *
     * @param path Full file path
     */
    override fun getAllMetadataFromPath(path: String) = getAllMetadataFromFile(File(path))

    /**
     * Given a file, extract necessary metadata using MediaMetadataRetriever.
     *
     * @param file File object
     */
    override fun getAllMetadataFromFile(file: File): SongTempData {
        if (!file.exists() || !file.canRead()) {
            Timber.tag(EXTRACTOR_TAG).e("File not found or unreadable: ${file.path}")
            throw InvalidAudioFileException("File not found or unreadable: ${file.path}")
        }
        if (EXTRACTOR_DEBUG) {
            Timber.tag(EXTRACTOR_TAG).d("Starting metadata extraction for: ${file.path}")
        }

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)

            val songId = SongEntity.generateSongId()
            var rawTitle: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val albumName: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val rawArtist: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val rawGenre: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val rawDuration: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val rawYear: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            var date: LocalDateTime? = null
            var year: Int? = null
            var codec: String? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.substringAfter("audio/")
            var bitrate: Int? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
            val sampleRate: Int? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()
            val channels: Int? = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)?.toIntOrNull() ?: 2 // Default to stereo if unknown

            if (EXTRACTOR_DEBUG && DEBUG_SAVE_OUTPUT) {
                val allData = """
                    Title: $rawTitle
                    Album: $albumName
                    Artist: $rawArtist
                    Genre: $rawGenre
                    Duration: $rawDuration
                    Year: $rawYear
                    MimeType: ${retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)}
                    Bitrate: $bitrate
                    SampleRate: $sampleRate
                    Channels: $channels
                """.trimIndent()
                Timber.tag(EXTRACTOR_TAG).d("Metadata for ${file.path}:\n$allData")
            }

            val title = when {
                rawTitle.isNullOrBlank() -> file.nameWithoutExtension
                else -> rawTitle.trim()
            }

            val duration = rawDuration?.toLongOrNull()?.div(1000) ?: 0L // Convert ms to seconds

            val artistList = ArrayList<ArtistEntity>()
            rawArtist?.split(ARTIST_SEPARATORS)?.forEach { artist ->
                if (artist.isNotBlank()) {
                    artistList.add(ArtistEntity(ArtistEntity.generateArtistId(), artist.trim(), isLocal = true))
                }
            }
            artistList.distinctBy { it.name }

            val genresList = ArrayList<GenreEntity>()
            rawGenre?.split(ARTIST_SEPARATORS)?.forEach { genre ->
                if (genre.isNotBlank()) {
                    genresList.add(GenreEntity(GenreEntity.generateGenreId(), genre.trim(), isLocal = true))
                }
            }
            genresList.distinctBy { it.title }

            try {
                if (rawYear != null && rawYear.isNotBlank()) {
                    date = LocalDate.parse(rawYear.trim()).atStartOfDay()
                    year = rawYear.trim().toIntOrNull()
                }
            } catch (e: DateTimeParseException) {
                if (SCANNER_DEBUG) {
                    Timber.tag(EXTRACTOR_TAG).w("Failed to parse date: $rawYear, error: ${e.message}")
                }
                year = rawYear?.trim()?.toIntOrNull()
            }

            val dateModified = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(file.lastModified()),
                ZoneOffset.UTC
            )

            val albumId = if (albumName != null) AlbumEntity.generateAlbumId() else null
            val albumEntity = if (albumName != null && albumId != null) {
                AlbumEntity(
                    id = albumId,
                    title = albumName.trim(),
                    songCount = 1,
                    duration = duration.toInt(),
                    isLocal = true
                )
            } else {
                null
            }

            // Fallback for codec and bitrate
            codec = codec ?: file.extension.lowercase()
            bitrate = bitrate ?: 128000 // Default to 128kbps if unknown

            return SongTempData(
                Song(
                    song = SongEntity(
                        id = songId,
                        title = title,
                        duration = duration.toInt(),
                        thumbnailUrl = null,
                        albumId = albumId,
                        albumName = albumName,
                        year = year,
                        date = date,
                        dateModified = dateModified,
                        isLocal = true,
                        inLibrary = LocalDateTime.now(),
                        localPath = file.path
                    ),
                    artists = artistList,
                    album = albumEntity,
                    genre = genresList
                ),
                FormatEntity(
                    id = songId,
                    itag = -1,
                    mimeType = "audio/$codec",
                    codecs = codec,
                    bitrate = bitrate,
                    sampleRate = sampleRate ?: 44100, // Default to 44.1kHz if unknown
                    contentLength = duration,
                    loudnessDb = null, // MediaMetadataRetriever doesn't support ReplayGain
                    playbackTrackingUrl = null
                )
            )
        } catch (e: Exception) {
            Timber.tag(EXTRACTOR_TAG).e(e, "Failed to extract metadata for: ${file.path}")
            throw InvalidAudioFileException("Failed to extract metadata: ${e.message}")
        } finally {
            retriever.release()
        }
    }
}