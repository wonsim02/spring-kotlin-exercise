package com.github.wonsim02.infra.mongodb

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class InfraMongodbTestBase {

    companion object {

        private const val DATABASE = "database"
        private const val USERNAME = "username"
        private const val PASSWORD = "password"

        @JvmStatic
        @Container
        val testContainer: MongoDBContainer = MongoDBContainer(
            DockerImageName
                .parse("public.ecr.aws/docker/library/mongo:4.0")
                .asCompatibleSubstituteFor("mongo")
        )
            .withEnv("MONGO_INITDB_DATABASE", DATABASE)
            .withEnv("MONGO_INITDB_USERNAME", USERNAME)
            .withEnv("MONGO_INITDB_PASSWORD", PASSWORD)

        private val springPropertyGetterMap: Map<String, () -> Any> = mapOf(
            "spring.data.mongodb.host" to testContainer::getHost,
            "spring.data.mongodb.port" to { testContainer.exposedPorts.first() },
            "spring.data.mongodb.database" to { DATABASE },
            "spring.data.mongodb.username" to { USERNAME },
            "spring.data.mongodb.password" to { PASSWORD },
        )

        fun provideMongoDbProperties(): Array<String> {
            return springPropertyGetterMap
                .map { (propertyKey, propertyValueGetter) ->
                    "--$propertyKey=${propertyValueGetter()}"
                }
                .toTypedArray()
        }

        @JvmStatic
        @DynamicPropertySource
        @Suppress("unused")
        fun setTestContainerProperties(registry: DynamicPropertyRegistry) {
            for ((propertyKey, propertyValueGetter) in springPropertyGetterMap) {
                registry.add(propertyKey, propertyValueGetter)
            }
        }
    }
}
