package com.github.wonsim02.infra.mongodb.suport

import com.github.wonsim02.infra.mongodb.AdditionalMongoDatabaseNamesSupplier
import com.github.wonsim02.infra.mongodb.InfraMongodbTestBase
import com.github.wonsim02.infra.mongodb.config.InfraMongodbConfiguration
import com.github.wonsim02.infra.mongodb.testcase.document.Document5
import com.mongodb.event.CommandListener
import com.mongodb.event.CommandStartedEvent
import org.bson.BsonDocument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource

@SpringBootTest(
    classes = [
        InfraMongodbConfiguration::class,
        MongoCollectionIndexConfigurerTest.App::class,
    ],
)
@EnableAutoConfiguration
@TestPropertySource(
    properties = ["CONF_MONGODB_RETRY_WRITES=false"],
)
class MongoCollectionIndexConfigurerTest : InfraMongodbTestBase() {

    @Test
    fun `MongoCollectionIndexConfigurer creates indexes as expected`() {
        val commands = MongoCommandCapturingListener.listCommands()

        // `createIndexes` 이전에 `listIndexes`부터 실행
        val listIndexesCommandIdx = commands.indexOfFirst {
            it.isListIndexesCommand(Document5.COLLECTION_NAME, Document5.DATABASE_NAME)
        }
        assertTrue(listIndexesCommandIdx >= 0)

        // `createIndexes`는 `listIndexes` 직후에 실행
        val createIndexesCommandIdx = commands.indexOfFirst {
            it.isCreateIndexesCommand(Document5.COLLECTION_NAME, Document5.DATABASE_NAME)
        }
        assertEquals(listIndexesCommandIdx + 1, createIndexesCommandIdx)

        // `idx003` 인덱스가 생성됨
        // 1. `name` 속성이 `idx003`이다.
        // 2. `background` 속성이 `true`이다.
        val createdIndexes = commands[createIndexesCommandIdx].getArray("indexes")
        assertEquals(1, createdIndexes.size)
        val createdIndex = createdIndexes[0] as BsonDocument
        assertEquals("idx003", createdIndex.getString("name").value)
        assertTrue(createdIndex.getBoolean("background").value)

        // `idx003`의 키 검증
        // 1. 키 순서가 `a`, `b`, `c` 순이다.
        // 2. `a`, `b`, `c`에 대한 값이 전부 1이다.
        val keys = createdIndex.getDocument("key")
        assertEquals(listOf("a", "b", "c"), keys.keys.toList())
        assertEquals(1, keys.getInt32("a").value)
        assertEquals(1, keys.getInt32("b").value)
        assertEquals(1, keys.getInt32("c").value)

        // `createIndexes`가 1번만 실행됨
        val numCreateIndexCommands = commands.count {
            it.isCreateIndexesCommand(Document5.COLLECTION_NAME, Document5.DATABASE_NAME)
        }
        assertEquals(1, numCreateIndexCommands)
    }

    private fun BsonDocument.isListIndexesCommand(
        collectionName: String,
        databaseName: String,
    ): Boolean {
        return containsKey("listIndexes") && getString("listIndexes").value == collectionName &&
            containsKey("\$db") && getString("\$db").value == databaseName
    }

    private fun BsonDocument.isCreateIndexesCommand(
        collectionName: String,
        databaseName: String,
    ): Boolean {
        return containsKey("createIndexes") && getString("createIndexes").value == collectionName &&
            containsKey("\$db") && getString("\$db").value == databaseName
    }

    private object MongoCommandCapturingListener : CommandListener {

        private val commands: MutableList<BsonDocument> = mutableListOf()

        fun listCommands(): List<BsonDocument> = commands.toList()

        override fun commandStarted(event: CommandStartedEvent?) {
            event
                ?.command
                ?.clone()
                ?.let(commands::add)
        }
    }

    @EntityScan(basePackages = ["com.github.wonsim02.infra.mongodb.testcase.document"])
    class App {

        @Bean
        fun testDatabaseNameSupplier(): AdditionalMongoDatabaseNamesSupplier =
            AdditionalMongoDatabaseNamesSupplier { listOf(Document5.DATABASE_NAME) }

        @Bean
        fun registeringMongoCommandCapturingListenerCustomizer(): MongoClientSettingsBuilderCustomizer =
            MongoClientSettingsBuilderCustomizer { it.addCommandListener(MongoCommandCapturingListener) }
    }
}
