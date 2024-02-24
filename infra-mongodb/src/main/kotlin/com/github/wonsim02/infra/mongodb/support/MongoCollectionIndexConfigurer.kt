package com.github.wonsim02.infra.mongodb.support

import com.github.wonsim02.infra.mongodb.MongoIndexDefinitionSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity

/**
 * [MongoTemplate] 빈 생성 이후 [MongoTemplate.mappingContext]에 속한 Mongo 엔티티에 대하여 [MongoIndexDefinitionSource.locationPattern]로
 * 지정된 위치에서 리소스를 불러들여 인덱스를 추가한다.
 */
class MongoCollectionIndexConfigurer : BeanPostProcessor {

    private val logger = LoggerFactory.getLogger(this::class.java)

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
        // TODO
    }
}
