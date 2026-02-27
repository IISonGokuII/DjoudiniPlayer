package com.djoudini.player.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.djoudini.player.data.repository.PlaylistSyncRepository
import com.djoudini.player.ui.components.DashboardTile
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val syncRepository: PlaylistSyncRepository
) : ViewModel() {
    val syncProgress = syncRepository.syncProgress
    val isSyncing = syncRepository.isSyncing
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val syncProgress by viewModel.syncProgress.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        HeaderSection(expirationDate = "2026-12-31")

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Optimized for TV/Mobile landscape
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardTile(
                    title = "Live TV",
                    icon = Icons.Default.LiveTv,
                    progress = if (isSyncing) syncProgress else 0f,
                    isSyncing = isSyncing,
                    onClick = { /* Navigate to Live */ }
                )
            }
            item {
                DashboardTile(
                    title = "VOD",
                    icon = Icons.Default.Movie,
                    onClick = { /* Navigate to VOD */ }
                )
            }
            item {
                DashboardTile(
                    title = "Series",
                    icon = Icons.Default.Tv,
                    onClick = { /* Navigate to Series */ }
                )
            }
            item {
                DashboardTile(
                    title = "Settings",
                    icon = Icons.Default.Settings,
                    onClick = { /* Navigate to Settings */ }
                )
            }
        }
    }
}

@Composable
fun HeaderSection(expirationDate: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "DJOUDINI PLAYER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Premium IPTV Experience",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Ablaufdatum:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
                Text(
                    text = expirationDate,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
