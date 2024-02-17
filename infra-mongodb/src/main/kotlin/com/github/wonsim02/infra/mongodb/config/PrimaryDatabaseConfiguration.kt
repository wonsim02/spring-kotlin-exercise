package com.github.wonsim02.infra.mongodb.config

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.autoconfigure.mongo.MongoPropertiesClientSettingsBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

@EnableConfigurationProperties(MongoProperties::class)
class PrimaryDatabaseConfiguration {

    /**
     * @see MongoAutoConfiguration.MongoClientSettingsConfiguration.mongoPropertiesCustomizer
     */
    @Bean
    fun mongoPropertiesCustomizer(
        properties: MongoProperties,
        environment: Environment,
    ): MongoPropertiesClientSettingsBuilderCustomizer {
        return MongoPropertiesClientSettingsBuilderCustomizer(properties, environment)
    }

    /**
     * @see MongoAutoConfiguration.MongoClientSettingsConfiguration.mongoClientSettings
     */
    @Bean
    @ConditionalOnMissingBean(MongoClientSettings::class)
    fun mongoClientSettings(): MongoClientSettings {
        return MongoClientSettings.builder().build()
    }

    /**
     * @see MongoAutoConfiguration.mongo
     */
    @Bean
    @ConditionalOnMissingBean(MongoClient::class)
    fun mongo(
        builderCustomizers: List<MongoClientSettingsBuilderCustomizer>,
        settings: MongoClientSettings,
    ): MongoClient {
        return MongoClientFactory(builderCustomizers)
            .createMongoClient(settings)
    }
}
