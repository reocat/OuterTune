package com.dd3boh.outertune.playback.downloadManager

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.utils.scanners.LocalMediaScanner.Companion.getRealDlPathFromUri
import java.io.File
import java.io.InputStream

class DownloadDirectoryManagerOt(private val context: Context, private var dir: File, extraDirs: List<String>) {
    var allDirs: List<File>

    init {
        dir = File(getRealDlPathFromUri(dir.absolutePath))
        if (!dir.exists()) {
            dir.mkdirs()  // ensure the directory exists
        }
        require(dir.isDirectory) { "Provided path is not a directory: ${dir.absolutePath}" }

        allDirs = mutableListOf(dir) + extraDirs.map { File(getRealDlPathFromUri(it)) }
            .filterNot { it.absolutePath == dir.absolutePath || !dir.isDirectory }
    }

    fun deleteFile(mediaId: String): Boolean {
        val existingFile = isExists(mediaId)?.name
        if (existingFile == null) return false

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val where = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
        // the / at the end is mandatory
        val args =
            arrayOf(existingFile, "${Environment.DIRECTORY_MUSIC}/${dir.absolutePath.substringAfter("/Music/")}/")
        val deleted = context.contentResolver.delete(uri, where, args)
        return deleted > 0
    }

    fun saveFile(mediaId: String, input: InputStream, displayName: String?): Uri? {
        val fileString = "$displayName [$mediaId].mka"
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

        return uri
    }

    fun isExists(mediaId: String): File? {
        allDirs.forEach { d ->
           val s = d.walk().firstOrNull { it.nameWithoutExtension.endsWith("[$mediaId]") }
            if (s!= null) {
                return s
            }
        }
        return null
    }

    fun getFilePathIfExists(mediaId: String): Uri? {
        var existingFile: File? = isExists(mediaId)
        return if (existingFile != null) Uri.fromFile(existingFile) else null
    }

    fun getMissingFiles(mediaId: List<Song>): List<Song> {
        val missingFiles = mediaId.toMutableSet()
        // crawl files, remove files that exist
        allDirs.forEach { d ->
            d.walk().forEach { file ->
                val mediaId = file.nameWithoutExtension.substringAfterLast('[').substringBeforeLast(']')
                missingFiles.removeIf { it.id == mediaId }
            }
        }

        return missingFiles.toList()
    }

    fun getAvailableFiles(): Map<String, String> {
        val availableFiles = HashMap<String, String>()
        // crawl files, add files that exist
        allDirs.forEach { d ->
            d.walk().forEach { file ->
                val mediaId = file.nameWithoutExtension.substringAfterLast('[').substringBeforeLast(']')
                availableFiles.put(mediaId, file.absolutePath)
            }
        }

        return availableFiles
    }
}

