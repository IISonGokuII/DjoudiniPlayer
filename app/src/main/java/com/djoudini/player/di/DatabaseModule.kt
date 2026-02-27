package com.djoudini.player.di

import android.content.Context
import androidx.room.Room
import com.djoudini.player.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "djoudini_player.db"
        ).build()
    }

    @Provides
    fun provideWatchProgressDao(database: AppDatabase) = database.watchProgressDao()

    @Provides
    fun provideVodDao(database: AppDatabase) = database.vodDao()
}
