package com.djoudini.player.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistSyncRepository @Inject constructor() {

    // Represents the sync progress from 0.0f (0%) to 1.0f (100%)
    private val _syncProgress = MutableStateFlow(0.0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Example dummy method to simulate chunked/memory-safe parsing with progress updates
    suspend fun syncPlaylist(url: String, categoriesToKeep: List<String>) {
        withContext(Dispatchers.IO) {
            _isSyncing.value = true
            _syncProgress.value = 0.0f

            try {
                // Simulate downloading and parsing in chunks (Memory-Safe)
                val totalChunks = 100
                for (chunk in 1..totalChunks) {
                    // Simulating heavy XML/M3U parse chunk logic here
                    delay(50) // Fake work

                    // Updating the progress cleanly over StateFlow
                    _syncProgress.value = chunk.toFloat() / totalChunks.toFloat()
                }
            } catch (e: Exception) {
                // Handle parsing errors, e.g., Network or format issues
                e.printStackTrace()
            } finally {
                _syncProgress.value = 1.0f
                _isSyncing.value = false
            }
        }
    }
}
