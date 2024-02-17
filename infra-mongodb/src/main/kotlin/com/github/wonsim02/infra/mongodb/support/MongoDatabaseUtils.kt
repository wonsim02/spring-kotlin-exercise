package com.github.wonsim02.infra.mongodb.support

import com.github.womsim02.common.util.toCamelCase
import org.springframework.boot.autoconfigure.mongo.MongoProperties

object MongoDatabaseUtils {

    private const val MONGO_DATABASE_FACTORY_POSTFIX = "MongoDatabaseFactory"
    private const val MAPPING_MONGO_CONVERTER_POSTFIX = "MappingMongoConverter"
    private const val MONGO_TEMPLATE_POSTFIX = "MongoTemplate"

    /**
     * Spring Data MongoDB 관련 빈을 등록할 때 1순위 mongo 데이터베이스 이름으로 지정할 값을 추출한다.
     * @see org.springframework.boot.autoconfigure.data.mongo.MongoDatabaseFactoryConfiguration.mongoDatabaseFactory
     * @see com.github.wonsim02.infra.mongodb.config.PrimaryDatabaseConfiguration.mongoDatabaseFactory
     */
    fun determinePrimaryDatabaseName(
        mongoProperties: MongoProperties,
    ): String {
        return mongoProperties.mongoClientDatabase
    }

    /**
     * 추가로 정의한 Mongo 데이터베이스에 대한 `MongoDatabaseFactory` 빈의 이름을 생성한다.
     */
    fun genForMongoDatabaseFactory(database: String): String {
        return database.toCamelCase() + MONGO_DATABASE_FACTORY_POSTFIX
    }

    /**
     * 추가로 정의한 Mongo 데이터베이스에 대한 `MappingMongoConverter` 빈의 이름을 생성한다.
     */
    fun genForMappingMongoConverter(database: String): String {
        return database.toCamelCase() + MAPPING_MONGO_CONVERTER_POSTFIX
    }

    /**
     * 추가로 정의한 Mongo 데이터베이스에 대한 `MongoTemplate` 빈의 이름을 생성한다.
     */
    fun genForMongoTemplate(database: String): String {
        return database.toCamelCase() + MONGO_TEMPLATE_POSTFIX
    }
}
