/*
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.utils

import android.content.pm.PackageManager
import com.dd3boh.outertune.ui.screens.settings.LibraryFilter
import com.dd3boh.outertune.ui.screens.settings.NavigationTab

fun reportException(throwable: Throwable) {
    throwable.printStackTrace()
}

/**
 * Converts the enable tabs list (string) to NavigationTab
 *
 * @param str Encoded string
 */
fun decodeTabString(str: String): List<NavigationTab> {
    return str.toCharArray().map {
        when (it) {
            'H' -> NavigationTab.HOME
            'S' -> NavigationTab.SONG
            'F' -> NavigationTab.FOLDERS
            'A' -> NavigationTab.ARTIST
            'B' -> NavigationTab.ALBUM
            'L' -> NavigationTab.PLAYLIST
            'M' -> NavigationTab.LIBRARY
            else -> {
                NavigationTab.NULL // this case should never happen. Just shut the compiler up
            }
        }
    }
}

/**
 * Converts the NavigationTab tabs list to string
 *
 * @param list Decoded NavigationTab list
 */
fun encodeTabString(list: List<NavigationTab>): String {
    var encoded = ""
    list.subList(0, list.indexOf(NavigationTab.NULL)).forEach {
        encoded += when (it) {
            NavigationTab.HOME -> "H"
            NavigationTab.SONG -> "S"
            NavigationTab.FOLDERS -> "F"
            NavigationTab.ARTIST -> "A"
            NavigationTab.ALBUM -> "B"
            NavigationTab.PLAYLIST -> "L"
            NavigationTab.LIBRARY -> "M"
            else -> { "" }
        }
    }

    return encoded
}

/**
 * Converts the enable filters list (string) to LibraryFilter
 *
 * @param str Encoded string
 */
fun decodeFilterString(str: String): List<LibraryFilter> {
    return str.toCharArray().map {
        when (it) {
            'A' -> LibraryFilter.ALBUMS
            'R' -> LibraryFilter.ARTISTS
            'P' -> LibraryFilter.PLAYLISTS
            'S' -> LibraryFilter.SONGS
            'F' -> LibraryFilter.FOLDERS
            'L' -> LibraryFilter.ALL
            else -> LibraryFilter.NULL
        }
    }
}

/**
 * Converts the LibraryFilter filters list to string
 *
 * @param list Decoded LibraryFilter list
 */
fun encodeFilterString(list: List<LibraryFilter>): String {
    var encoded = ""
    list.subList(0, list.indexOf(LibraryFilter.NULL)).forEach {
        encoded += when (it) {
            LibraryFilter.ALBUMS -> "A"
            LibraryFilter.ARTISTS -> "R"
            LibraryFilter.PLAYLISTS -> "P"
            LibraryFilter.SONGS -> "S"
            LibraryFilter.FOLDERS -> "F"
            LibraryFilter.ALL -> "L"
            else -> { "" }
        }
    }
    return encoded
}

/**
 * Check if a package with the specified package name is installed
 */
fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}