package com.github.wonsim02.infra.mongodb.support

import com.github.wonsim02.infra.mongodb.MongoIndexDefinitionSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * [MongoTemplate] 빈 생성 이후 [MongoTemplate.mappingContext]에 속한 Mongo 엔티티에 대하여 [MongoIndexDefinitionSource.locationPattern]로
 * 지정된 위치에서 리소스를 불러들여 인덱스를 추가한다.
 */
class MongoCollectionIndexConfigurer : BeanPostProcessor {

    private val resourcePatternResolver = PathMatchingResourcePatternResolver()
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val indexDefinitionsCache: ConcurrentMap<String /* database */, ConcurrentMap<String /* collection */, List<IndexDefinitionFromResource>>> =
        ConcurrentHashMap()

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean !is MongoTemplate) return bean

        logger.info("Configuring indexes for MongoTemplate(beanName={})", beanName)
        for (entity in bean.converter.mappingContext.persistentEntities) {
            createIndexesIfNotExisting(bean, entity)
        }

        return bean
    }

    private fun createIndexesIfNotExisting(
        mongoTemplate: MongoTemplate,
        entity: MongoPersistentEntity<*>,
    ) {
        val indexDefinitionSource = entity
            .findAnnotation(MongoIndexDefinitionSource::class.java)
            ?: return
        val resources = try {
            resourcePatternResolver
                .getResources(indexDefinitionSource.locationPattern)
        } catch (t: FileNotFoundException) {
            // `locationPattern`에 맞는 리소스가 존재하지 않음
            return
        }

        val database = mongoTemplate.db.name
        val collection = entity.collection
        val indexOps = mongoTemplate.indexOps(collection, entity.type)

        val indexDefinitions = indexDefinitionsCache
            .computeIfAbsent(database) { ConcurrentHashMap() }
            .computeIfAbsent(collection) { resources.mapNotNull { buildIndexDefinition(it, collection) } }

        // TODO
    }

    /**
     * 주어진 리소스로부터 인덱스 정의를 생성한다.
     * 만약 리소스가 인덱스 정의를 생성하기에 적합하지 않으면 `null`을 반환한다.
     * @param resource 인덱스 정의를 생성할 리소스
     * @param collection 인덱스를 추가하려는 Mongo 콜렉션
     * @return [IndexDefinitionFromResource] or `null`
     */
    private fun buildIndexDefinition(
        resource: Resource,
        collection: String,
    ): IndexDefinitionFromResource? {
        val parseFilenameResult = resource
            .filename
            ?.let(MongoIndexDefinitionSource.Companion::parseFilename)
            ?: return null

        val definition = MongoIndexDefinitionBuilder.build(
            indexName = parseFilenameResult.indexName,
            jsonStr = String(resource.inputStream.readAllBytes(), StandardCharsets.UTF_8),
            collection = collection,
        ) ?: return null

        return IndexDefinitionFromResource(parseFilenameResult, definition)
    }
}
