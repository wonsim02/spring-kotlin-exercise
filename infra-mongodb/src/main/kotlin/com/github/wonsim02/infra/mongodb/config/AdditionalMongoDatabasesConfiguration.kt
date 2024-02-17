package com.github.wonsim02.infra.mongodb.config

import com.github.wonsim02.infra.mongodb.support.AdditionalMongoDatabasesRegistrar
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

class AdditionalMongoDatabasesConfiguration {

    @Bean
    fun additionalMongoDatabasesRegistrarByProperties(
        additionalMongodbProperties: AdditionalMongodbProperties,
        applicationContext: ApplicationContext,
        mongoProperties: MongoProperties,
    ): AdditionalMongoDatabasesRegistrar {
        return AdditionalMongoDatabasesRegistrar(
            applicationContext = applicationContext,
            additionalDatabases = additionalMongodbProperties.additionalDatabases.toSet(),
            mongoProperties = mongoProperties,
        )
    }
}
