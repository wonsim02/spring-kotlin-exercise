package com.github.wonsim02.examples.rediscacheusinghash.service

import com.github.wonsim02.examples.rediscacheusinghash.cache.WatchedVideosCountsCache
import com.github.wonsim02.examples.rediscacheusinghash.dto.WatchHistoryCounts
import com.github.wonsim02.examples.rediscacheusinghash.model.WatchHistory
import com.github.wonsim02.examples.rediscacheusinghash.repository.WatchHistoryRepository
import org.springframework.stereotype.Service

@Service
class WatchHistoryService(
    private val userService: UserService,
    private val videoService: VideoService,
    private val watchedVideosCountsCache: WatchedVideosCountsCache?,
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

        // Video 하나에 대한 count가 변경되면 해당 video를 포함하는 PlayList의 watchedVideosCount 전부가 변경될 수 있으므로
        // 해당 사용자의 모든 watchedVideosCount 캐시를 삭제한다.
        watchedVideosCountsCache?.evictByUserId(userId)
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

        return watchHistoryRepository.countByUserIdAndVideoIds(
            userId = userId,
            videoIds = videoIds,
        )
    }

    class InvalidUserIdException(userId: Long) : RuntimeException("Invalid userId=$userId given.")

    class InvalidVideoIdException(videoId: Long) : RuntimeException("Invalid videoId=$videoId given.")
}
