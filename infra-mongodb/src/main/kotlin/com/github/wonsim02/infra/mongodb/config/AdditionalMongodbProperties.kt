package com.github.wonsim02.infra.mongodb.config

import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * [MongoProperties]에는 없는 MongoDB 연결 관련 추가적인 속성.
 * @property retryWrites [retryWrites](https://www.mongodb.com/docs/manual/core/retryable-writes/) 설정에 대한 값
 * @property additionalDatabases [MongoProperties.database] 외 추가로 연결할 Mongo 데이터베이스 이름
 * @see CustomizingMongoClientConfiguration.mongoClientSettingsBuilderCustomizer
 * @see AdditionalMongoDatabasesConfiguration.additionalMongoDatabaseNamesSupplierFromProperty
 */
@ConfigurationProperties(prefix = "com.github.wonsim02.infra.mongodb")
@ConstructorBinding
class AdditionalMongodbProperties(
    val retryWrites: Boolean,
    val additionalDatabases: List<String> = listOf(),
)
