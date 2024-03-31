package com.github.wonsim02.infra.jpa

import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class CustomPostgresqlContainer(
    dockerImageName: DockerImageName,
    database: String,
    username: String,
    password: String,
) : PostgreSQLContainer<CustomPostgresqlContainer>(dockerImageName) {

    init {
        this.withDatabaseName(database)
            .withUsername(username)
            .withPassword(password)
            .withExposedPorts(5432)
    }

    constructor(
        database: String,
        username: String,
        password: String,
    ) : this(
        dockerImageName = DockerImageName
            .parse("public.ecr.aws/docker/library/postgres:11.14")
            .asCompatibleSubstituteFor("postgres"),
        database = database,
        username = username,
        password = password,
    )

    fun getMappedPortFor5432(): Int {
        return this.getMappedPort(5432)
    }

    private val springPropertyGetterMap: Map<String, () -> Any> = mapOf(
        "CONF_RDB_HOST" to this::getHost,
        "CONF_RDB_PORT" to this::getMappedPortFor5432,
        "CONF_RDB_DATABASE" to this::getDatabaseName,
        "CONF_RDB_USERNAME" to this::getUsername,
        "CONF_RDB_PASSWORD" to this::getPassword,
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
}
