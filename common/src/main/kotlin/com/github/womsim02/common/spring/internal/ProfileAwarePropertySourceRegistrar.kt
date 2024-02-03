package com.github.womsim02.common.spring.internal

import com.github.womsim02.common.spring.ProfileAwarePropertySource
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.EnvironmentAware
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ConfigurationClassPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.PriorityOrdered
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.core.type.classreading.MetadataReaderFactory

/**
 * [ProfileAwarePropertySource] 어노테이션을 해석하여 스프링 부트 속성으로 등록하는 [BeanDefinitionRegistryPostProcessor].
 * @see ConfigurationClassPostProcessor.processConfigBeanDefinitions
 * @see org.springframework.context.annotation.ConfigurationClassParser.parse
 */
internal class ProfileAwarePropertySourceRegistrar :
    BeanDefinitionRegistryPostProcessor, PriorityOrdered, EnvironmentAware, ResourceLoaderAware {

    private lateinit var environment: Environment
    private lateinit var resourceLoader: ResourceLoader
    private lateinit var metadataReaderFactory: MetadataReaderFactory

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
        this.metadataReaderFactory = CachingMetadataReaderFactory(resourceLoader)
    }

    // does nothing
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) { }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        // TODO
    }

    /**
     * @see ConfigurationClassPostProcessor.getOrder
     */
    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }
}
