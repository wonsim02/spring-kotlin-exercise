package com.github.wonsim02.infra.mongodb.testcase.document

import com.github.wonsim02.infra.mongodb.UseMongoDatabase
import org.springframework.data.mongodb.core.mapping.Document

@UseMongoDatabase(Document4.DATABASE_NAME_1, Document4.DATABASE_NAME_2)
@Document(collection = Document4.COLLECTION_NAME)
data class Document4(
    val id: Long,
) {

    companion object {

        const val DATABASE_NAME_1 = "test_database_3"
        const val DATABASE_NAME_2 = "test_database_4"
        const val COLLECTION_NAME = "test_collection_4"
    }
}
