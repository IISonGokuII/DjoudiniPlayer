package com.djoudini.player.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

data class XtreamAuthResponse(
    val user_info: UserInfo?,
    val server_info: ServerInfo?
)

data class UserInfo(
    val username: String?,
    val exp_date: String?,
    val active_cons: String?,
    val max_connections: String?
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
    val category_id: String,
    val category_name: String,
    val parent_id: Int? = 0
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
}
