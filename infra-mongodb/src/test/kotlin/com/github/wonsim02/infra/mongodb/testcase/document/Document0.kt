package com.github.wonsim02.infra.mongodb.testcase.document

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = Document0.COLLECTION_NAME)
data class Document0(
    val id: Long,
) {

    companion object {

        const val COLLECTION_NAME = "test_collection_0"
    }
}
