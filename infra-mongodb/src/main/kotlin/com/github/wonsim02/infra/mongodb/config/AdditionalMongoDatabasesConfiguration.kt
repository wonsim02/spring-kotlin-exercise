package com.github.wonsim02.infra.mongodb.config

import com.github.wonsim02.infra.mongodb.AdditionalMongoDatabaseNamesSupplier
import com.github.wonsim02.infra.mongodb.support.AdditionalMongoDatabasesRegistrar
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate

/**
 * [AdditionalMongoDatabaseNamesSupplier] 빈으로부터 추가로 등록할 Mongo 데이터베이스 이름을 수집하여
 * [AdditionalMongoDatabasesRegistrar] 빈을 통해 추가 Mongo 데이터베이스에 대한 [MongoTemplate] 빈을 등록하는 설정.
 * 이 과정에서 [AdditionalMongodbProperties.additionalDatabases]로 지정된 Mongo 데이터베이스에 대해서도 [MongoTemplate] 빈을 등록한다.
 * @see AdditionalMongodbProperties.additionalDatabases
 * @see AdditionalMongoDatabasesRegistrar
 */
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
    ): AdditionalMongoDatabasesRegistrar {
        return AdditionalMongoDatabasesRegistrar(
            applicationContext = applicationContext,
            additionalDatabases = additionalMongoDatabaseNamesSuppliers
                .flatMapTo(mutableSetOf()) { it() },
        )
    }
}
