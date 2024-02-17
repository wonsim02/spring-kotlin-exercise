package com.github.wonsim02.infra.mongodb.config

import com.github.wonsim02.infra.mongodb.AdditionalMongoDatabaseNamesSupplier
import com.github.wonsim02.infra.mongodb.support.AdditionalMongoDatabasesRegistrar
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

class AdditionalMongoDatabasesConfiguration {

    @Bean
    fun additionalMongoDatabaseNamesSupplierFromProperty(
        properties: AdditionalMongodbProperties,
    ): AdditionalMongoDatabaseNamesSupplier = AdditionalMongoDatabaseNamesSupplier {
        properties.additionalDatabases
    }

    @Bean
    fun additionalMongoDatabasesRegistrar(
        additionalMongoDatabaseNamesSuppliers: ObjectProvider<AdditionalMongoDatabaseNamesSupplier>,
        applicationContext: ApplicationContext,
        mongoProperties: MongoProperties,
    ): AdditionalMongoDatabasesRegistrar {
        return AdditionalMongoDatabasesRegistrar(
            applicationContext = applicationContext,
            additionalDatabases = additionalMongoDatabaseNamesSuppliers
                .flatMapTo(mutableSetOf()) { it() },
            mongoProperties = mongoProperties,
        )
    }
}
