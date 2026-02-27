package com.djoudini.player.ui.epg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.player.data.local.ChannelDao
import com.djoudini.player.data.local.ChannelWithPrograms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class EpgState(
    val channelsWithPrograms: List<ChannelWithPrograms> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class EpgViewModel @Inject constructor(
    private val channelDao: ChannelDao
) : ViewModel() {

    val uiState: StateFlow<EpgState> = channelDao.getChannelsWithPrograms()
        .map { EpgState(channelsWithPrograms = it, isLoading = false) }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EpgState(isLoading = true)
        )
}
