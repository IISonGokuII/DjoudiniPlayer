package com.djoudini.player.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.djoudini.player.data.local.ChannelDao
import com.djoudini.player.data.local.ChannelEntity
import com.djoudini.player.data.local.PreferencesManager
import com.djoudini.player.data.remote.XtreamApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import retrofit2.Response

// Define a generic stream class as the API response is not a direct list
data class XtreamStream(
    @SerializedName("stream_id")
    val streamId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("stream_icon")
    val streamIcon: String?,
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("epg_channel_id")
    val epgChannelId: String?
)

@HiltWorker
class XtreamDataSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val xtreamApi: XtreamApi,
    private val channelDao: ChannelDao,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("XtreamDataSyncWorker", "Starting Xtream data sync...")
        try {
            val account = preferencesManager.getAccountInfo()
            val server = account["server"] ?: return@withContext Result.failure()
            val user = account["user"] ?: return@withContext Result.failure()
            val pass = account["pass"] ?: return@withContext Result.failure()
            
            val selectedLiveCategories = preferencesManager.getSelectedLiveCategories()
            Log.d("XtreamDataSyncWorker", "Syncing Live TV for categories: $selectedLiveCategories")

            if (selectedLiveCategories.isEmpty()) {
                Log.d("XtreamDataSyncWorker", "No live categories selected, sync complete.")
                return@withContext Result.success()
            }
            
            // Note: For VOD/Series, you would have separate getVodStreams, getSeriesStreams calls
            val allChannels = mutableListOf<ChannelEntity>()

            selectedLiveCategories.forEach { categoryId ->
                val url = "$server/player_api.php?username=$user&password=$pass&action=get_live_streams&category_id=$categoryId"
                try {
                    val response = xtreamApi.getLiveCategories(url) // Re-using this for now; needs a proper response model
                    if (response.isSuccessful && response.body() != null) {
                         // This is a placeholder as the API response is not a direct list
                         // We will simulate a successful response with one channel for each category
                         val channel = ChannelEntity(
                            categoryId = categoryId.toLongOrNull() ?: 0,
                            name = "Channel for Cat $categoryId",
                            logo = null,
                            streamUrl = "http://dummy.stream.url/live.m3u8",
                            streamId = "stream_$categoryId",
                            epgId = "epg_$categoryId"
                        )
                        allChannels.add(channel)
                    } else {
                        Log.e("XtreamDataSyncWorker", "API error for category $categoryId: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                     Log.e("XtreamDataSyncWorker", "Exception syncing category $categoryId", e)
                }
            }
            
            if (allChannels.isNotEmpty()) {
                channelDao.insertChannels(allChannels)
                Log.d("XtreamDataSyncWorker", "${allChannels.size} channels saved to database.")
            }

            Log.d("XtreamDataSyncWorker", "Sync finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("XtreamDataSyncWorker", "Sync failed with exception", e)
            Result.failure()
        }
    }
}
