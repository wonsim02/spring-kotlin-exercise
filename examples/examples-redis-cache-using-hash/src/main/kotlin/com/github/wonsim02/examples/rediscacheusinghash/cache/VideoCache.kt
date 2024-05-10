package com.github.wonsim02.examples.rediscacheusinghash.cache

import com.github.wonsim02.examples.rediscacheusinghash.config.CustomRedisCacheConfiguration
import com.github.wonsim02.examples.rediscacheusinghash.model.Video
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.cache.CacheKeyPrefix
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.stereotype.Component
import java.time.Duration

@CacheConfig(cacheNames = [VideoCache.CACHE_NAME])
@Component
class VideoCache(
    cacheKeyPrefix: CacheKeyPrefix,
    private val redisTemplate: RedisTemplate<String, ByteArray>,
) : CustomRedisCacheConfiguration.CustomRedisCacheConfigurationBuilder() {

    override val cacheName = CACHE_NAME
    override val cacheDuration: Duration = Duration.ofHours(1L)
    override val redisValueSerializer = JdkSerializationRedisSerializer()

    private val prefix: String = cacheKeyPrefix.compute(CACHE_NAME)

    @Cacheable(key = "{ #videoId }")
    fun get(videoId: Long): Video? {
        return null
    }

    @CachePut(key = "{ #video.id }")
    fun put(video: Video): Video {
        return video
    }

    fun multiGet(videoIds: Collection<Long>): Map<Long, Video> {
        if (videoIds.isEmpty()) return mapOf()

        val cacheKeys = videoIds.map { "$prefix$it" }
        return redisTemplate
            .opsForValue()
            .multiGet(cacheKeys)
            ?.asSequence()
            ?.mapNotNull { redisValueSerializer.deserialize(it) as? Video }
            ?.associateBy { it.id }
            ?: mapOf()
    }

    companion object {

        const val CACHE_NAME = "VIDEO"
    }
}
