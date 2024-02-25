package com.github.wonsim02.infra.mongodb

import com.github.wonsim02.infra.mongodb.MongoIndexDefinitionSource.Companion.ParseFilenameResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.stream.Stream

/**
 * [MongoIndexDefinitionSource.parseFilename]에 대한 테스트.
 */
class MongoIndexDefinitionSourceParseFilenameTest {

    @TestFactory
    fun `parseFilename works as expected`(): Stream<out DynamicTest> {
        return Stream
            .of(
                TestCase.Success(
                    displayName = "indexName is idx000 and description is description",
                    filename = "idx000__description.json",
                    expectedIndexName = "idx000",
                    expectedDescription = "description",
                ),
                TestCase.Failure(
                    displayName = "invalid indexName",
                    filename = "idx__description.json",
                ),
                TestCase.Failure(
                    displayName = "failed to find description",
                    filename = "idx0_description.json",
                ),
                TestCase.Failure(
                    displayName = "invalid extension",
                    filename = "idx00__description.yml",
                ),
            )
            .map { it.toDynamicTest() }
    }

    sealed class TestCase {

        abstract val displayName: String
        abstract val filename: String
        abstract fun test()

        fun toDynamicTest(): DynamicTest {
            return DynamicTest.dynamicTest(displayName) { test() }
        }

        data class Success(
            override val displayName: String,
            override val filename: String,
            val expectedIndexName: String,
            val expectedDescription: String?,
        ) : TestCase() {

            override fun test() {
                val parseResult = assertDoesNotThrow {
                    MongoIndexDefinitionSource.parseFilename(filename)!!
                }

                val expected = ParseFilenameResult(expectedIndexName, expectedDescription)
                assertEquals(expected, parseResult)
            }
        }

        data class Failure(
            override val displayName: String,
            override val filename: String,
        ) : TestCase() {

            override fun test() {
                assertNull(MongoIndexDefinitionSource.parseFilename(filename))
            }
        }
    }
}
