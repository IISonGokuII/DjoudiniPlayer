package com.djoudini.player.ui.epg

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.djoudini.player.data.local.ChannelEntity
import com.djoudini.player.data.local.EpgProgramEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EpgScreen(
    viewModel: EpgViewModel = hiltViewModel(),
    onChannelClick: (ChannelEntity) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(16.dp)
    ) {
        Text(
            text = "Programmzeitschrift (EPG)",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (state.channels.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No channels found.\n\nThe background sync might still be in progress, or no Live TV categories were selected.",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.channels) { channel ->
                    EpgChannelRow(
                        channel = channel,
                        programs = state.programsByChannel[channel.id] ?: emptyList(),
                        onChannelClick = { onChannelClick(channel) }
                    )
                }
            }
        }
    }
}

@Composable
fun EpgChannelRow(
    channel: ChannelEntity,
    programs: List<EpgProgramEntity>,
    onChannelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight()
                .clickable { onChannelClick() },
            color = Color.DarkGray,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(programs) { program ->
                EpgProgramItem(program = program)
            }
        }
    }
}

@Composable
fun EpgProgramItem(program: EpgProgramEntity) {
    var isFocused by remember { mutableStateOf(false) }
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val durationMin = ((program.endTime - program.startTime) / (1000 * 60)).toInt()
    
    val itemWidth = durationMin.coerceAtLeast(50) * 3

    Surface(
        modifier = Modifier
            .width(itemWidth.dp)
            .fillMaxHeight()
            .onFocusChanged { focusState -> isFocused = focusState.isFocused }
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .focusable()
            .clickable { /* Show program details or catch-up */ },
        color = if (isFocused) Color.Gray.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = program.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${timeFormat.format(Date(program.startTime))} - ${timeFormat.format(Date(program.endTime))}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.LightGray
            )
        }
    }
}
