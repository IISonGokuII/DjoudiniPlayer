package com.djoudini.player.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.ColumnInfo

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val type: String, // "M3U" or "XTREAM"
    @ColumnInfo(name = "expiration_date") val expirationDate: Long?, // Unix timestamp
    @ColumnInfo(name = "last_updated") val lastUpdated: Long
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlist_id"]), Index(value = ["type", "name"])]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "playlist_id") val playlistId: Long,
    val name: String,
    val type: String // "LIVE", "VOD", "SERIES"
)

@Entity(
    tableName = "channels",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["category_id"]), Index(value = ["stream_id"])]
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val name: String,
    val logo: String?,
    @ColumnInfo(name = "stream_url") val streamUrl: String,
    @ColumnInfo(name = "stream_id") val streamId: String?, // Xtream stream ID or tvg-id
    val epgId: String? // tvg-id mapping
)

@Entity(
    tableName = "vods",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["category_id"]), Index(value = ["stream_id"])]
)
data class VodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val name: String,
    val logo: String?,
    @ColumnInfo(name = "stream_url") val streamUrl: String,
    @ColumnInfo(name = "stream_id") val streamId: String?,
    val rating: Float?,
    val releaseDate: String?
)

@Entity(
    tableName = "epg_programs",
    foreignKeys = [
        ForeignKey(
            entity = ChannelEntity::class,
            parentColumns = ["id"],
            childColumns = ["channel_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["channel_id"]), Index(value = ["start_time", "end_time"])]
)
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "channel_id") val channelId: Long,
    val title: String,
    val description: String?,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long
)

@Entity(
    tableName = "watch_progress",
    indices = [Index(value = ["stream_id"], unique = true)]
)
data class WatchProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "stream_id") val streamId: String, // Maps to VodEntity or ChannelEntity stream_id
    val type: String, // "VOD" or "SERIES_EPISODE"
    @ColumnInfo(name = "progress_ms") val progressMs: Long,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "last_watched") val lastWatched: Long
)
