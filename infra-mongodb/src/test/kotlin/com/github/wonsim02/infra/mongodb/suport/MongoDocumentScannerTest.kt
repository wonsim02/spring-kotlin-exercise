package com.github.wonsim02.infra.mongodb.suport

import com.github.wonsim02.infra.mongodb.AdditionalMongoDatabaseNamesSupplier
import com.github.wonsim02.infra.mongodb.InfraMongodbTestBase
import com.github.wonsim02.infra.mongodb.UseMongoDatabase
import com.github.wonsim02.infra.mongodb.config.InfraMongodbConfiguration
import com.github.wonsim02.infra.mongodb.support.MongoDatabaseUtils
import com.github.wonsim02.infra.mongodb.support.MongoDocumentScanner
import com.github.wonsim02.infra.mongodb.testcase.document.Document0
import com.github.wonsim02.infra.mongodb.testcase.document.Document1
import com.github.wonsim02.infra.mongodb.testcase.document.Document2
import com.github.wonsim02.infra.mongodb.testcase.document.Document3
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity
import org.springframework.test.context.TestPropertySource
import java.util.stream.Stream

@SpringBootTest(
    classes = [
        InfraMongodbConfiguration::class,
        MongoDocumentScannerTest.App::class,
    ],
)
@EnableAutoConfiguration
@TestPropertySource(
    properties = ["CONF_MONGODB_RETRY_WRITES=false"],
)
class MongoDocumentScannerTest : InfraMongodbTestBase() {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var primaryMongoDatabaseFactory: MongoDatabaseFactory

    @Autowired
    private lateinit var primaryMongoMappingContext: MongoMappingContext

    /**
     * Primary로 지정된 Mongo 데이터베이스에 대해서 [Document0] 및 [Document3]을 [MongoDocumentScanner]가 찾아내는 지 검사한다.
     * - [Document0] : [UseMongoDatabase] 어노테이션이 없음
     * - [Document3] : [UseMongoDatabase]의 `databases` 값이 [InfraMongodbTestBase.DATABASE] 값과 일치
     */
    @Test
    fun `scanDocuments() works as expected for primary database`() {
        val scanner = MongoDocumentScanner(
            context = applicationContext,
            database = primaryMongoDatabaseFactory.mongoDatabase.name,
            isPrimary = true,
        )
        assertEquals(setOf(Document0::class.java, Document3::class.java), scanner.scanDocuments())
    }

    @Test
    fun `MongoMappingContext for primary database contains scanDocuments() results`() {
        val persistentEntityClasses = primaryMongoMappingContext.persistentEntities

        assertTrue(persistentEntityClasses.any { it.matches(Document0::class.java, Document0.COLLECTION_NAME) })
        assertTrue(persistentEntityClasses.any { it.matches(Document3::class.java, Document3.COLLECTION_NAME) })
    }

    @Test
    fun `MongoMappingContext for primary database does not contain document classes not in scanDocuments() results`() {
        val persistenceEntityClasses = primaryMongoMappingContext.persistentEntities

        assertEquals(0, persistenceEntityClasses.count { it.type == Document1::class.java })
        assertEquals(0, persistenceEntityClasses.count { it.type == Document2::class.java })
    }

    @TestFactory
    fun `scanDocuments() works as expected for non-primary databases`(): Stream<out DynamicTest> {
        return Stream
            .of(
                Document1.DATABASE_NAME to Document1::class.java,
                Document2.DATABASE_NAME to Document2::class.java,
            )
            .map { (database, documentClass) ->
                DynamicTest.dynamicTest(database) {
                    val scanner = MongoDocumentScanner(
                        context = applicationContext,
                        database = database,
                        isPrimary = false,
                    )
                    assertEquals(setOf(documentClass), scanner.scanDocuments())
                }
            }
    }

    @TestFactory
    fun `MongoMappingContext for non-primary database contains scanDocuments() results`(): Stream<out DynamicTest> {
        return Stream
            .of(
                Triple(Document1.DATABASE_NAME, Document1.COLLECTION_NAME, Document1::class.java),
                Triple(Document2.DATABASE_NAME, Document2.COLLECTION_NAME, Document2::class.java),
            )
            .map { (database, collection, documentClass) ->
                DynamicTest.dynamicTest(database) {
                    val mongoMappingContext = assertInstanceOf(
                        MongoMappingContext::class.java,
                        applicationContext.getBean(MongoDatabaseUtils.genForMongoMappingContext(database)),
                    )
                    assertTrue(mongoMappingContext.persistentEntities.any { it.matches(documentClass, collection) })
                }
            }
    }

    @TestFactory
    fun `MongoMappingContext for non-primary database does not contain document classes not in scanDocuments() results`(): Stream<out DynamicTest> {
        return Stream
            .of(
                Document1.DATABASE_NAME to setOf(Document0::class.java, Document2::class.java, Document3::class.java),
                Document2.DATABASE_NAME to setOf(Document0::class.java, Document1::class.java, Document3::class.java),
            )
            .map { (database, documentClasses) ->
                DynamicTest.dynamicTest(database) {
                    val mongoMappingContext = assertInstanceOf(
                        MongoMappingContext::class.java,
                        applicationContext.getBean(MongoDatabaseUtils.genForMongoMappingContext(database)),
                    )
                    val persistentEntities = mongoMappingContext.persistentEntities

                    for (documentClass in documentClasses) {
                        assertEquals(0, persistentEntities.count { it.type == documentClass })
                    }
                }
            }
    }

    /**
     * 주어진 [MongoPersistentEntity]의 `type` 및 `collection`을 검사한다.
     * @see MongoPersistentEntity.getType
     * @see MongoPersistentEntity.getCollection
     */
    private fun MongoPersistentEntity<*>.matches(
        documentClass: Class<out Any>,
        collectionName: String,
    ): Boolean {
        return type == documentClass && collection == collectionName
    }

    @EntityScan(basePackages = ["com.github.wonsim02.infra.mongodb.testcase.document"])
    class App {

        @Bean
        fun testDatabaseNamesSupplier(): AdditionalMongoDatabaseNamesSupplier = AdditionalMongoDatabaseNamesSupplier {
            listOf(Document1.DATABASE_NAME, Document2.DATABASE_NAME)
        }
    }
}
