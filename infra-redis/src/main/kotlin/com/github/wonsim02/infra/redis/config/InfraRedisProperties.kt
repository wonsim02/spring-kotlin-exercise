package com.github.wonsim02.infra.redis.config

import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Redis 연결 관련 추가적인 속성.
 * @property clustered 해당 값이 `true`일 경우, [RedisProperties]가 가리키는 엔드포인트를 단일 노드로 가지는 redis 클러스터로 간주한다.
 */
@ConfigurationProperties(prefix = "com.github.wonsim02.infra.redis")
@ConstructorBinding
class InfraRedisProperties(
    val clustered: Boolean,
)
