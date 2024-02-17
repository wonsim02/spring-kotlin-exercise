package com.github.wonsim02.infra.mongodb.support

import com.github.womsim02.common.util.toCamelCase
import com.mongodb.client.MongoClient
import org.springframework.beans.BeanUtils
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.PropertyMapper
import org.springframework.context.ApplicationContext
import org.springframework.data.mapping.model.FieldNamingStrategy
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoDatabaseFactorySupport
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.convert.DbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

object MongoDatabaseUtils {

    private const val MONGO_DATABASE_FACTORY_POSTFIX = "MongoDatabaseFactory"
    private const val MAPPING_MONGO_CONVERTER_POSTFIX = "MappingMongoConverter"
    private const val MONGO_TEMPLATE_POSTFIX = "MongoTemplate"

    /**
     * Spring Data MongoDB 관련 빈을 등록할 때 1순위 mongo 데이터베이스 이름으로 지정할 값을 추출한다.
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryConfiguration.mongoDatabaseFactory
     * @see com.github.wonsim02.infra.mongodb.config.PrimaryMongoDatabaseConfiguration.mongoDatabaseFactory
     */
    fun determinePrimaryDatabaseName(
        mongoProperties: MongoProperties,
    ): String {
        return mongoProperties.mongoClientDatabase
    }

    /**
     * 추가로 정의한 Mongo 데이터베이스에 대한 `MongoDatabaseFactory` 빈의 이름을 생성한다.
     * @see AdditionalMongoDatabasesRegistrar.registerBeansForSingleDatabase
     */
    fun genForMongoDatabaseFactory(database: String): String {
        return database.toCamelCase() + MONGO_DATABASE_FACTORY_POSTFIX
    }

    /**
     * 추가로 정의한 Mongo 데이터베이스에 대한 `MappingMongoConverter` 빈의 이름을 생성한다.
     * @see AdditionalMongoDatabasesRegistrar.registerBeansForSingleDatabase
     */
    fun genForMappingMongoConverter(database: String): String {
        return database.toCamelCase() + MAPPING_MONGO_CONVERTER_POSTFIX
    }

    /**
     * 추가로 정의한 Mongo 데이터베이스에 대한 `MongoTemplate` 빈의 이름을 생성한다.
     * @see AdditionalMongoDatabasesRegistrar.registerBeansForSingleDatabase
     */
    fun genForMongoTemplate(database: String): String {
        return database.toCamelCase() + MONGO_TEMPLATE_POSTFIX
    }

    /**
     * [MongoMappingContext] 타입의 빈으로 사용될 객체를 생성한다.
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDataConfiguration.mongoCustomConversions
     * @see com.github.wonsim02.infra.mongodb.config.PrimaryMongoDatabaseConfiguration.mongoMappingContext
     */
    fun buildMongoMappingContext(
        applicationContext: ApplicationContext,
        properties: MongoProperties,
        conversions: MongoCustomConversions,
        database: String,
        isPrimary: Boolean,
    ): MongoMappingContext {
        val context = MongoMappingContext()
        val mapper: PropertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull()
        mapper.from(properties.isAutoIndexCreation).to(context::setAutoIndexCreation)

        val scanner = MongoDocumentScanner(applicationContext, database, isPrimary)
        context.setInitialEntitySet(scanner.scanDocuments())

        properties.fieldNamingStrategy?.let { strategyClass ->
            (BeanUtils.instantiateClass(strategyClass) as? FieldNamingStrategy)
                ?.let(context::setFieldNamingStrategy)
        }

        context.setSimpleTypeHolder(conversions.simpleTypeHolder)
        return context
    }

    /**
     * [MongoDatabaseFactorySupport] 타입의 빈으로 사용될 객체를 생성한다.
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryConfiguration.mongoDatabaseFactory
     * @see com.github.wonsim02.infra.mongodb.config.PrimaryMongoDatabaseConfiguration.mongoDatabaseFactory
     * @see AdditionalMongoDatabasesRegistrar.registerBeansForSingleDatabase
     */
    fun buildMongoDatabaseFactory(
        mongoClient: MongoClient,
        database: String,
    ): MongoDatabaseFactorySupport<*> {
        return SimpleMongoClientDatabaseFactory(mongoClient, database)
    }

    /**
     * [MappingMongoConverter] 타입의 빈으로 사용될 객체를 생성한다.
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryDependentConfiguration.mappingMongoConverter
     * @see com.github.wonsim02.infra.mongodb.config.PrimaryMongoDatabaseConfiguration.mappingMongoConverter
     * @see AdditionalMongoDatabasesRegistrar.registerBeansForSingleDatabase
     */
    fun buildMappingMongoConverter(
        factory: MongoDatabaseFactory,
        context: MongoMappingContext,
        conversions: MongoCustomConversions,
    ): MappingMongoConverter {
        val dbRefResolver: DbRefResolver = DefaultDbRefResolver(factory)
        val mappingConverter = MappingMongoConverter(dbRefResolver, context)
        mappingConverter.customConversions = conversions
        return mappingConverter
    }

    /**
     * [MongoTemplate] 타입의 빈으로 사용될 객체를 생성한다.
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryDependentConfiguration.mongoTemplate
     * @see com.github.wonsim02.infra.mongodb.config.PrimaryMongoDatabaseConfiguration.mongoTemplate
     * @see AdditionalMongoDatabasesRegistrar.registerBeansForSingleDatabase
     */
    fun buildMongoTemplate(
        factory: MongoDatabaseFactory,
        converter: MappingMongoConverter,
    ): MongoTemplate {
        return MongoTemplate(factory, converter)
    }
}
