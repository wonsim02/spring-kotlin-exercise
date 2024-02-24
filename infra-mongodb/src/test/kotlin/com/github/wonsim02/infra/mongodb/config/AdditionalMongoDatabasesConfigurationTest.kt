package com.github.wonsim02.infra.mongodb.config

import com.github.wonsim02.infra.mongodb.AdditionalMongoDatabaseNamesSupplier
import com.github.wonsim02.infra.mongodb.InfraMongodbTestBase
import com.github.wonsim02.infra.mongodb.support.MongoDatabaseUtils
import com.mongodb.client.MongoClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.test.context.TestPropertySource

/**
 * [AdditionalMongoDatabasesConfiguration]으로 빈이 잘 등록되는지 검증하는 테스트.
 */
@SpringBootTest(
    classes = [
        InfraMongodbConfiguration::class,
        AdditionalMongoDatabasesConfigurationTest.App::class,
    ],
)
@EnableAutoConfiguration
@TestPropertySource(
    properties = [
        "CONF_MONGODB_RETRY_WRITES=false",
        "com.github.wonsim02.infra.mongodb.additional-databases=" +
            AdditionalMongoDatabasesConfigurationTest.ADDITIONAL_DATABASE_NAME_BY_PROPERTY,
    ],
)
class AdditionalMongoDatabasesConfigurationTest : InfraMongodbTestBase() {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var primaryMongoMappingContext: MongoMappingContext

    @Autowired
    private lateinit var primaryMongoDatabaseFactory: MongoDatabaseFactory

    @Autowired
    private lateinit var primaryMappingMongoConverter: MappingMongoConverter

    @Autowired
    private lateinit var primaryMongoTemplate: MongoTemplate

    /**
     * [MongoClient] 타입의 빈이 정확히 1개 등록되었는지 검증한다.
     */
    @Test
    fun `exactly one mongoClient bean registered`() {
        assertEquals(1, applicationContext.getBeansOfType(MongoClient::class.java).size)
    }

    /**
     * 이름이 [database]인 Mongo 데이터베이스에 대한 [MongoMappingContext], [MongoDatabaseFactory], [MappingMongoConverter] 및
     * [MongoTemplate] 빈이 잘 등록되었는지, 그리고 해당 빈들이 primary 빈이 아닌지 검사한다.
     */
    @ParameterizedTest
    @ValueSource(
        strings = [
            ADDITIONAL_DATABASE_NAME_BY_BEAN,
            ADDITIONAL_DATABASE_NAME_BY_PROPERTY,
        ],
    )
    fun `mongodb-related beans for additional database registered as expected`(database: String) {
        // verify existence of MongoMappingContext bean
        val mongoMappingContextName = MongoDatabaseUtils.genForMongoMappingContext(database)
        val mongoMappingContext = assertInstanceOf(
            MongoMappingContext::class.java,
            assertDoesNotThrow { applicationContext.getBean(mongoMappingContextName) },
        )

        // verify existence of MongoDatabaseFactory bean
        val mongoDatabaseFactoryName = MongoDatabaseUtils.genForMongoDatabaseFactory(database)
        val mongoDatabaseFactory = assertInstanceOf(
            MongoDatabaseFactory::class.java,
            assertDoesNotThrow { applicationContext.getBean(mongoDatabaseFactoryName) },
        )

        // verify MongoDatabaseFactory bean
        assertEquals(database, mongoDatabaseFactory.mongoDatabase.name)

        // verify existence of MappingMongoConverter bean
        val mappingMongoConverterName = MongoDatabaseUtils.genForMappingMongoConverter(database)
        val mappingMongoConverter = assertInstanceOf(
            MappingMongoConverter::class.java,
            assertDoesNotThrow { applicationContext.getBean(mappingMongoConverterName) },
        )

        // verify MappingMongoConverter bean
        assertTrue(mongoMappingContext === mappingMongoConverter.mappingContext)

        // verify existence of MongoTemplate bean
        val mongoTemplateName = MongoDatabaseUtils.genForMongoTemplate(database)
        val mongoTemplate = assertInstanceOf(
            MongoTemplate::class.java,
            assertDoesNotThrow { applicationContext.getBean(mongoTemplateName) },
        )

        // verify MongoTemplate bean
        assertTrue(mongoDatabaseFactory === mongoTemplate.mongoDatabaseFactory)
        assertTrue(mappingMongoConverter === mongoTemplate.converter)

        // compare with primary beans
        assertTrue(primaryMongoMappingContext !== mongoMappingContext)
        assertTrue(primaryMongoDatabaseFactory !== mongoDatabaseFactory)
        assertTrue(primaryMappingMongoConverter !== mappingMongoConverter)
        assertTrue(primaryMongoTemplate !== mongoTemplate)
    }

    @Configuration
    class App {

        @Bean
        fun additionalDatabasesNamesForTest(): AdditionalMongoDatabaseNamesSupplier =
            AdditionalMongoDatabaseNamesSupplier { listOf(ADDITIONAL_DATABASE_NAME_BY_BEAN) }
    }

    companion object {

        const val ADDITIONAL_DATABASE_NAME_BY_BEAN = "additional_database_name_by_bean"
        const val ADDITIONAL_DATABASE_NAME_BY_PROPERTY = "additional_database_name_by_property"
    }
}
