package com.github.wonsim02.infra.mongodb.testcase.document

import com.github.wonsim02.infra.mongodb.MongoIndexDefinitionSource
import com.github.wonsim02.infra.mongodb.UseMongoDatabase
import org.springframework.data.mongodb.core.mapping.Document

@UseMongoDatabase(Document5.DATABASE_NAME)
@Document(collection = Document5.COLLECTION_NAME)
@MongoIndexDefinitionSource("classpath:/test_collection_5/*.json")
data class Document5(
    val id: Long,
    val a: String,
    val b: Int,
    val c: Float,
) {
    companion object {

        const val DATABASE_NAME = "test_database_5"
        const val COLLECTION_NAME = "test_collection_5"
    }
}
