package com.github.wonsim02.infra.redis.config

import com.github.womsim02.common.spring.ProfileAwarePropertySource
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties

/**
 * [RedisAutoConfiguration] 이전에 [RedisProperties]를 수정하는 configuration.
 */
@EnableConfigurationProperties(
    InfraRedisProperties::class,
    RedisProperties::class,
)
@ProfileAwarePropertySource(
    locations = [
        "classpath:/infra-redis-config.yml",
        "classpath:/infra-redis-config-*.yml",
    ],
)
@AutoConfigureBefore(RedisAutoConfiguration::class)
class InfraRedisConfiguration(
    infraRedisProperties: InfraRedisProperties,
    redisProperties: RedisProperties,
) {

    init {
        // `infraRedisProperties.clustered`가 `true`이면 `redisProperties.cluster` 값을 설정해 준다.
        if (infraRedisProperties.clustered) {
            val endpoint = redisProperties.run { "$host:$port" }
            val cluster = RedisProperties.Cluster()
            cluster.nodes = mutableListOf(endpoint)
            redisProperties.cluster = cluster
        }
    }
}
