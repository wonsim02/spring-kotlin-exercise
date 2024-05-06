package com.github.wonsim02.examples.rediscacheusinghash.cache

import com.github.wonsim02.examples.rediscacheusinghash.dto.WatchHistoryCounts
import org.springframework.data.redis.cache.CacheKeyPrefix
import org.springframework.data.redis.core.RedisTemplate
import java.io.Serializable
import java.time.Duration

class WatchHistoryCountsCache(
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

        fun get(videoIds: Collection<Long>): WatchHistoryCounts {
            if (videoIds.isEmpty()) return mapOf()

            return redisTemplate
                .opsForHash<Long, WatchHistoryCountsEntry>()
                .multiGet(cacheKey, videoIds)
                .asSequence()
                .filterNotNull()
                .associate { it.videoId to it.count }
        }

        fun put(watchHistoryCounts: WatchHistoryCounts) {
            if (watchHistoryCounts.isEmpty()) return
            val entries = watchHistoryCounts.mapValues { (videoId, count) ->
                WatchHistoryCountsEntry(videoId = videoId, count = count)
            }

            redisTemplate
                .opsForHash<Long, WatchHistoryCountsEntry>()
                .putAll(cacheKey, entries)
            redisTemplate.expire(cacheKey, cacheDuration)
        }
    }

    data class WatchHistoryCountsEntry(
        val videoId: Long,
        val count: Long,
    ) : Serializable

    companion object {

        const val CACHE_NAME = "WATCH_HISTORY_COUNTS"
    }
}
