package com.github.wonsim02.infra.mongodb.support

import com.github.wonsim02.infra.mongodb.UseMongoDatabase
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.autoconfigure.domain.EntityScanPackages
import org.springframework.boot.autoconfigure.domain.EntityScanner
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils

/**
 * @[Document]로 지정된 엔티티 중 주어진 Mongo 데이터베이스에 기록될 엔티티를 검색하는 유틸.
 * @property context 엔티티 후보를 조회할 때 사용할 [ApplicationContext]
 * @property database 대상 Mongo 데이터베이스의 이름
 * @property isPrimary 대상이 기본 Mongo 데이터베이스인지 여부
 */
class MongoDocumentScanner(
    private val context: ApplicationContext,
    private val database: String,
    private val isPrimary: Boolean,
) : EntityScanner(context) {

    /**
     * [EntityScanner.scan]의 동작에 검색한 엔티티 클래스가 [isInitialEntity] 조건을 만족하는 지 검사하는 과정을 추가한 함수.
     * @see EntityScanner.scan
     */
    fun scanDocuments(): Set<Class<*>> {
        val packages = getPackages().ifEmpty { return emptySet() }
        val scanner = createClassPathScanningCandidateComponentProvider(context)
        scanner.addIncludeFilter(AnnotationTypeFilter(Document::class.java))

        return packages
            .asSequence()
            .filter(StringUtils::hasText)
            .flatMap(scanner::findCandidateComponents)
            .mapNotNull(BeanDefinition::getBeanClassName)
            .map { beanClassName -> ClassUtils.forName(beanClassName, context.classLoader) }
            .filter(this::isInitialEntity) // 해당 filter 로직이 `EntityScanner.scan()`에는 없던 로직이다
            .toSet()
    }

    /**
     * @[Document]로 지정된 엔티티가 이름이 [database]인 Mongo 데이터베이스에 기록될 지 판별한다.
     * - 만약 [documentClass]에 [UseMongoDatabase] 어노테이션이 달려있지 않으면 기본 Mongo 데이터베이스에만 기록됨을 의미하므로,
     *  [isPrimary]가 `true`이면 `true`, [isPrimary]가 `false`이면 `false`이다. 즉, [isPrimary] 값을 그대로 따라간다.
     * - [documentClass]에 달린 [UseMongoDatabase] 어노테이션을 찾았으면 [database]가 [UseMongoDatabase.databases]에 포함되어 있는지
     *  검사한다.
     * @see UseMongoDatabase
     */
    private fun isInitialEntity(documentClass: Class<*>): Boolean {
        val targetAnnotation = AnnotationUtils
            .findAnnotation(documentClass, UseMongoDatabase::class.java)
            // `@UseMongoDatabase` 어노테이션을 찾지 못하면 `isPrimary=true`인 데이터베이스에만 기록됨
            ?: return isPrimary
        return targetAnnotation.databases.contains(database)
    }

    /**
     * @see EntityScanner.getPackages
     */
    private fun getPackages(): List<String> {
        val packages: List<String> = EntityScanPackages.get(context).packageNames
        return if (packages.isEmpty() && AutoConfigurationPackages.has(context)) {
            AutoConfigurationPackages.get(context)
        } else {
            packages
        }
    }
}
