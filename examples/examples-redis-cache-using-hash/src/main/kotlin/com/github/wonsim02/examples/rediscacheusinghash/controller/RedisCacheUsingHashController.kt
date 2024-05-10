package com.github.wonsim02.examples.rediscacheusinghash.controller

import com.github.wonsim02.examples.rediscacheusinghash.config.UserIdHeaderParameterConfiguration
import com.github.wonsim02.examples.rediscacheusinghash.dto.PlayListWithDetails
import com.github.wonsim02.examples.rediscacheusinghash.model.WatchHistory
import com.github.wonsim02.examples.rediscacheusinghash.service.PlayListService
import com.github.wonsim02.examples.rediscacheusinghash.service.WatchHistoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RedisCacheUsingHashController(
    private val playListService: PlayListService,
    private val userRequestContext: UserIdHeaderParameterConfiguration.UserRequestContext,
    private val watchHistoryService: WatchHistoryService,
) {

    @GetMapping("/playlists")
    fun listPlayLists(
        @RequestParam("cursor", required = false) cursor: Long?,
        @RequestParam("limit", required = true) limit: Long,
    ): ResponseEntity<ListPlayListsResponse> {
        val userId = userRequestContext.userId
        val playLists = playListService.list(userId, cursor, limit)

        return ResponseEntity.ok(ListPlayListsResponse(playLists))
    }

    @PutMapping("/watch-histories")
    fun createWatchHistory(
        @RequestParam("videoId") videoId: Long,
    ): ResponseEntity<WatchHistory> {
        val userId = userRequestContext.userId
        val watchHistory = watchHistoryService.create(userId, videoId)

        return ResponseEntity.ok(watchHistory)
    }

    data class ListPlayListsResponse(val playLists: List<PlayListWithDetails>)
}
