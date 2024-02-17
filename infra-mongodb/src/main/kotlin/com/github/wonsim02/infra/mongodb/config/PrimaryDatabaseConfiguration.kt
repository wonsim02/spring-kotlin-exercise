package com.github.wonsim02.infra.mongodb.config

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import org.springframework.beans.BeanUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.domain.EntityScanner
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.autoconfigure.mongo.MongoPropertiesClientSettingsBuilderCustomizer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.PropertyMapper
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.data.mapping.model.FieldNamingStrategy
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

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

    /**
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDataConfiguration.mongoMappingContext
     */
    @Bean(name = [MONGO_MAPPING_CONTEXT])
    @ConditionalOnMissingBean
    fun mongoMappingContext(
        applicationContext: ApplicationContext,
        properties: MongoProperties,
        conversions: MongoCustomConversions,
    ): MongoMappingContext {
        val mapper: PropertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull()
        val context = MongoMappingContext()
        mapper.from(properties.isAutoIndexCreation).to(context::setAutoIndexCreation)
        context.setInitialEntitySet(EntityScanner(applicationContext).scan(Document::class.java))
        properties.fieldNamingStrategy?.let { strategyClass ->
            (BeanUtils.instantiateClass(strategyClass) as? FieldNamingStrategy)
                ?.let(context::setFieldNamingStrategy)
        }
        context.setSimpleTypeHolder(conversions.simpleTypeHolder)
        return context
    }

    /**
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDataConfiguration.mongoCustomConversions
     */
    @Bean
    @ConditionalOnMissingBean
    fun mongoCustomConversions(): MongoCustomConversions {
        return MongoCustomConversions(emptyList<Any>())
    }

    companion object {

        const val MONGO_MAPPING_CONTEXT = "mongoMappingContext"
    }
}
