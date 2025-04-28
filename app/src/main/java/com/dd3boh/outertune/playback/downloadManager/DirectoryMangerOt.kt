package com.dd3boh.outertune.playback.downloadManager

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.dd3boh.outertune.db.entities.Song
import java.io.File
import java.io.IOException
import java.io.InputStream

class DownloadDirectoryManagerOt(private val context: Context, private val dir: File) {

    init {
        if (!dir.exists()) {
            dir.mkdirs()  // ensure the directory exists
        }
        require(dir.isDirectory) { "Provided path is not a directory: ${dir.absolutePath}" }
    }

    fun deleteFile(mediaId: String): Boolean {
        val existingFile = dir.walk().filter {
            it.nameWithoutExtension.endsWith("[$mediaId]")
        }.firstOrNull()?.name

        if (existingFile == null) return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val where = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
            // the / at the end is mandatory
            val args =
                arrayOf(existingFile, "${Environment.DIRECTORY_MUSIC}/${dir.absolutePath.substringAfter("/Music/")}/")
            val deleted = context.contentResolver.delete(uri, where, args)
            deleted > 0
        } else {
            val outFile = File(dir, existingFile)
            outFile.exists() && outFile.delete()
        }
    }

    fun saveFile(mediaId: String, input: InputStream, displayName: String?): Uri? {
        val fileString = "$displayName [$mediaId].mka"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileString)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/mka")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_MUSIC + "/" + dir.absolutePath.substringAfter("/Music/")
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = resolver.insert(collection, values) ?: return null

            resolver.openOutputStream(uri)?.use { out ->
                input.use { inp -> inp.copyTo(out) }
            }


            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            uri
        } else {
            val outFile = File(dir, fileString)
            return try {
                input.use { inp ->
                    outFile.outputStream().use { out -> inp.copyTo(out) }
                }
                Uri.fromFile(outFile)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    fun isExists(mediaId: String): Boolean {
        return dir.walk().filter { it.name.endsWith("[$mediaId]") }.firstOrNull() != null
    }

    fun getFilePathIfExists(mediaId: String): Uri? {
        val existingFile = dir.walk().filter {
            it.nameWithoutExtension.endsWith("[$mediaId]")
        }.firstOrNull()
        return if (existingFile != null) Uri.fromFile(existingFile) else null
    }

    fun getMissingFiles(mediaId: List<Song>): List<Song> {
        val missingFiles = mediaId.toMutableSet()
        // crawl files, remove files that exist
        dir.walk().forEach { file ->
            val mediaId = file.nameWithoutExtension.substringAfterLast('[').substringBeforeLast(']')
            missingFiles.removeIf { it.id == mediaId }
        }

        return missingFiles.toList()
    }

    fun getAvailableFiles(): Map<String, String> {
        val availableFiles = HashMap<String, String>()
        // crawl files, add files that exist
        dir.walk().forEach { file ->
            val mediaId = file.nameWithoutExtension.substringAfterLast('[').substringBeforeLast(']')
            availableFiles.put(mediaId, file.absolutePath)
        }

        return availableFiles
    }
}

