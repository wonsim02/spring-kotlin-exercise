package com.github.wonsim02.infra.mongodb.config

import com.github.wonsim02.infra.mongodb.InfraMongodbTestBase
import com.mongodb.client.internal.MongoClientImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

class CustomizingMongoClientConfigurationTest : InfraMongodbTestBase() {

    /**
     * [AdditionalMongodbProperties.retryWrites] 값에 따라 [MongoClientImpl]의 `retryWrites` 값이 설정되었는지 검사한다.
     */
    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `retryWrites value of MongoClientImpl set as expected`(retryWrites: Boolean) {
        val profile = "test-retry-writes-$retryWrites"
        val profileArg = "--spring.profiles.active=$profile"
        val application = runApplication<App>(profileArg, *testContainer.provideMongoDbProperties())

        // infra-mongodb-config-test-retry-writes-*.yml 파일에 명시된 대로 값이 설정되어 있음
        val additionalMongodbProperties = application.getBean(AdditionalMongodbProperties::class.java)
        assertEquals(retryWrites, additionalMongodbProperties.retryWrites)

        val mongoClient = application.getBean(MongoClientImpl::class.java)
        assertEquals(retryWrites, mongoClient.settings.retryWrites)
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(InfraMongodbConfiguration::class)
    class App
}
