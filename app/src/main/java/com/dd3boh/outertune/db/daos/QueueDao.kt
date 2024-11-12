package com.dd3boh.outertune.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.dd3boh.outertune.db.entities.QueueEntity
import com.dd3boh.outertune.db.entities.QueueSongMap
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.models.MultiQueueObject
import com.dd3boh.outertune.models.QueueBoard
import com.dd3boh.outertune.models.toMediaMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock

@Dao
interface QueueDao {
    // region Gets
    @Query("SELECT * from queue ORDER BY `index`")
    fun getAllQueues(): Flow<List<QueueEntity>>

    @Transaction
    @Query("""
        SELECT song.* 
        FROM queue_song_map 
        JOIN song ON queue_song_map.songId = song.id 
        WHERE queueId = :queueId AND shuffled = 1
    """)
    fun getQueueSongs(queueId: Long): Flow<List<Song>>

    @Transaction
    @Query("""
        SELECT song.* 
        FROM queue_song_map 
        JOIN song ON queue_song_map.songId = song.id 
        WHERE queueId = :queueId AND shuffled = 0
    """)
    fun getQueueSongsUnshuffled(queueId: Long): Flow<List<Song>>

    @Query("SELECT id FROM queue")
    fun getAllQueueIds(): List<Long>

    fun readQueue(): List<MultiQueueObject> {
        val resultQueues = ArrayList<MultiQueueObject>()
        val queues = runBlocking { getAllQueues().first() }

        queues.forEach { queue ->
            val shuffledSongs = runBlocking { getQueueSongs(queue.id).first() }
            val unshuffledSongs = runBlocking { getQueueSongsUnshuffled(queue.id).first() }

            resultQueues.add(
                MultiQueueObject(
                    id = queue.id,
                    title = queue.title,
                    queue = shuffledSongs.map { it.toMediaMetadata() }.toMutableList(),
                    unShuffled = unshuffledSongs.map { it.toMediaMetadata() }.toMutableList(),
                    shuffled = queue.shuffled,
                    queuePos = queue.queuePos,
                    index = queue.index
                )
            )
        }

        return resultQueues
    }
    // endregion

    // region Inserts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(queue: QueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(queueSong: QueueSongMap)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertQueues(queues: List<QueueEntity>)
    // endregion

    // region Updates
    @Update
    fun update(queue: QueueEntity)

    @Upsert
    fun updateQueues(queues: List<QueueEntity>)

    @Transaction
    fun updateQueue(mq: MultiQueueObject) {
        update(
            QueueEntity(
                id = mq.id,
                title = mq.title,
                shuffled = mq.shuffled,
                queuePos = mq.queuePos,
                index = mq.index,
                playlistId = mq.playlistId
            )
        )
    }

    @Transaction
    fun updateAllQueues(mqs: List<MultiQueueObject>) {
        CoroutineScope(Dispatchers.IO).launch {
            QueueBoard.mutex.withLock { // possible ConcurrentModificationException
                mqs.forEach { updateQueue(it) }
            }
        }
    }
    // endregion

    // region Deletes
    @Delete
    fun delete(mq: QueueEntity)

    @Query("DELETE FROM queue")
    fun deleteAllQueues()

    @Query("DELETE FROM queue_song_map WHERE queueId = :id")
    fun deleteAllQueueSongs(id: Long)

    @Query("DELETE FROM queue WHERE id IN (:queueIds)")
    fun deleteQueuesByIds(queueIds: List<String>)

    @Query("DELETE FROM queue WHERE id = :id")
    fun deleteQueue(id: Long)
    // endregion
}
