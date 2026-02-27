package com.djoudini.player.ui.player

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.djoudini.player.player.ExoPlayerFactory
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    streamUrl: String = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
    streamId: String = "test_stream_1", // Maps to Trakt/Room
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1L) } // Avoid divide by zero

    // Load initial progress on startup
    LaunchedEffect(streamId) {
        val savedPosition = viewModel.getInitialProgress(streamId)
        currentPositionMs = savedPosition
        
        exoPlayer = ExoPlayerFactory.buildPlayer(context).apply {
            setMediaItem(MediaItem.fromUri(streamUrl))
            prepare()
            if (savedPosition > 0L) {
                seekTo(savedPosition)
            }
        }
    }

    // Trakt Tracker: Report to Trakt every 10 seconds while playing
    LaunchedEffect(exoPlayer) {
        while (true) {
            val player = exoPlayer ?: break
            if (player.isPlaying && player.duration > 0) {
                durationMs = player.duration
                currentPositionMs = player.currentPosition
                val progressPercentage = (currentPositionMs.toFloat() / durationMs.toFloat()) * 100f
                
                // Demo TMDB ID
                viewModel.startTraktScrobble(tmdbId = 12345, progressPercentage = progressPercentage)
            }
            delay(10000)
        }
    }

    // Manage Player Lifecycle and Save Progress locally
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val player = exoPlayer ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    player.pause()
                    val duration = player.duration
                    val position = player.currentPosition
                    if (duration > 0) {
                        viewModel.saveProgressLocal(streamId, position, duration)
                        val progressPercentage = (position.toFloat() / duration.toFloat()) * 100f
                        viewModel.stopTraktScrobble(tmdbId = 12345, progressPercentage = progressPercentage)
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    val duration = player.duration
                    val position = player.currentPosition
                    if (duration > 0) {
                        viewModel.saveProgressLocal(streamId, position, duration)
                    }
                    player.release()
                    exoPlayer = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer?.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // Keeping Screen On while video plays
                        keepScreenOn = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Multi-View Grid Support (Phase 4): Up to 4 players side-by-side.
 */
@Composable
fun MultiViewPlayerScreen(streamUrls: List<String>) {
    if (streamUrls.isEmpty()) return

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.weight(1f)) {
                PlayerScreen(streamUrl = streamUrls[0], streamId = "multi_1")
            }
            if (streamUrls.size > 1) {
                Box(modifier = Modifier.weight(1f)) {
                    PlayerScreen(streamUrl = streamUrls[1], streamId = "multi_2")
                }
            }
        }
        if (streamUrls.size > 2) {
            Row(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.weight(1f)) {
                    PlayerScreen(streamUrl = streamUrls[2], streamId = "multi_3")
                }
                if (streamUrls.size > 3) {
                    Box(modifier = Modifier.weight(1f)) {
                        PlayerScreen(streamUrl = streamUrls[3], streamId = "multi_4")
                    }
                }
            }
        }
    }
}
