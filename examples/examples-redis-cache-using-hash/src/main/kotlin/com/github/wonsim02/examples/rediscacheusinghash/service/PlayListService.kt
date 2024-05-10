package com.github.wonsim02.examples.rediscacheusinghash.service

import com.github.wonsim02.examples.rediscacheusinghash.cache.WatchedVideosCountsCache
import com.github.wonsim02.examples.rediscacheusinghash.dto.PlayListWithDetails
import com.github.wonsim02.examples.rediscacheusinghash.dto.WatchedVideosCounts
import com.github.wonsim02.examples.rediscacheusinghash.model.PlayList
import com.github.wonsim02.examples.rediscacheusinghash.repository.PlayListRepository
import org.springframework.stereotype.Service

@Service
class PlayListService(
    private val playListRepository: PlayListRepository,
    private val videoService: VideoService,
    private val watchHistoryService: WatchHistoryService,
    private val watchedVideosCountsCache: WatchedVideosCountsCache?,
) {

    @Throws(InvalidUserIdException::class)
    fun list(
        userId: Long,
        cursor: Long?,
        limit: Long,
    ): List<PlayListWithDetails> {
        val playLists = playListRepository.list(cursor, limit)
        if (playLists.isEmpty()) return listOf()

        val videoMap = videoService.fetch(videoIds = playLists.flatMap { it.videoIds })
        val watchedVideosCounts = getWatchedVideosCounts(userId, playLists)

        return playLists.map { PlayListWithDetails(it, videoMap, watchedVideosCounts) }
    }

    /**
     * 주어진 사용자 및 [PlayList]들에 대하여 [PlayListWithDetails.watchedVideosCount] 값을 구한다.
     * 만약 [WatchedVideosCountsCache] 빈을 주입받았으면 우선 캐시된 값을 활용한다.
     * 이후 `watchedVideosCount`가 캐시되지 않은 [PlayList]에 대한 [WatchedVideosCounts] 값을 계산하여 이를 캐시에 저장한다.
     * @return [WatchedVideosCounts] 캐시된 값과 새로 계산하여 캐시에 저장한 값 모두를 반환한다.
     * @throws InvalidUserIdException 주어진 [userId]가 유효하지 않음
     */
    @Throws(InvalidUserIdException::class)
    private fun getWatchedVideosCounts(
        userId: Long,
        playLists: List<PlayList>,
    ): WatchedVideosCounts {
        val cacheByUserId = watchedVideosCountsCache?.getByUserId(userId)
        val playListIds = playLists.mapTo(mutableSetOf()) { it.id }

        val cached = cacheByUserId?.get(playListIds) ?: mapOf()
        val notCachedIds = playListIds - cached.keys
        if (notCachedIds.isEmpty()) return cached

        val toBePutInCache = calculateWatchedVideosCounts(
            userId = userId,
            playLists = playLists.filter { notCachedIds.contains(it.id) },
        )
        cacheByUserId?.put(toBePutInCache)

        return cached + toBePutInCache
    }

    /**
     * 주어진 사용자 및 [PlayList]들에 대하여 [PlayListWithDetails.watchedVideosCount] 값을 계산한다.
     * @param userId 사용자의 ID
     * @param playLists [PlayListWithDetails.watchedVideosCount] 값을 계산할 [PlayList] 목록
     * @return [WatchedVideosCounts]
     * @throws InvalidUserIdException 주어진 [userId]가 유효하지 않음
     */
    @Throws(InvalidUserIdException::class)
    private fun calculateWatchedVideosCounts(
        userId: Long,
        playLists: List<PlayList>,
    ): WatchedVideosCounts {
        val allVideoIds = playLists.flatMap { it.videoIds }
        val watchHistoryCounts = try {
            watchHistoryService.countByUserIdAndVideoIds(
                userId = userId,
                videoIds = allVideoIds,
            )
        } catch (t: WatchHistoryService.InvalidUserIdException) {
            throw InvalidUserIdException(userId).initCause(t)
        }

        return playLists.associate { playList ->
            playList.id to playList
                .videoIds
                .count { videoId -> watchHistoryCounts[videoId]?.let { it > 0 } ?: false }
        }
    }

    class InvalidUserIdException(userId: Long) : RuntimeException("Invalid userId=$userId received.")
}
