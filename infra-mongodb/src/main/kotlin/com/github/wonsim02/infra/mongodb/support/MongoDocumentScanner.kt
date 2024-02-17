package com.github.wonsim02.infra.mongodb.support

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.autoconfigure.domain.EntityScanPackages
import org.springframework.boot.autoconfigure.domain.EntityScanner
import org.springframework.context.ApplicationContext
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils

/**
 * [EntityScanner.scan] 동작을 @[Document]에 대해서 진행하는 유틸.
 */
class MongoDocumentScanner(
    private val context: ApplicationContext,
) : EntityScanner(context) {

    /**
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
            .toSet()
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
