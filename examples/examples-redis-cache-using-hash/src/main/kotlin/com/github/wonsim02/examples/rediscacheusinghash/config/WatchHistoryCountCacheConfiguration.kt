package com.github.wonsim02.examples.rediscacheusinghash.config

import com.github.wonsim02.examples.rediscacheusinghash.cache.WatchHistoryCountsCache
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.CacheKeyPrefix
import org.springframework.data.redis.core.RedisTemplate

@Configuration
@ConditionalOnProperty(
    prefix = WatchHistoryCountCacheConfiguration.WATCH_HISTORY_COUNT_CACHE_PROPERTY_PREFIX,
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
class WatchHistoryCountCacheConfiguration {

    @Bean
    fun watchHistoryCountsCache(
        cacheKeyPrefix: CacheKeyPrefix,
        redisTemplate: RedisTemplate<String, ByteArray>,
    ): WatchHistoryCountsCache {
        return WatchHistoryCountsCache(cacheKeyPrefix, redisTemplate)
    }

    companion object {

        const val WATCH_HISTORY_COUNT_CACHE_PROPERTY_PREFIX = "com.github.wonsim02.examples.redis-cache-using-hash.watch-history-counts-cache"
    }
}
