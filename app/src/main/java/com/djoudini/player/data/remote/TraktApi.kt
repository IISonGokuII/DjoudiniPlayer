package com.djoudini.player.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class TraktScrobbleRequest(
    val movie: TraktMovie? = null,
    val episode: TraktEpisode? = null,
    val progress: Float,
    val app_version: String = "1.0",
    val app_date: String = "2026-02-27"
)

data class TraktMovie(
    val title: String,
    val year: Int?,
    val ids: TraktIds
)

data class TraktEpisode(
    val season: Int,
    val number: Int,
    val title: String?,
    val ids: TraktIds
)

data class TraktIds(
    val trakt: Int? = null,
    val imdb: String? = null,
    val tmdb: Int? = null
)

interface TraktApi {

    @POST("scrobble/start")
    suspend fun startScrobble(
        @Header("Authorization") token: String,
        @Header("trakt-api-version") apiVersion: String = "2",
        @Header("trakt-api-key") apiKey: String,
        @Body request: TraktScrobbleRequest
    ): Response<Unit>

    @POST("scrobble/pause")
    suspend fun pauseScrobble(
        @Header("Authorization") token: String,
        @Header("trakt-api-version") apiVersion: String = "2",
        @Header("trakt-api-key") apiKey: String,
        @Body request: TraktScrobbleRequest
    ): Response<Unit>

    @POST("scrobble/stop")
    suspend fun stopScrobble(
        @Header("Authorization") token: String,
        @Header("trakt-api-version") apiVersion: String = "2",
        @Header("trakt-api-key") apiKey: String,
        @Body request: TraktScrobbleRequest
    ): Response<Unit>
}
