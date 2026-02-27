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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

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
            if (selectedLiveCategories.isEmpty()) {
                Log.d("XtreamDataSyncWorker", "No live categories selected, sync complete.")
                return@withContext Result.success()
            }
            
            val allChannels = mutableListOf<ChannelEntity>()

            // Parallel fetching of streams for each category
            val streamJobs = selectedLiveCategories.map { categoryId ->
                async {
                    val url = "$server/player_api.php?username=$user&password=$pass&action=get_live_streams&category_id=$categoryId"
                    try {
                        val response = xtreamApi.getLiveStreams(url)
                        if (response.isSuccessful) {
                            response.body()?.map { stream ->
                                ChannelEntity(
                                    categoryId = stream.categoryId.toLongOrNull() ?: 0L,
                                    name = stream.name,
                                    logo = stream.streamIcon,
                                    streamUrl = "$server/live/$user/$pass/${stream.streamId}.m3u8",
                                    streamId = stream.streamId.toString(),
                                    epgId = stream.epgChannelId
                                )
                            }
                        } else {
                            Log.e("XtreamDataSyncWorker", "API error for category $categoryId: ${response.code()}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("XtreamDataSyncWorker", "Exception syncing category $categoryId", e)
                        null
                    }
                }
            }
            
            // Collect all results
            streamJobs.awaitAll().forEach { channelList ->
                channelList?.let { allChannels.addAll(it) }
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
