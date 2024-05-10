package com.github.wonsim02.infra.redis.config

import com.github.wonsim02.infra.redis.util.ByteArrayRedisSerializer
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration::class)
class InfraRedisConfiguration {

    @Bean(name = [BYTE_ARRAY_REDIS_TEMPLATE])
    @ConditionalOnSingleCandidate(RedisConnectionFactory::class)
    fun byteArrayRedisTemplate(
        redisConnectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, ByteArray> {
        val template = RedisTemplate<String, ByteArray>()

        template.setConnectionFactory(redisConnectionFactory)
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = ByteArrayRedisSerializer()
        template.hashKeySerializer = JdkSerializationRedisSerializer()
        template.hashValueSerializer = JdkSerializationRedisSerializer()

        return template
    }

    companion object {

        const val BYTE_ARRAY_REDIS_TEMPLATE = "byteArrayRedisTemplate"
    }
}
