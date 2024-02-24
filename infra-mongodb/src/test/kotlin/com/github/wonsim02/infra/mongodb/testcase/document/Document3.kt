package com.github.wonsim02.infra.mongodb.testcase.document

import com.github.wonsim02.infra.mongodb.InfraMongodbTestBase
import com.github.wonsim02.infra.mongodb.UseMongoDatabase
import org.springframework.data.mongodb.core.mapping.Document

@UseMongoDatabase(InfraMongodbTestBase.DATABASE)
@Document(collection = Document3.COLLECTION_NAME)
data class Document3(
    val id: Long,
) {

    companion object {

        const val COLLECTION_NAME = "test_collection_3"
    }
}
