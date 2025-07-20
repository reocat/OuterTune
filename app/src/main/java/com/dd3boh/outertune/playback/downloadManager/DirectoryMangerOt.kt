package com.dd3boh.outertune.playback.downloadManager

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.utils.reportException
import com.dd3boh.outertune.utils.scanners.LocalMediaScanner.Companion.scanDfRecursive
import com.dd3boh.outertune.utils.scanners.documentFileFromUri
import timber.log.Timber
import java.io.IOException
import java.io.InputStream

class DownloadDirectoryManagerOt(private var context: Context, private var dir: Uri, extraDirs: List<Uri>) {
    val TAG = DownloadDirectoryManagerOt::class.simpleName.toString()
    var mainDir: DocumentFile? = null
    var allDirs: List<DocumentFile> = mutableListOf()

    init {
        if (dir.path.isNullOrEmpty()) {
            Timber.tag(TAG).w("Download directory URI is not set. Download manager will not be initialized.")
        } else {
            doInit(context, dir, extraDirs)
        }
    }

    fun doInit(context: Context, dir: Uri, extraDirs: List<Uri>) {
        Timber.tag(TAG).i("Initializing download manager: $dir")
        this.context = context
        this.dir = dir
        try {
            mainDir = documentFileFromUri(context, dir)
            if (mainDir == null || !mainDir!!.isDirectory) {
                throw IOException("Invalid directory")
            }

            /* TODO: .nomedia for downloads folder (permission denied)
            if (!mainDir!!.listFiles().any { it.name == ".nomedia" }) {
               documentFileFromUri(context, dir)?.createFile("audio/mka", ".nomedia")
            }
            */
            val newAllDirs = mutableListOf<DocumentFile>()
            newAllDirs.add(mainDir!!)
            if (extraDirs.isNotEmpty()) {
                newAllDirs.addAll(
                    documentFileFromUri(context, extraDirs.filterNot { it == dir }).filter { it.isDirectory }
                )
            }
            allDirs = newAllDirs.toList()
            Timber.tag(TAG).i("Download manager initialized successfully. Found ${allDirs.size} directories.")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to initiate download manager")
            mainDir = null
            allDirs = mutableListOf()
            reportException(e)
        }
    }

    fun deleteFile(mediaId: String): Boolean {
        val file = isExists(mediaId)
        return file?.delete() == true
    }

    fun saveFile(mediaId: String, input: InputStream, displayName: String?): Uri? {
        val directory = mainDir
        if (directory == null || !directory.isDirectory) {
            throw IOException("Invalid directory")
        }

        val resolver = context.contentResolver
        val fileName = "$displayName [$mediaId].mka"
        val newFile = directory.createFile("audio/mka", fileName)

        newFile?.uri?.let { uri ->
            resolver.openOutputStream(uri)?.use { out ->
                input.copyTo(out)
            }
            return uri
        }

        return null
    }

    fun isExists(mediaId: String): DocumentFile? {
        if (allDirs.isEmpty()) return null
        val result = ArrayList<DocumentFile>()
        for (dir in allDirs) {
            scanDfRecursive(dir, result, true) { it.substringAfterLast('[').substringBeforeLast(']') == mediaId }
        }
        return result.firstOrNull()
    }

    fun getFilePathIfExists(mediaId: String): Uri? {
        val existingFile: DocumentFile? = isExists(mediaId)
        return existingFile?.uri
    }

    fun getMissingFiles(mediaId: List<Song>): List<Song> {
        if (allDirs.isEmpty()) return emptyList()

        val missingFiles = mediaId.toMutableSet()
        val result = getAvailableFiles()
        missingFiles.removeIf { f -> result.any { it.key == f.id } }
        return missingFiles.toList()
    }

    fun getAvailableFiles(): Map<String, Uri> {
        if (allDirs.isEmpty()) return emptyMap()
        val availableFiles = HashMap<String, Uri>()
        val result = ArrayList<DocumentFile>()
        for (dir in allDirs) {
            scanDfRecursive(dir, result, true)
        }

        for (file in result) {
            val path = file.name ?: continue
            availableFiles[path.substringAfterLast('[').substringBeforeLast(']')] = file.uri
        }
        return availableFiles
    }

    fun getMainDlStorageUsage(): Long {
        if (mainDir == null) return -1L
        val result = ArrayList<DocumentFile>()
        scanDfRecursive(mainDir!!, result, true)

        return result.filter { it.name != null }.sumOf { it.length() }
    }

    fun getTotalDlStorageUsage(): Long {
        if (allDirs.isEmpty()) return 0
        val result = ArrayList<DocumentFile>()
        for (dir in allDirs) {
            scanDfRecursive(dir, result, true)
        }

        return result.filter { it.name != null }.sumOf { it.length() }
    }

    fun getExtraDlStorageUsage(): Long {
        val dirs = allDirs.filter { it != mainDir }
        if (dirs.isEmpty()) return 0
        val result = ArrayList<DocumentFile>()
        for (dir in dirs) {
            scanDfRecursive(dir, result, true)
        }

        return result.filter { it.name != null }.sumOf { it.length() }
    }
}