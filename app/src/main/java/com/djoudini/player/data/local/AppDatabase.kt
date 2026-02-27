package com.djoudini.player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlaylistEntity::class,
        CategoryEntity::class,
        ChannelEntity::class,
        VodEntity::class,
        EpgProgramEntity::class,
        WatchProgressEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchProgressDao(): WatchProgressDao
    abstract fun vodDao(): VodDao
    abstract fun epgDao(): EpgDao
    abstract fun channelDao(): ChannelDao
}
