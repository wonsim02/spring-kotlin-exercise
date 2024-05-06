package com.github.wonsim02.examples.rediscacheusinghash.service

import com.github.wonsim02.examples.rediscacheusinghash.cache.WatchHistoryCountsCache
import com.github.wonsim02.examples.rediscacheusinghash.dto.WatchHistoryCounts
import com.github.wonsim02.examples.rediscacheusinghash.model.WatchHistory
import com.github.wonsim02.examples.rediscacheusinghash.repository.WatchHistoryRepository
import org.springframework.stereotype.Service

@Service
class WatchHistoryService(
    private val userService: UserService,
    private val videoService: VideoService,
    private val watchHistoryCountsCache: WatchHistoryCountsCache?,
    private val watchHistoryRepository: WatchHistoryRepository,
) {

    @Throws(
        InvalidUserIdException::class,
        InvalidVideoIdException::class,
    )
    fun create(
        userId: Long,
        videoId: Long,
    ): WatchHistory {
        try {
            userService.getUser(userId)
        } catch (t: UserService.UserNotFoundException) {
            throw InvalidUserIdException(userId).initCause(t)
        }

        if (!videoService.existsByVideoId(videoId)) {
            throw InvalidVideoIdException(videoId)
        }

        watchHistoryCountsCache?.evictByUserId(userId)
        return watchHistoryRepository.create(userId, videoId)
    }

    @Throws(InvalidUserIdException::class)
    fun countByUserIdAndVideoIds(
        userId: Long,
        videoIds: Collection<Long>,
    ): WatchHistoryCounts {
        try {
            userService.getUser(userId)
        } catch (t: UserService.UserNotFoundException) {
            throw InvalidUserIdException(userId).initCause(t)
        }
        if (videoIds.isEmpty()) return mapOf()

        val cacheByUserId = watchHistoryCountsCache?.getByUserId(userId)
        val cached = cacheByUserId?.get(videoIds) ?: mapOf()
        val notCachedIds = videoIds.toSet() - cached.keys
        if (notCachedIds.isEmpty()) return cached

        val fromRepository = watchHistoryRepository.countByUserIdAndVideoIds(
            userId = userId,
            videoIds = notCachedIds,
        )
        val videoIdsWithZeroCounts = notCachedIds - fromRepository.keys
        val toBePutInCache = fromRepository + videoIdsWithZeroCounts.associateWith { 0L }
        cacheByUserId?.put(toBePutInCache)

        return cached + fromRepository
    }

    class InvalidUserIdException(userId: Long) : RuntimeException("Invalid userId=$userId given.")

    class InvalidVideoIdException(videoId: Long) : RuntimeException("Invalid videoId=$videoId given.")
}
