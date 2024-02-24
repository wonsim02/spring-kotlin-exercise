package com.github.wonsim02.infra.mongodb.testcase.document

import com.github.wonsim02.infra.mongodb.UseMongoDatabase
import org.springframework.data.mongodb.core.mapping.Document

@UseMongoDatabase(Document1.DATABASE_NAME)
@Document(collection = Document1.COLLECTION_NAME)
data class Document1(
    val id: Long,
) {

    companion object {

        const val DATABASE_NAME = "test_database_1"
        const val COLLECTION_NAME = "test_collection_1"
    }
}
