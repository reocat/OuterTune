package com.dd3boh.outertune.lyrics

import org.akanework.gramophone.logic.utils.SemanticLyrics
import timber.log.Timber
import com.maxrave.lyricsproviders.models.lyrics.Lyrics as ClientLyrics

fun convertToSemanticLyrics(lyrics: ClientLyrics?): SemanticLyrics? {
    Timber.d("Trying to convert ClientLyrics to SemanticLyrics, nya~")
    val clientLyricsBody = lyrics?.lyrics ?: run {
        Timber.w("Cannot convert, input ClientLyrics or its body is null. ono")
        return null
    }
    val clientLines = clientLyricsBody.lines
    if (clientLines.isNullOrEmpty()) {
        Timber.w("Cannot convert, lyrics lines are null or empty. ;w;")
        return null
    }

    Timber.d("Found ${clientLines.size} lines to convert. Sync type: ${clientLyricsBody.syncType}. uwu")
    val lines = mutableListOf<SemanticLyrics.LyricLine>()
    val isUnsynced = clientLyricsBody.syncType == "UNSYNCED"

    clientLines.forEachIndexed { index, line ->
        val startTime = if (isUnsynced) 0uL else line.startTimeMs.toULong()

        val endTime = if (isUnsynced || index + 1 >= clientLines.size) {
            startTime + 5000uL
        } else {
            clientLines[index + 1].startTimeMs.toULong()
        }

        lines.add(
            SemanticLyrics.LyricLine(
                text = line.words,
                start = startTime,
                end = endTime,
                words = null,
                speaker = null,
                isTranslated = false
            )
        )
    }

    return if (isUnsynced) {
        Timber.d("Returning UnsyncedLyrics! :3")
        SemanticLyrics.UnsyncedLyrics(lines.map { it.text to null })
    } else {
        Timber.d("Returning SyncedLyrics! OwO")
        SemanticLyrics.SyncedLyrics(lines)
    }
}