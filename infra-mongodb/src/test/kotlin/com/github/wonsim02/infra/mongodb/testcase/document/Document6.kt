package com.github.wonsim02.infra.mongodb.testcase.document

import com.github.wonsim02.infra.mongodb.UseMongoDatabase
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDateTime

@UseMongoDatabase(Document6.DATABASE_NAME)
@Document(collection = Document6.COLLECTION_NAME)
data class Document6(
    val id: Long,
    val a: Instant,
    val b: LocalDateTime,
    val c: Map<String, Long>,
    val subDocument: SubDocument,
    val subDocuments: List<SubDocument>,
    val subDocumentsLists: List<List<SubDocument>>,
) {
    data class SubDocument(
        val a: Long,
        val b: String,
        val c: Float,
        val subSubDocuments: List<SubSubDocument>,
    ) {
        data class SubSubDocument(
            val a: Long,
            val b: String,
        )
    }

    companion object {

        const val DATABASE_NAME = "test_database_6"
        const val COLLECTION_NAME = "test_collection_6"
    }
}
