package com.djoudini.player.ui.epg

import app.cash.turbine.test
import com.djoudini.player.data.local.ChannelDao
import com.djoudini.player.data.local.ChannelEntity
import com.djoudini.player.data.local.ChannelWithPrograms
import com.djoudini.player.ui.onboarding.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class EpgViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val channelDao: ChannelDao = mock()
    
    @Test
    fun `uiState - when dao emits data, uiState is updated correctly`() = runTest {
        // Arrange
        val testData = listOf(
            ChannelWithPrograms(ChannelEntity(id=1, categoryId=1, name="Channel 1", logo=null, streamUrl="", streamId="1", epgId="1"), programs = emptyList())
        )
        whenever(channelDao.getChannelsWithPrograms()) doReturn flowOf(testData)
        
        // Act
        val viewModel = EpgViewModel(channelDao)

        // Assert
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState.isLoading)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals(1, successState.channelsWithPrograms.size)
            assertEquals("Channel 1", successState.channelsWithPrograms[0].channel.name)
        }
    }

    @Test
    fun `uiState - when dao emits empty list, uiState shows empty`() = runTest {
        // Arrange
        whenever(channelDao.getChannelsWithPrograms()) doReturn flowOf(emptyList())
        
        // Act
        val viewModel = EpgViewModel(channelDao)

        // Assert
        viewModel.uiState.test {
            awaitItem() // Initial loading state
            val emptyState = awaitItem()
            assertFalse(emptyState.isLoading)
            assertTrue(emptyState.channelsWithPrograms.isEmpty())
        }
    }
}
