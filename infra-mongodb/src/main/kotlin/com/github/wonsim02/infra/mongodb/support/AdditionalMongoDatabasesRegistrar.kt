package com.github.wonsim02.infra.mongodb.support

import com.github.wonsim02.infra.mongodb.config.PrimaryDatabaseConfiguration
import com.mongodb.client.MongoClient
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoDatabaseFactorySupport
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

/**
 * [additionalDatabases]로 지정된 Mongo 데이터베이스에 대해 [MongoDatabaseFactory], [MappingMongoConverter] 및 [MongoTemplate]
 * 빈을 등록하는 컴포넌트.
 * [PrimaryDatabaseConfiguration]에서 `@Primary` 어노테이션으로 정의된 빈의 타입에 대하여 각 데이터베이스 이름마다 새로운 빈이 추가된다.
 */
class AdditionalMongoDatabasesRegistrar(
    private val applicationContext: ApplicationContext,
    private val additionalDatabases: Set<String>,
    mongoProperties: MongoProperties,
) : BeanFactoryPostProcessor {

    private val primaryMongoDatabase: String = MongoDatabaseUtils.determinePrimaryDatabaseName(mongoProperties)

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        if (beanFactory !is DefaultListableBeanFactory) return
        for (database in additionalDatabases) {
            if (database != primaryMongoDatabase) {
                registerBeansForSingleDatabase(beanFactory, database)
            }
        }
    }

    private fun registerBeansForSingleDatabase(
        beanFactory: DefaultListableBeanFactory,
        database: String,
    ) {
        // register MongoDatabaseFactorySupport
        val mongoDatabaseFactoryName = MongoDatabaseUtils.genForMongoDatabaseFactory(database)
        val mongoDatabaseFactoryDef = RootBeanDefinition(MongoDatabaseFactorySupport::class.java) {
            MongoDatabaseUtils.buildMongoDatabaseFactory(
                mongoClient = applicationContext.getBean(MongoClient::class.java),
                database = database,
            )
        }
        beanFactory.registerBeanDefinition(mongoDatabaseFactoryName, mongoDatabaseFactoryDef)

        // register MappingMongoConverter
        val mappingMongoConverterName = MongoDatabaseUtils.genForMappingMongoConverter(database)
        val mappingMongoConverterDef = RootBeanDefinition(MappingMongoConverter::class.java) {
            MongoDatabaseUtils.buildMappingMongoConverter(
                factory = applicationContext.getBean(mongoDatabaseFactoryName) as MongoDatabaseFactory,
                context = applicationContext.getBean(MongoMappingContext::class.java),
                conversions = applicationContext.getBean(MongoCustomConversions::class.java),
            )
        }
        beanFactory.registerBeanDefinition(mappingMongoConverterName, mappingMongoConverterDef)

        // register MongoTemplate
        val mongoTemplateName = MongoDatabaseUtils.genForMongoTemplate(database)
        val mongoTemplateDef = RootBeanDefinition(MongoTemplate::class.java) {
            MongoDatabaseUtils.buildMongoTemplate(
                factory = applicationContext.getBean(mongoDatabaseFactoryName) as MongoDatabaseFactory,
                converter = applicationContext.getBean(mappingMongoConverterName) as MappingMongoConverter,
            )
        }
        beanFactory.registerBeanDefinition(mongoTemplateName, mongoTemplateDef)
    }
}
