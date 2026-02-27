package com.djoudini.player.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector

@UnstableApi
object ExoPlayerFactory {

    /**
     * Creates a highly tuned ExoPlayer instance with User-Agent spoofing and custom buffer strategy.
     */
    fun buildPlayer(context: Context, userAgent: String? = null): ExoPlayer {

        // 1. User-Agent Spoofing
        // This is crucial for circumventing anti-bot protections on IPTV servers
        val defaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 VLC/3.0.18"
        val activeUserAgent = userAgent ?: defaultUserAgent
        
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(activeUserAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        // 2. Custom LoadControl (Buffering Strategy)
        // Highly aggressive for live TV without stuttering
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,   // Min buffer before starting (e.g. 50s)
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,   // Max buffer (e.g. 50s)
                2500,  // Buffer needed before playback starts
                5000   // Buffer needed after rebuffer
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        // 3. Track Selection (Automatic AFR and Quality Adaptation)
        val trackSelector = DefaultTrackSelector(context)
        val trackSelectorParameters = trackSelector.buildUponParameters()
            .setMaxVideoSizeSd()
            .build()
        trackSelector.setParameters(trackSelectorParameters)

        // 4. Constructing the Player
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                playWhenReady = true // Auto-play
            }
    }
}
