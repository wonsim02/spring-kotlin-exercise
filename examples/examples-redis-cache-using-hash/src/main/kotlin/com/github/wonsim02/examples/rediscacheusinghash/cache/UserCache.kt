package com.github.wonsim02.examples.rediscacheusinghash.cache

import com.github.wonsim02.examples.rediscacheusinghash.config.CustomRedisCacheConfiguration
import com.github.wonsim02.examples.rediscacheusinghash.model.User
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.stereotype.Component
import java.time.Duration

@CacheConfig(cacheNames = [UserCache.CACHE_NAME])
@Component
class UserCache : CustomRedisCacheConfiguration.CustomRedisCacheConfigurationBuilder() {

    override val cacheName = CACHE_NAME
    override val cacheDuration: Duration = Duration.ofHours(1L)
    override val redisValueSerializer = JdkSerializationRedisSerializer()

    @Cacheable(key = "{ #userId }")
    fun get(userId: Long): User? {
        return null
    }

    @CachePut(key = "{ #user.id }")
    fun put(user: User): User {
        return user
    }

    companion object {

        const val CACHE_NAME = "USER"
    }
}
