package com.github.wonsim02.infra.mongodb.testcase.document

import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = Document0.COLLECTION_NAME)
data class Document0(
    val id: Long,
    val a: List<A>,
    val f: Float,
) {
    data class A(
        val b: List<B>,
        val c: Int,
    )

    data class B(
        val d: String,
        val e: Instant,
    )

    companion object {

        const val COLLECTION_NAME = "test_collection_0"
    }
}
