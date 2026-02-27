package com.djoudini.player.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChannelDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var channelDao: ChannelDao
    private lateinit var epgDao: EpgDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        channelDao = db.channelDao()
        epgDao = db.epgDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertChannelsAndGetWithPrograms() = runBlocking {
        // Arrange
        val channels = listOf(
            ChannelEntity(id = 1, categoryId = 10, name = "Channel A", streamUrl = "url1", streamId = "s1", epgId = "e1", logo = null),
            ChannelEntity(id = 2, categoryId = 10, name = "Channel B", streamUrl = "url2", streamId = "s2", epgId = "e2", logo = null)
        )
        val programs = listOf(
            EpgProgramEntity(id=101, channelId = 1, title = "Program 1", startTime = 0, endTime = 100, description = null),
            EpgProgramEntity(id=102, channelId = 1, title = "Program 2", startTime = 100, endTime = 200, description = null)
        )
        
        // Act
        channelDao.insertChannels(channels)
        epgDao.insertPrograms(programs)
        
        val channelsWithPrograms = channelDao.getChannelsWithPrograms().first()

        // Assert
        assertEquals(2, channelsWithPrograms.size)
        
        val channelA = channelsWithPrograms.find { it.channel.id == 1 }
        assertNotNull(channelA)
        assertEquals("Channel A", channelA!!.channel.name)
        assertEquals(2, channelA.programs.size)
        assertEquals("Program 1", channelA.programs[0].title)

        val channelB = channelsWithPrograms.find { it.channel.id == 2 }
        assertNotNull(channelB)
        assertTrue(channelB!!.programs.isEmpty())
    }
}
