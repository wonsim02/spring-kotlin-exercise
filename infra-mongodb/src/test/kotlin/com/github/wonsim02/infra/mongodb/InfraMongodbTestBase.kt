package com.github.wonsim02.infra.mongodb

import com.github.wonsim02.infra.mongodb.testutil.CustomMongoDBContainer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class InfraMongodbTestBase {

    companion object {

        const val DATABASE = "database"
        const val USERNAME = "username"
        const val PASSWORD = "password"

        @JvmStatic
        @Container
        val testContainer: CustomMongoDBContainer = CustomMongoDBContainer()

        @JvmStatic
        @DynamicPropertySource
        @Suppress("unused")
        fun setTestContainerProperties(registry: DynamicPropertyRegistry) {
            testContainer.setTestContainerProperties(registry)
        }
    }
}
