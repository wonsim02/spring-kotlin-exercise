package com.github.wonsim02.infra.mongodb.support

import org.bson.Document
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition
import org.springframework.data.mongodb.core.index.IndexDefinition
import org.springframework.data.mongodb.core.index.IndexOperations
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver
import org.springframework.util.ObjectUtils

object MongoIndexDefinitionBuilder {

    private const val KEYS = "keys"
    private const val OPTIONS = "options"

    /**
     * `createIndexes()` 명령어의 [options](https://www.mongodb.com/docs/manual/reference/method/db.collection.createIndex/#options-for-all-index-types)
     * 변수에 설정할 수 있는 속성.
     */
    private object Options {

        const val UNIQUE = "unique"
        const val SPARSE = "sparse"
    }

    /**
     * 주어진 Mongo 인덱스 속성으로부터 [IndexDefinition]을 생성한다.
     * 이 때 [background](https://www.mongodb.com/docs/v4.0/core/index-creation/#background-construction) 설정은 항상 `true`로
     * 설정된다.
     * 생성된 인덱스 정의는 추후 [IndexOperations.ensureIndex]의 변수로 사용된다.
     * @param indexName 인덱스 이름
     * @param jsonStr [db.collection.createIndex()](https://www.mongodb.com/docs/manual/reference/method/db.collection.createIndex/#db.collection.createindex)
     *  명령어의 변수로 사용될 JSON 형식의 인덱스 정의. `keys` 및 `options` 2개의 속성 값이 각각 `createIndex()` 명령어의 `keys` 및 `options`
     *  변수의 값이 된다.
     * @param collection 인덱스가 생성되는 Mongo 콜렉션의 이름
     * @return 생성된 [IndexDefinition]. 만약 [jsonStr]가 유효하지 않으면 `null`을 반환한다.
     * @see MongoPersistentEntityIndexResolver.createCompoundIndexDefinition
     */
    fun build(
        indexName: String,
        jsonStr: String,
        collection: String,
    ): IndexDefinition? {
        val parsed = runCatching { Document.parse(jsonStr) }
            .getOrElse { return null }
        val keys = parsed[KEYS]?.toDocument() ?: return null
        val indexDefinition = CompoundIndexDefinition(keys)

        // configure index name
        indexDefinition.named(indexName)

        // configure remaining index options
        parsed[OPTIONS]?.toDocument()?.let { options ->
            // configure unique
            val unique = options[Options.UNIQUE] as? Boolean
            if (unique == true) indexDefinition.unique()

            // configure sparse
            val sparse = options[Options.SPARSE] as? Boolean
            if (sparse == true) indexDefinition.sparse()
        }

        // always create indexes in background
        // see: https://www.mongodb.com/docs/v4.0/core/index-creation/#background-construction
        indexDefinition.background()

        return MongoPersistentEntityIndexResolver.IndexDefinitionHolder("", indexDefinition, collection)
    }

    private fun Any.toDocument(): Document? {
        return this as? Document
            ?: runCatching { Document.parse(ObjectUtils.nullSafeToString(this)) }
                .getOrNull()
    }
}
