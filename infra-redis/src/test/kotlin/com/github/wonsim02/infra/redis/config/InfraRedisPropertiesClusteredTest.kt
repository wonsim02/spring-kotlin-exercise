package com.github.wonsim02.infra.redis.config

import com.github.wonsim02.infra.redis.CustomRedisContainer
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.runApplication
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * [InfraRedisProperties.clustered] 값으로 cluster 연결이 잘 설정되는 지 검사한다.
 * @see org.springframework.boot.autoconfigure.data.redis.LettuceConnectionConfiguration.redisConnectionFactory
 */
@Testcontainers
class InfraRedisPropertiesClusteredTest {

    @Test
    fun `redis configuration is standard if clustered = false`() {
        val app = runApplication<App>("--CONF_REDIS_CLUSTERED=false", *redisContainer.provideTestContainerProperties())

        try {
            // check InfraRedisProperties
            val infraRedisProperties = assertDoesNotThrow {
                app.getBean(InfraRedisProperties::class.java)
            }
            assertFalse(infraRedisProperties.clustered)

            // check RedisProperties
            val redisProperties = assertDoesNotThrow {
                app.getBean(RedisProperties::class.java)
            }
            assertNull(redisProperties.cluster)

            // check LettuceConnectionFactory
            val lettuceConnectionFactory = assertDoesNotThrow {
                app.getBean(LettuceConnectionFactory::class.java)
            }
            assertFalse(lettuceConnectionFactory.isClusterAware)
            assertNull(lettuceConnectionFactory.clusterConfiguration)
        } finally {
            SpringApplication.exit(app)
        }
    }

    @Test
    fun `redis configuration is clustered if clustered = true`() {
        val app = runApplication<App>("--CONF_REDIS_CLUSTERED=true", *redisContainer.provideTestContainerProperties())

        try {
            // check InfraRedisProperties
            val infraRedisProperties = assertDoesNotThrow {
                app.getBean(InfraRedisProperties::class.java)
            }
            assertTrue(infraRedisProperties.clustered)

            // check RedisProperties
            val redisProperties = assertDoesNotThrow {
                app.getBean(RedisProperties::class.java)
            }
            assertNotNull(redisProperties.cluster)

            // check LettuceConnectionFactory
            val lettuceConnectionFactory = assertDoesNotThrow {
                app.getBean(LettuceConnectionFactory::class.java)
            }
            assertTrue(lettuceConnectionFactory.isClusterAware)
            assertNotNull(lettuceConnectionFactory.clusterConfiguration)
        } finally {
            SpringApplication.exit(app)
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    class App

    companion object {

        @Container
        @JvmStatic
        val redisContainer = CustomRedisContainer("password")
    }
}
