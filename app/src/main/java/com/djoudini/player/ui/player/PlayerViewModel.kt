package com.djoudini.player.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.djoudini.player.data.local.WatchProgressDao
import com.djoudini.player.data.local.WatchProgressEntity
import com.djoudini.player.data.remote.TraktApi
import com.djoudini.player.data.remote.TraktIds
import com.djoudini.player.data.remote.TraktMovie
import com.djoudini.player.data.remote.TraktScrobbleRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val watchProgressDao: WatchProgressDao,
    private val traktApi: TraktApi
) : ViewModel() {

    // Dummy user tokens (Would normally come from Auth Manager/DataStore)
    private val traktToken = "Bearer DUMMY_TOKEN"
    private val traktApiKey = "DUMMY_API_KEY"

    fun saveProgressLocal(streamId: String, progressMs: Long, durationMs: Long, type: String = "VOD") {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = WatchProgressEntity(
                streamId = streamId,
                type = type,
                progressMs = progressMs,
                durationMs = durationMs,
                lastWatched = System.currentTimeMillis()
            )
            watchProgressDao.saveProgress(progress)
        }
    }

    suspend fun getInitialProgress(streamId: String): Long {
        return withContext(Dispatchers.IO) {
            val progress = watchProgressDao.getProgressSync(streamId)
            progress?.progressMs ?: 0L
        }
    }

    // Trakt Integration: Start playing
    fun startTraktScrobble(tmdbId: Int, progressPercentage: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = TraktScrobbleRequest(
                    movie = TraktMovie(title = "Dummy", year = null, ids = TraktIds(tmdb = tmdbId)),
                    progress = progressPercentage
                )
                traktApi.startScrobble(traktToken, "2", traktApiKey, request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Trakt Integration: Pause/Stop
    fun stopTraktScrobble(tmdbId: Int, progressPercentage: Float, isComplete: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = TraktScrobbleRequest(
                    movie = TraktMovie(title = "Dummy", year = null, ids = TraktIds(tmdb = tmdbId)),
                    progress = progressPercentage
                )
                if (isComplete || progressPercentage > 90f) {
                    traktApi.stopScrobble(traktToken, "2", traktApiKey, request)
                } else {
                    traktApi.pauseScrobble(traktToken, "2", traktApiKey, request)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
