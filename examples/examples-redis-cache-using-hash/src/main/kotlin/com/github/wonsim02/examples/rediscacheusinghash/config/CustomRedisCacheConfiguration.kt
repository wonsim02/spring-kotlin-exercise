package com.github.wonsim02.examples.rediscacheusinghash.config

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.CacheKeyPrefix
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class CustomRedisCacheConfiguration {

    @Bean
    fun cacheKeyPrefix(): CacheKeyPrefix {
        return CacheKeyPrefix { name -> "$name$SEPARATOR" }
    }

    @Bean
    fun redisCacheManagerBuilderCustomizer(
        customRedisCacheConfigurationBuilders: List<CustomRedisCacheConfigurationBuilder>,
        cacheKeyPrefix: CacheKeyPrefix,
    ): RedisCacheManagerBuilderCustomizer {
        return RedisCacheManagerBuilderCustomizer { builder ->
            customRedisCacheConfigurationBuilders.forEach {
                it.configureRedisCacheManagerBuilder(builder, cacheKeyPrefix)
            }
        }
    }

    abstract class CustomRedisCacheConfigurationBuilder {

        abstract val cacheName: String
        abstract val cacheDuration: Duration
        abstract val redisValueSerializer: RedisSerializer<*>

        private fun buildCacheConfiguration(
            cacheKeyPrefix: CacheKeyPrefix,
        ): RedisCacheConfiguration {
            return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheDuration)
                .serializeValuesWith(
                    RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(redisValueSerializer)
                )
                .computePrefixWith(cacheKeyPrefix)
        }

        fun configureRedisCacheManagerBuilder(
            builder: RedisCacheManager.RedisCacheManagerBuilder,
            cacheKeyPrefix: CacheKeyPrefix,
        ): RedisCacheManager.RedisCacheManagerBuilder {
            return builder.withCacheConfiguration(
                cacheName,
                buildCacheConfiguration(cacheKeyPrefix),
            )
        }
    }

    companion object {

        const val SEPARATOR = "::"
    }
}
