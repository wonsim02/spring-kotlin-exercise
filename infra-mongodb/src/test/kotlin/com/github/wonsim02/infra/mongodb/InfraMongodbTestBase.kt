package com.github.wonsim02.infra.mongodb

import com.github.wonsim02.infra.mongodb.testutil.CustomMongoDBContainer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class InfraMongodbTestBase {

    companion object {

        const val DATABASE = "database"
        private const val USERNAME = "username"
        private const val PASSWORD = "password"

        @JvmStatic
        @Container
        val testContainer: CustomMongoDBContainer = CustomMongoDBContainer(
            dockerImageName = DockerImageName
                .parse("public.ecr.aws/docker/library/mongo:4.0")
                .asCompatibleSubstituteFor("mongo"),
            database = DATABASE,
            username = USERNAME,
            password = PASSWORD,
        )

        private val springPropertyGetterMap: Map<String, () -> Any> = mapOf(
            "spring.data.mongodb.host" to testContainer::getHost,
            "spring.data.mongodb.port" to { testContainer.getMappedPorts().first() },
            "spring.data.mongodb.database" to { DATABASE },
            "spring.data.mongodb.username" to { USERNAME },
            "spring.data.mongodb.password" to { PASSWORD },
            "CONF_MONGODB_AUTHENTICATION_DATABASE" to { "admin" },
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
