/*
 * Copyright (C) 2025 O⁠ute⁠rTu⁠ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.utils

import android.Manifest
import android.os.Build
import com.dd3boh.outertune.constants.MAX_CONCURRENT_JOBS
import com.dd3boh.outertune.models.DirectoryTree
import com.dd3boh.outertune.utils.LmImageCacheMgr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

const val TAG = "LocalMediaUtils"

const val EXTRACTOR_TAG = "MetadataExtractor"

@OptIn(ExperimentalCoroutinesApi::class)
val scannerSession = Dispatchers.IO.limitedParallelism(MAX_CONCURRENT_JOBS)

// stuff to make this work
val MEDIA_PERMISSION_LEVEL =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE
const val STORAGE_ROOT = "/storage/"
const val DEFAULT_SCAN_PATH = "/tree/primary:Music\n"
val ARTIST_SEPARATORS = Regex("\\s*;\\s*|\\s*ft\\.\\s*|\\s*feat\\.\\s*|\\s*&\\s*|\\s*,\\s*", RegexOption.IGNORE_CASE)
val uninitializedDirectoryTree = DirectoryTree(STORAGE_ROOT)
private var cachedDirectoryTree: MutableStateFlow<DirectoryTree?> = MutableStateFlow(uninitializedDirectoryTree)

var imageCache: LmImageCacheMgr = LmImageCacheMgr()

/**
 * ==========================
 * Various misc helpers
 * ==========================
 */


/**
 * Get cached DirectoryTree
 */
fun getDirectoryTree(): MutableStateFlow<DirectoryTree?> {
    return cachedDirectoryTree
}

/**
 * Cache a DirectoryTree
 */
fun cacheDirectoryTree(new: DirectoryTree?) {
    cachedDirectoryTree.value = new?: uninitializedDirectoryTree
}