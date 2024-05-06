package com.github.wonsim02.examples.rediscacheusinghash.service

import com.github.wonsim02.examples.rediscacheusinghash.dto.PlayListWithDetails
import com.github.wonsim02.examples.rediscacheusinghash.repository.PlayListRepository
import org.springframework.stereotype.Service

@Service
class PlayListService(
    private val playListRepository: PlayListRepository,
    private val videoService: VideoService,
    private val watchHistoryService: WatchHistoryService,
) {

    @Throws(InvalidUserIdException::class)
    fun list(
        userId: Long,
        cursor: Long?,
        limit: Long,
    ): List<PlayListWithDetails> {
        val playLists = playListRepository.list(cursor, limit)
        if (playLists.isEmpty()) return listOf()

        val allVideoIds = playLists.flatMap { it.videoIds }
        val videoMap = videoService.fetch(videoIds = allVideoIds)
        val watchHistoryCounts = try {
            watchHistoryService.countByUserIdAndVideoIds(
                userId = userId,
                videoIds = allVideoIds,
            )
        } catch (t: WatchHistoryService.InvalidUserIdException) {
            throw InvalidUserIdException(userId).initCause(t)
        }

        return playLists.map { PlayListWithDetails(it, videoMap, watchHistoryCounts) }
    }

    class InvalidUserIdException(userId: Long) : RuntimeException("Invalid userId=$userId recevied.")
}
