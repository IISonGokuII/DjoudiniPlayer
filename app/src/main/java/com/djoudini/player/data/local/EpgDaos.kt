package com.djoudini.player.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class ChannelWithPrograms(
    @androidx.room.Embedded
    val channel: ChannelEntity,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "channel_id"
    )
    val programs: List<EpgProgramEntity>
)

@Dao
interface EpgDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE end_time < :currentTime")
    suspend fun deleteOldPrograms(currentTime: Long)
}

@Dao
interface ChannelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Transaction
    @Query("SELECT * FROM channels")
    fun getChannelsWithPrograms(): Flow<List<ChannelWithPrograms>>
}
