package com.github.wonsim02.examples.rediscacheusinghash.cache

import com.github.wonsim02.examples.rediscacheusinghash.dto.WatchedVideosCounts
import org.springframework.data.redis.cache.CacheKeyPrefix
import org.springframework.data.redis.core.RedisTemplate
import java.io.Serializable
import java.time.Duration

class WatchedVideosCountsCache(
    cacheKeyPrefix: CacheKeyPrefix,
    private val redisTemplate: RedisTemplate<String, ByteArray>,
) {

    private val cacheDuration: Duration = Duration.ofHours(1L)
    private val prefix: String = cacheKeyPrefix.compute(CACHE_NAME)

    fun getByUserId(userId: Long): ByUserId {
        return ByUserId("$prefix$userId")
    }

    fun evictByUserId(userId: Long) {
        redisTemplate.delete("$prefix$userId")
    }

    inner class ByUserId(
        private val cacheKey: String,
    ) {

        fun get(playListIds: Collection<Long>): WatchedVideosCounts {
            if (playListIds.isEmpty()) return mapOf()

            return redisTemplate
                .opsForHash<Long, WatchedVideosCountsEntry>()
                .multiGet(cacheKey, playListIds)
                .asSequence()
                .filterNotNull()
                .associate { it.playListId to it.count }
        }

        fun put(watchHistoryCounts: WatchedVideosCounts) {
            if (watchHistoryCounts.isEmpty()) return
            val entries = watchHistoryCounts.mapValues { (playListId, count) ->
                WatchedVideosCountsEntry(playListId = playListId, count = count)
            }

            redisTemplate
                .opsForHash<Long, WatchedVideosCountsEntry>()
                .putAll(cacheKey, entries)
            redisTemplate.expire(cacheKey, cacheDuration)
        }
    }

    data class WatchedVideosCountsEntry(
        val playListId: Long,
        val count: Int,
    ) : Serializable

    companion object {

        const val CACHE_NAME = "WATCH_HISTORY_COUNTS"
    }
}
