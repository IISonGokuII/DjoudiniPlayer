package com.djoudini.player.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url
import com.google.gson.annotations.SerializedName

data class XtreamAuthResponse(
    @SerializedName("user_info")
    val userInfo: UserInfo?,
    @SerializedName("server_info")
    val serverInfo: ServerInfo?
)

data class UserInfo(
    val username: String?,
    @SerializedName("exp_date")
    val expDate: String?,
    @SerializedName("active_cons")
    val activeCons: String?,
    @SerializedName("max_connections")
    val maxConnections: String?
)

data class ServerInfo(
    val url: String?,
    val port: String?,
    val https_port: String?,
    val server_protocol: String?,
    val rtmp_port: String?,
    val timezone: String?
)

data class XtreamCategory(
    @SerializedName("category_id")
    val category_id: String,
    @SerializedName("category_name")
    val category_name: String,
    val parent_id: Int? = 0
)

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

interface XtreamApi {
    @GET
    suspend fun authenticate(@Url url: String): Response<XtreamAuthResponse>

    @GET
    suspend fun getLiveCategories(@Url url: String): Response<List<XtreamCategory>>

    @GET
    suspend fun getVodCategories(@Url url: String): Response<List<XtreamCategory>>

    @GET
    suspend fun getSeriesCategories(@Url url: String): Response<List<XtreamCategory>>

    @GET
    suspend fun getLiveStreams(@Url url: String): Response<List<XtreamStream>>
}
