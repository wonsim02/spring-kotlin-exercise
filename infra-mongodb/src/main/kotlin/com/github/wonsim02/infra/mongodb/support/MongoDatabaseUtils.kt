package com.github.wonsim02.infra.mongodb.support

import org.springframework.boot.autoconfigure.mongo.MongoProperties

object MongoDatabaseUtils {

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
}
