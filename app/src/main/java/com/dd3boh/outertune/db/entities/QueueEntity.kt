package com.dd3boh.outertune.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dd3boh.outertune.utils.RandomStringUtil

@Entity(tableName = "queue")
data class QueueEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "title", defaultValue = "")
    var title: String,
    var shuffled: Boolean = false,
    var queuePos: Int = -1, // position of current song
    @ColumnInfo(name = "index", defaultValue = "0")
    val index: Int, // order of queue
    val playlistId: String? = null,
) {
    companion object {
        fun generateQueueId(): Long {
            // Generate a random alphanumeric string, then convert to Long if possible.
            return RandomStringUtil.random(8, false, true).toLongOrNull() ?: 0L
        }
    }
}