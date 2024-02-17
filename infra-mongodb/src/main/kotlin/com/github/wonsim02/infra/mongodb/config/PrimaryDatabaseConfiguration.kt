package com.github.wonsim02.infra.mongodb.config

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Qualifier
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
import org.springframework.context.annotation.Primary
import org.springframework.core.env.Environment
import org.springframework.data.mapping.model.FieldNamingStrategy
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoDatabaseFactorySupport
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.convert.DbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
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

    /**
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryConfiguration.mongoDatabaseFactory
     */
    @Primary
    @Bean(name = [MONGO_DATABASE_FACTORY])
    fun mongoDatabaseFactory(
        mongoClient: MongoClient,
        properties: MongoProperties,
    ): MongoDatabaseFactorySupport<*> {
        return SimpleMongoClientDatabaseFactory(mongoClient, properties.mongoClientDatabase)
    }

    /**
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryDependentConfiguration.mappingMongoConverter
     */
    @Primary
    @Bean(name = [MAPPING_MONGO_CONVERTER])
    fun mappingMongoConverter(
        @Qualifier(MONGO_DATABASE_FACTORY) factory: MongoDatabaseFactory,
        @Qualifier(MONGO_MAPPING_CONTEXT) context: MongoMappingContext,
        conversions: MongoCustomConversions,
    ): MappingMongoConverter {
        val dbRefResolver: DbRefResolver = DefaultDbRefResolver(factory)
        val mappingConverter = MappingMongoConverter(dbRefResolver, context)
        mappingConverter.customConversions = conversions
        return mappingConverter
    }

    /**
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryDependentConfiguration.mongoTemplate
     */
    @Primary
    @Bean(name = [MONGO_TEMPLATE])
    fun mongoTemplate(
        @Qualifier(MONGO_DATABASE_FACTORY) factory: MongoDatabaseFactory,
        @Qualifier(MAPPING_MONGO_CONVERTER) converter: MappingMongoConverter,
    ): MongoTemplate {
        return MongoTemplate(factory, converter)
    }

    companion object {

        const val MAPPING_MONGO_CONVERTER = "mappingMongoConverter"
        const val MONGO_DATABASE_FACTORY = "mongoDatabaseFactory"
        const val MONGO_MAPPING_CONTEXT = "mongoMappingContext"
        const val MONGO_TEMPLATE = "mongoTemplate"
    }
}
