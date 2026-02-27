package com.djoudini.player.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchProgressDao {

    @Query("SELECT * FROM watch_progress WHERE stream_id = :streamId LIMIT 1")
    fun getProgressByStreamId(streamId: String): Flow<WatchProgressEntity?>

    @Query("SELECT * FROM watch_progress WHERE stream_id = :streamId LIMIT 1")
    suspend fun getProgressSync(streamId: String): WatchProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: WatchProgressEntity)

    @Query("DELETE FROM watch_progress WHERE stream_id = :streamId")
    suspend fun deleteProgress(streamId: String)
}

@Dao
interface VodDao {
    @Query("SELECT * FROM vods WHERE category_id = :categoryId")
    fun getVodsByCategory(categoryId: Long): Flow<List<VodEntity>>
}
