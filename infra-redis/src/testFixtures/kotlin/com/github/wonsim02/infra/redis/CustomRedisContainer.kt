package com.github.wonsim02.infra.redis

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

class CustomRedisContainer(
    dockerImageName: DockerImageName,
    private val password: String,
) : GenericContainer<CustomRedisContainer>(dockerImageName) {

    init {
        withCommand("redis-server", "--requirepass", password)
        withExposedPorts(REDIS_PORT)
        waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1))
    }

    constructor(
        password: String,
    ) : this(
        dockerImageName = DockerImageName
            .parse("public.ecr.aws/docker/library/redis:6.0.16")
            .asCompatibleSubstituteFor("redis"),
        password = password,
    )

    fun getMappedPortFor6379(): Int {
        return this.getMappedPort(REDIS_PORT)
    }

    fun getPassword(): String = password

    private val springPropertyGetterMap: Map<String, () -> Any> = mapOf(
        "CONF_REDIS_HOST" to this::getHost,
        "CONF_REDIS_PORT" to this::getMappedPortFor6379,
        "CONF_REDIS_PASSWORD" to this::getPassword,
    )

    fun provideTestContainerProperties(): Array<String> {
        return springPropertyGetterMap
            .map { (propertyKey, propertyValueGetter) ->
                "--$propertyKey=${propertyValueGetter()}"
            }
            .toTypedArray()
    }

    fun setTestContainerProperties(registry: DynamicPropertyRegistry) {
        for ((propertyKey, propertyValueGetter) in springPropertyGetterMap) {
            registry.add(propertyKey, propertyValueGetter)
        }
    }

    companion object {

        const val REDIS_PORT = 6379
    }
}
