package com.github.womsim02.common.spring.internal

import com.github.womsim02.common.spring.ProfileAwarePropertySource
import org.springframework.aop.framework.AopInfrastructureBean
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ConfigurationClassPostProcessor
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportResource
import org.springframework.context.event.EventListenerFactory
import org.springframework.core.Conventions
import org.springframework.core.Ordered
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.stereotype.Component
import java.util.Properties

/**
 * [ProfileAwarePropertySourceRegistrar]에서 [ProfileAwarePropertySource]를 해석하는 데 도움이 되는 함수를 모아 둔 오브젝트.
 */
internal object ProfileAwarePropertySourceUtil {

    /**
     * @see org.springframework.context.annotation.ConfigurationClassUtils.ORDER_ATTRIBUTE
     */
    private val ORDER_ATTRIBUTE =
        Conventions.getQualifiedAttributeName(ConfigurationClassPostProcessor::class.java, "order")

    /**
     * @see org.springframework.context.annotation.ConfigurationClassUtils.candidateIndicators
     */
    private val CANDIDATE_INDICATORS = setOf(
        Component::class.java.name,
        ComponentScan::class.java.name,
        Import::class.java.name,
        ImportResource::class.java.name,
    )

    /**
     * @see org.springframework.context.annotation.ConfigurationClassUtils.getOrder
     */
    internal fun getOrder(beanDef: BeanDefinition): Int {
        return runCatching { beanDef.getAttribute(ORDER_ATTRIBUTE) as Int }
            .getOrDefault(Ordered.LOWEST_PRECEDENCE)
    }

    /**
     * [ConfigurationClassPostProcessor.processConfigBeanDefinitions] 함수 내에서 호출되는 `ConfigurationClassUtils`의
     * `checkConfigurationClassCandidate()` 함수를 kotlin으로 재작성한 함수.
     * @see org.springframework.context.annotation.ConfigurationClassUtils.checkConfigurationClassCandidate
     */
    internal fun isCandidate(
        beanDef: BeanDefinition,
        metadataReaderFactory: MetadataReaderFactory,
    ): Boolean {
        val className = beanDef.beanClassName
        if (className == null || beanDef.factoryMethodName != null) {
            return false
        }

        val metadata: AnnotationMetadata = when {
            beanDef is AnnotatedBeanDefinition && beanDef.metadata.className == className
            -> beanDef.metadata

            beanDef is AbstractBeanDefinition && beanDef.hasBeanClass() -> {
                val beanClass = beanDef.beanClass
                if (BeanFactoryPostProcessor::class.java.isAssignableFrom(beanClass) ||
                    BeanPostProcessor::class.java.isAssignableFrom(beanClass) ||
                    AopInfrastructureBean::class.java.isAssignableFrom(beanClass) ||
                    EventListenerFactory::class.java.isAssignableFrom(beanClass)
                ) {
                    return false
                } else AnnotationMetadata.introspect(beanClass)
            }

            else
            -> runCatching { metadataReaderFactory.getMetadataReader(className).annotationMetadata }
                .getOrElse { return false }
        }

        val config = metadata.getAnnotationAttributes(Configuration::class.java.name)
        return if (config != null && java.lang.Boolean.FALSE != config["proxyBeanMethods"]) {
            true
        } else config != null || isCandidate(metadata)
    }

    /**
     * @see org.springframework.context.annotation.ConfigurationClassUtils.isConfigurationCandidate
     */
    private fun isCandidate(metadata: AnnotationMetadata): Boolean {
        if (metadata.isInterface) return false
        if (CANDIDATE_INDICATORS.any(metadata::isAnnotated)) return true
        return runCatching { metadata.hasAnnotatedMethods(Bean::class.java.name) }
            .getOrDefault(false)
    }

    /**
     * [activeProfiles]의 정보를 참조하여 [locations]의 각 원소를 실제 리소스의 경로로 변환한다.
     * @see ProfileAwarePropertySource
     */
    internal fun resolveLocations(locations: Array<out String>, activeProfiles: Array<String>): List<String> {
        val profiles = activeProfiles.plus("")
        return locations
            .asSequence()
            .flatMap { location ->
                profiles
                    .asSequence()
                    .map { location.replace(ProfileAwarePropertySource.PROFILE_WILDCARD, it) }
            }
            .distinct()
            .toList()
    }

    /**
     * `ConfigurationClassParser`의 `processPropertySource()` 함수의 로직 중 어노테이션에서 리소스 위치 정보를 추출한 뒤 불러와
     * [Properties]로 변환하는 부분만을 구현한 함수. [locations]에 위치한 리소스가 YAML 형식임을 상정한다.
     * @see org.springframework.context.annotation.ConfigurationClassParser.processPropertySource
     * @see org.springframework.context.annotation.ConfigurationClassParser.addPropertySource
     */
    internal fun loadYamlProperties(
        locations: Iterable<String>,
        resourceLoader: ResourceLoader,
    ): Properties {
        val resources = locations.mapNotNull { location ->
            try {
                resourceLoader
                    .getResource(location)
                    .inputStream
                    .let { InputStreamResource(it) }
            } catch (t: Throwable) {
                null
            }
        }

        val yamlPropFactory = YamlPropertiesFactoryBean()
        yamlPropFactory.setResources(*resources.toTypedArray())
        return yamlPropFactory.`object` ?: Properties()
    }
}
