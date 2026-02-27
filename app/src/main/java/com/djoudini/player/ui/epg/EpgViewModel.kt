package com.djoudini.player.ui.epg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.player.data.local.ChannelDao
import com.djoudini.player.data.local.ChannelEntity
import com.djoudini.player.data.local.EpgDao
import com.djoudini.player.data.local.EpgProgramEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EpgState(
    val channels: List<ChannelEntity> = emptyList(),
    val programsByChannel: Map<Long, List<EpgProgramEntity>> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class EpgViewModel @Inject constructor(
    private val channelDao: ChannelDao,
    private val epgDao: EpgDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpgState())
    val uiState: StateFlow<EpgState> = _uiState.asStateFlow()

    init {
        loadEpg()
    }

    private fun loadEpg() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                channelDao.getAllChannels().collect { channels ->
                    val currentTime = System.currentTimeMillis()
                    val programsMap = mutableMapOf<Long, List<EpgProgramEntity>>()
                    
                    channels.forEach { channel ->
                        try {
                            val programs = epgDao.getUpcomingPrograms(channel.id, currentTime).firstOrNull() ?: emptyList()
                            programsMap[channel.id] = programs
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    _uiState.value = EpgState(
                        channels = channels,
                        programsByChannel = programsMap,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = EpgState(isLoading = false)
            }
        }
    }
}
