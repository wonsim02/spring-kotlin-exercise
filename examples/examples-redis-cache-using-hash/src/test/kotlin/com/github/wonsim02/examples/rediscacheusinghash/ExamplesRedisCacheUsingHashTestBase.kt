package com.github.wonsim02.examples.rediscacheusinghash

import com.github.wonsim02.infra.jpa.CustomPostgresqlContainer
import com.github.wonsim02.infra.redis.CustomRedisContainer
import org.springframework.boot.SpringApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class ExamplesRedisCacheUsingHashTestBase {

    protected abstract val args: Array<out String>

    protected inline fun testWithApplication(testAction: (ConfigurableApplicationContext) -> Unit) {
        val app = runApplication<App>(
            *postgresqlContainer.provideTestContainerProperties(),
            *redisContainer.provideTestContainerProperties(),
            *args,
            "--CONF_REDIS_CLUSTERED=false",
        )

        try {
            testAction(app)
        } finally {
            SpringApplication.exit(app)
        }
    }

    companion object {

        @Container
        @JvmStatic
        val postgresqlContainer = CustomPostgresqlContainer(
            database = "database",
            username = "username",
            password = "password",
        )

        @Container
        @JvmStatic
        val redisContainer = CustomRedisContainer(password = "password")
    }
}
