package com.github.wonsim02.infra.mongodb.config

import com.mongodb.MongoClientSettings
import com.mongodb.client.internal.MongoClientImpl
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.context.annotation.Bean

/**
 * [MongoClientImpl]을 생성할 때 사용할 [MongoClientSettings]을 커스텀화한다.
 * 1. [AdditionalMongodbProperties.retryWrites] 값을 이용하여
 *  [retryWrites](https://www.mongodb.com/docs/manual/core/retryable-writes/) 값을 설정한다.
 * @see MongoClientSettings.Builder
 * @see org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.mongo
 */
class CustomizingMongoClientConfiguration {

    @Bean
    fun mongoClientSettingsBuilderCustomizer(
        additionalMongodbProperties: AdditionalMongodbProperties,
    ): MongoClientSettingsBuilderCustomizer {
        return MongoClientSettingsBuilderCustomizer { clientSettingsBuilder ->
            // sets retry writes
            // see: https://www.mongodb.com/docs/manual/core/retryable-writes/
            clientSettingsBuilder.retryWrites(additionalMongodbProperties.retryWrites)
        }
    }
}
