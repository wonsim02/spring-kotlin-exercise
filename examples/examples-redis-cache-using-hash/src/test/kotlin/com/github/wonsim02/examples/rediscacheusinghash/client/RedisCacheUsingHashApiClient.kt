package com.github.wonsim02.examples.rediscacheusinghash.client

import com.github.wonsim02.examples.rediscacheusinghash.config.UserIdHeaderParameterConfiguration.Companion.USER_ID_HEADER
import com.github.wonsim02.examples.rediscacheusinghash.controller.RedisCacheUsingHashController
import com.github.wonsim02.examples.rediscacheusinghash.model.WatchHistory
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * [RedisCacheUsingHashController]의 API에 대한 retrofit 클라이언트.
 */
interface RedisCacheUsingHashApiClient {

    @GET("/playlists")
    fun listPlayLists(
        @Header(USER_ID_HEADER) userId: Long,
        @Query("cursor") cursor: Long?,
        @Query("limit") limit: Long,
    ): Call<RedisCacheUsingHashController.ListPlayListsResponse>

    @PUT("/watch-histories")
    fun createWatchHistory(
        @Header(USER_ID_HEADER) userId: Long,
        @Query("videoId") videoId: Long,
    ): Call<WatchHistory>
}
