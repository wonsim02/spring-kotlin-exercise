package com.github.wonsim02.examples.rediscacheusinghash

import com.github.wonsim02.infra.jpa.CustomPostgresqlContainer
import com.github.wonsim02.infra.redis.CustomRedisContainer
import org.springframework.boot.runApplication

fun main(vararg args: String) {
    val postgresqlContainer = CustomPostgresqlContainer(
        database = "database",
        username = "username",
        password = "password",
    )
    val redisContainer = CustomRedisContainer(password = "password")

    postgresqlContainer.start()
    redisContainer.start()

    runApplication<App>(
        *postgresqlContainer.provideTestContainerProperties(),
        *redisContainer.provideTestContainerProperties(),
        "--CONF_REDIS_CLUSTERED=false",
    )
}
