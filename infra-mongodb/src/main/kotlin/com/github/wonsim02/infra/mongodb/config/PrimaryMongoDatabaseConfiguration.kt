package com.github.wonsim02.infra.mongodb.config

import com.github.wonsim02.infra.mongodb.support.MongoDatabaseUtils
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
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
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

/**
 * [MongoDataAutoConfiguration]으로 등록되는 빈의 정의를 그대로 가져온 스프링 설정.
 * 그 중 Mongo 데이터베이스가 달라지면 새로 빈을 등록해야 하는 경우에는 @[Primary] 어노테이션을 통해 해당 빈의 타입에 대해 2개 이상의 빈이 등록될 수 있도록 조치함.
 * - [MongoAutoConfiguration] : [mongoPropertiesCustomizer], [mongoClientSettings], [mongo]
 * - [org.springframework.boot.autoconfigure.data.mongo.MongoDataConfiguration] : [mongoMappingContext], [mongoCustomConversions]
 * - [org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryConfiguration] : [mongoDatabaseFactory]
 *      - `mongoDatabaseFactory` 빈의 경우 연결하려는 Mongo 데이터베이스의 값에 따라 `MongoDatabaseFactorySupport` 타입의 빈이 추가될 수 있으므로 `Primary` 빈으로 정의
 * - [org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryDependentConfiguration] : [mappingMongoConverter], [mongoTemplate]
 *      - `mappingMongoConverter` 및 `mongoTemplate`의 경우 `MongoDatabaseFactory` 타입의 빈을 주입받아 생성되므로 Mongo 데이터베이스의 값에 따라
 *       새로운 빈이 등록될 수 있다. 따라서 `Primary` 빈으로 정의하여 여러 개의 `MappingMongoConverter` 및 `MongoTemplate` 빈의 등록을 허용한다.
 * @see MongoDatabaseUtils.buildMongoDatabaseFactory
 * @see MongoDatabaseUtils.buildMappingMongoConverter
 * @see MongoDatabaseUtils.buildMongoTemplate
 */
@EnableConfigurationProperties(MongoProperties::class)
class PrimaryMongoDatabaseConfiguration {

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
        return MongoDatabaseUtils.buildMongoDatabaseFactory(
            mongoClient = mongoClient,
            database = MongoDatabaseUtils.determinePrimaryDatabaseName(properties),
        )
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
        return MongoDatabaseUtils.buildMappingMongoConverter(factory, context, conversions)
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
        return MongoDatabaseUtils.buildMongoTemplate(factory, converter)
    }

    companion object {

        const val MAPPING_MONGO_CONVERTER = "mappingMongoConverter"
        const val MONGO_DATABASE_FACTORY = "mongoDatabaseFactory"
        const val MONGO_MAPPING_CONTEXT = "mongoMappingContext"
        const val MONGO_TEMPLATE = "mongoTemplate"
    }
}
