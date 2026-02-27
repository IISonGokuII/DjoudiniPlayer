package com.djoudini.player.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgDao {

    @Query("""
        SELECT * FROM epg_programs 
        WHERE channel_id = :channelId 
        AND end_time > :currentTime 
        ORDER BY start_time ASC
    """)
    fun getUpcomingPrograms(channelId: Long, currentTime: Long): Flow<List<EpgProgramEntity>>

    @Query("""
        SELECT * FROM epg_programs 
        WHERE channel_id = :channelId 
        AND start_time <= :currentTime 
        AND end_time >= :currentTime 
        LIMIT 1
    """)
    suspend fun getCurrentProgram(channelId: Long, currentTime: Long): EpgProgramEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE end_time < :currentTime")
    suspend fun deleteOldPrograms(currentTime: Long)
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE category_id = :categoryId")
    fun getChannelsByCategory(categoryId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels")
    fun getAllChannels(): Flow<List<ChannelEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)
}
