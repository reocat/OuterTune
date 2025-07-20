/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.collection.LruCache
import androidx.core.graphics.scale
import com.dd3boh.outertune.App
import com.dd3boh.outertune.constants.MaxImageCacheSizeKey
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

class LmImageCacheMgr {

    // Keep a small in-memory cache for frequently accessed thumbnails
    private val memCache: LruCache<String, Bitmap> = LruCache(50 * 1024 * 1024) // 50MB memory cache

    private fun getDiskCacheDir(context: Context): File {
        return File(context.cacheDir, "embedded_thumbnails").also { it.mkdirs() }
    }

    /**
     * Extract the album art from the audio file.
     * This now uses a persistent disk cache for performance.
     *
     * @param path Full path of audio file
     * @param resize Whether to resize the Bitmap to a thumbnail size (300x300)
     */
    fun getLocalThumbnail(path: String?, resize: Boolean): Bitmap? {
        if (path == null) return null

        val key = generateCacheKey(path, resize)
        val context = App.instance.applicationContext

        memCache[key]?.let { return it }

        val diskCacheDir = getDiskCacheDir(context)
        val file = File(diskCacheDir, key)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap != null) {
                memCache.put(key, bitmap)
                return bitmap
            }
        }

        val mData = MediaMetadataRetriever()
        try {
            mData.setDataSource(path)
            val art = mData.embeddedPicture ?: return null
            val bitmap = BitmapFactory.decodeByteArray(art, 0, art.size)

            val finalBitmap = if (resize) {
                bitmap.scale(300, 300, false)
            } else {
                bitmap
            }

            file.outputStream().use {
                finalBitmap.compress(Bitmap.CompressFormat.WEBP, 85, it)
            }
            memCache.put(key, finalBitmap)

            manageDiskCacheSize(context, diskCacheDir)

            return finalBitmap
        } catch (e: Exception) {
            reportException(e)
            return null
        } finally {
            mData.release()
        }
    }

    private fun generateCacheKey(path: String, resize: Boolean): String {
        return md5(path) + if (resize) "_thumb" else "_full"
    }

    private fun manageDiskCacheSize(context: Context, diskCacheDir: File) {
        val maxSizeMB = context.dataStore.get(MaxImageCacheSizeKey, 512)
        if (maxSizeMB <= 0) {
            purgeCache()
            return
        }
        val maxSize = maxSizeMB * 1024 * 1024L
        val files = diskCacheDir.listFiles() ?: return
        var currentSize = files.sumOf { it.length() }

        if (currentSize > maxSize) {
            val sortedFiles = files.sortedBy { it.lastModified() }
            for (cacheFile in sortedFiles) {
                if (currentSize <= maxSize) break
                currentSize -= cacheFile.length()
                cacheFile.delete()
            }
        }
    }

    /**
     * Purges both memory and disk caches.
     */
    fun purgeCache() {
        val context = App.instance.applicationContext
        memCache.evictAll()
        getDiskCacheDir(context).deleteRecursively()
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }
}