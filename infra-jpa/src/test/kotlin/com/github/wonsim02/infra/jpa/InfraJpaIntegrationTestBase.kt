package com.github.wonsim02.infra.jpa

import com.github.wonsim02.infra.jpa.config.InfraJpaConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container

@SpringBootTest(classes = [InfraJpaConfiguration::class])
@EnableAutoConfiguration
@EntityScan(basePackages = ["com.github.wonsim02.infra.jpa.entity"])
abstract class InfraJpaIntegrationTestBase {

    companion object {

        @Container
        @JvmStatic
        val testContainer = CustomPostgresqlContainer(
            database = "database",
            username = "username",
            password = "password",
        )

        @DynamicPropertySource
        @JvmStatic
        @Suppress("unused")
        fun setTestDatabaseProperties(registry: DynamicPropertyRegistry) {
            testContainer.start()
            testContainer.setTestContainerProperties(registry)
        }
    }
}
