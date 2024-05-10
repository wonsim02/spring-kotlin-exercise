package com.github.wonsim02.examples.rediscacheusinghash.config

import com.github.wonsim02.examples.rediscacheusinghash.cache.WatchedVideosCountsCache
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.CacheKeyPrefix
import org.springframework.data.redis.core.RedisTemplate

@Configuration
@ConditionalOnProperty(
    prefix = WatchedVideosCountCacheConfiguration.WATCHED_VIDEOS_COUNT_CACHE_PROPERTY_PREFIX,
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
class WatchedVideosCountCacheConfiguration {

    @Bean
    fun watchedVideosCountsCache(
        cacheKeyPrefix: CacheKeyPrefix,
        redisTemplate: RedisTemplate<String, ByteArray>,
    ): WatchedVideosCountsCache {
        return WatchedVideosCountsCache(cacheKeyPrefix, redisTemplate)
    }

    companion object {

        const val WATCHED_VIDEOS_COUNT_CACHE_PROPERTY_PREFIX = "com.github.wonsim02.examples.redis-cache-using-hash.watched-videos-counts-cache"
    }
}
