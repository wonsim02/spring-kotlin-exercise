package com.github.wonsim02.infra.mongodb.config

import com.github.wonsim02.infra.mongodb.InfraMongodbTestBase
import com.mongodb.client.MongoClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.test.context.TestPropertySource

/**
 * [PrimaryMongoDatabaseConfiguration]으로 빈이 잘 등록되는지 검증하는 테스트.
 */
@SpringBootTest(classes = [InfraMongodbConfiguration::class])
@EnableAutoConfiguration
@TestPropertySource(
    properties = ["CONF_MONGODB_RETRY_WRITES=false"],
)
class PrimaryMongoDatabaseConfigurationTest : InfraMongodbTestBase() {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var mongoProperties: MongoProperties

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
     * [PrimaryMongoDatabaseConfiguration]으로 [MongoDatabaseFactory] 빈이 잘 등록되었는지 검사한다.
     * 1. 이름이 [PrimaryMongoDatabaseConfiguration.MONGO_DATABASE_FACTORY] 빈이 존재하는 지 확인한다.
     * 2. 1의 빈의 타입이 [MongoDatabaseFactory]인지 확인한다.
     * 3. 2의 빈이 가리키는 Mongo 데이터베이스가 [MongoProperties.database]와 일치하는지 확인한다.
     * 4. 2의 빈이 별도로 빈 이름을 지정하지 않고 불러온 [MongoDatabaseFactory] 타입의 빈과 동일한 객체인지 확인한다.
     */
    @Test
    fun `mongoDatabaseFactory registered as expected`() {
        val bean = assertDoesNotThrow {
            applicationContext.getBean(PrimaryMongoDatabaseConfiguration.MONGO_DATABASE_FACTORY)
        }
        val mongoDatabaseFactory = assertInstanceOf(MongoDatabaseFactory::class.java, bean)
        assertEquals(mongoProperties.database, mongoDatabaseFactory.mongoDatabase.name)

        assertTrue(primaryMongoDatabaseFactory === mongoDatabaseFactory)
    }

    /**
     * [PrimaryMongoDatabaseConfiguration]으로 [MappingMongoConverter] 빈이 잘 등록되었는지 검사한다.
     * 1. 이름이 [PrimaryMongoDatabaseConfiguration.MAPPING_MONGO_CONVERTER] 빈이 존재하는 지 확인한다.
     * 2. 1의 빈의 타입이 [MappingMongoConverter]인지 확인한다.
     * 3. 2의 빈이 별도로 빈 이름을 지정하지 않고 불러온 [MappingMongoConverter] 타입의 빈과 동일한 객체인지 확인한다.
     */
    @Test
    fun `mappingMongoConverter registered as expected`() {
        val bean = assertDoesNotThrow {
            applicationContext.getBean(PrimaryMongoDatabaseConfiguration.MAPPING_MONGO_CONVERTER)
        }
        val mappingMongoConverter = assertInstanceOf(MappingMongoConverter::class.java, bean)

        assertTrue(primaryMappingMongoConverter === mappingMongoConverter)
    }

    /**
     * [PrimaryMongoDatabaseConfiguration]으로 [MongoTemplate] 빈이 잘 등록되었는지 검사한다.
     * 1. 이름이 [PrimaryMongoDatabaseConfiguration.MONGO_TEMPLATE] 빈이 존재하는 지 확인한다.
     * 2. 1의 빈의 타입이 [MongoTemplate]인지 확인한다.
     * 3. 2의 빈이 [MongoTemplate.getMongoDatabaseFactory]가 별도로 빈 이름을 지정하지 않고 불러온 [MongoDatabaseFactory]와 동일한 객체인지 확인한다.
     * 4. 2의 빈의 [MongoTemplate.mongoConverter]가 별도로 빈 이름을 지정하지 않고 불러온 [MappingMongoConverter]와 동일한 객체인지 확인한다.
     * 5. 2의 빈이 별도로 빈 이름을 지정하지 않고 불러온 [MongoTemplate] 타입의 빈과 동일한 객체인지 확인한다.
     */
    @Test
    fun `mongoTemplate registered as expected`() {
        val bean = assertDoesNotThrow {
            applicationContext.getBean(PrimaryMongoDatabaseConfiguration.MONGO_TEMPLATE)
        }
        val mongoTemplate = assertInstanceOf(MongoTemplate::class.java, bean)

        assertTrue(primaryMongoDatabaseFactory === mongoTemplate.mongoDatabaseFactory)
        assertTrue(primaryMappingMongoConverter === mongoTemplate.converter)

        assertTrue(primaryMongoTemplate === mongoTemplate)
    }
}
