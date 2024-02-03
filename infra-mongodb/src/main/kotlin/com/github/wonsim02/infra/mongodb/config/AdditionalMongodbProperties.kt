package com.github.wonsim02.infra.mongodb.config

import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * [MongoProperties]에는 없는 MongoDB 연결 관련 추가적인 속성.
 */
@ConfigurationProperties(prefix = "com.github.wonsim02.infra.mongodb")
@ConstructorBinding
class AdditionalMongodbProperties(
    val retryWrites: Boolean,
)
