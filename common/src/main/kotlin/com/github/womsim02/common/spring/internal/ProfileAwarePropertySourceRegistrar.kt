package com.github.womsim02.common.spring.internal

import com.github.womsim02.common.spring.ProfileAwarePropertySource
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.EnvironmentAware
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.ConfigurationClassPostProcessor
import org.springframework.context.annotation.PropertySource
import org.springframework.core.Ordered
import org.springframework.core.PriorityOrdered
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.core.type.classreading.MetadataReaderFactory
import java.util.Properties

/**
 * [ProfileAwarePropertySource] 어노테이션을 해석하여 스프링 부트 속성으로 등록하는 [BeanDefinitionRegistryPostProcessor].
 * @see ConfigurationClassPostProcessor.processConfigBeanDefinitions
 * @see org.springframework.context.annotation.ConfigurationClassParser.parse
 */
internal class ProfileAwarePropertySourceRegistrar :
    BeanDefinitionRegistryPostProcessor, PriorityOrdered, EnvironmentAware, ResourceLoaderAware {

    /**
     * [postProcessBeanDefinitionRegistry] 함수에서 스프링 부트 속성을 등록하려면 [ConfigurableEnvironment.getPropertySources]
     * 함수를 통해 스프링 부트 속성에 접근해아 하므로 [ConfigurableEnvironment] 값만 설정할 수 있도록 한다.
     */
    private lateinit var environment: ConfigurableEnvironment
    private lateinit var resourceLoader: ResourceLoader
    private lateinit var metadataReaderFactory: MetadataReaderFactory

    private val processedConfigurationClasses: MutableSet<Class<*>> = mutableSetOf()

    override fun setEnvironment(environment: Environment) {
        if (environment is ConfigurableEnvironment) {
            this.environment = environment
        }
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
        this.metadataReaderFactory = CachingMetadataReaderFactory(resourceLoader)
    }

    // does nothing
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) { }

    /**
     * [BeanDefinitionRegistry]에 등록된 모든 빈에 대하여 [parse] 함수를 호출한 후 결과 [ProfileAwarePropertySourceHolder]에
     * 대해 [ConfigurationClassPostProcessor.processConfigBeanDefinitions]의 나머지 과정을 거친다.
     *
     * 1. [ProfileAwarePropertySource.locations]을 실제 리소스 경로로 변환.
     * 2. 1의 결과값 경로에 위치한 YAML 형식의 리소스를 읽어들여 [Properties] 생성.
     * 3. 2로부터 [PropertiesPropertySource] 생성 후 스프링 부트 속성으로 등록.
     * 4. 중복 생성을 방지하기 위하여 [processedConfigurationClasses]에 빈 클래스 등록.
     *
     * @see ConfigurationClassPostProcessor.processConfigBeanDefinitions
     * @see org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass
     * @see org.springframework.context.annotation.ConfigurationClassParser.processPropertySource
     */
    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        // `environment`에 값이 지정되지 않았으면 아무런 작업도 진행하지 않는다.
        if (!this::environment.isInitialized) return

        // `registry`에 등록된 `bean` 정의로부터 `ProfileAwarePropertySourceHolder`를 추출한 후
        // `ProfileAwarePropertySourceHolder.order` 값에 따라 정렬한다.
        val propertySourceHolders = registry
            .beanDefinitionNames
            .mapNotNull { parse(it, registry) }
            .sortedBy { it.order }
        // 만약 스프링 부트 속성으로 등록 가능한 `ProfileAwarePropertySource` 어노테이션이 달린 빈이 없으면
        // 추가 작업을 진행하지 않는다.
        if (propertySourceHolders.isEmpty()) return

        // `propertySourceHolders`의 빈에 대하여 스프링 부트 속성 등록 과정을 진행한다.
        for (holder in propertySourceHolders) {
            // `ProfileAwarePropertySource.locations` 값을 실제 리소스 경로로 변환한다.
            val resolvedLocations = ProfileAwarePropertySourceUtil.resolveLocations(
                holder.annotation.locations,
                environment.activeProfiles,
            )

            // `resolvedLocations`에 위치한 YAML 형식의 리소스를 읽어들인다.
            val properties: Properties = ProfileAwarePropertySourceUtil.loadYamlProperties(
                resolvedLocations,
                resourceLoader,
            )

            // 읽어들인 속성으로부터 `PropertiesPropertySource`를 생성한 후 스프링 부트 속성으로 등록한다.
            val propertySource = PropertiesPropertySource(holder.propertySourceName, properties)
            environment.propertySources.addLast(propertySource)

            // 스프링 부트 속성 중복 등록을 방지하기 위해 `processedConfigurationClasses`에 빈 클래스를 추가한다.
            processedConfigurationClasses.add(holder.annotatedClass)
        }
    }

    /**
     * [ConfigurationClassPostProcessor.processConfigBeanDefinitions] 함수에서 빈으로부터 [PropertySource] 어노테이션을 추출하는
     * 로직을 참고하여 작성한 [ProfileAwarePropertySourceHolder] 추출 함수. 추출 과정은 다음과 같다:
     *
     * 1. [beanName]으로부터 [BeanDefinition]을 추출한 후 [ProfileAwarePropertySourceHolder] 추출 대상인 지 검사.
     *  ([ConfigurationClassPostProcessor.processConfigBeanDefinitions] 함수의 `configCandidates` 선택 로직 참고)
     * 2. [processedConfigurationClasses]에 [beanName]이 가리키는 빈의 타입이 이미 포함되어 있는 지 검사.
     *  (`ConfigurationClassParser`의 `processConfigurationClass()` 함수의 `configurationClasses`에 포함되어 있는지 검사하는 로직 참고)
     * 3. [beanName]이 가리키는 빈에 달린 [ProfileAwarePropertySource] 어노테이션을 찾은 후 [ProfileAwarePropertySourceHolder] 생성
     *  (`ConfigurationClassParser`의 `doProcessConfigurationClass()` 함수의 [PropertySource] 어노테이션을 추출하여 `processPropertySource()`
     *  함수를 호출하는 로직 참고)
     *
     * [ConfigurationClassPostProcessor.processConfigBeanDefinitions]에서는 대상 빈을 `ConfigurationClassUtils.getOrder()`
     * 함수의 결과값에 따라 정렬한 후 2, 3의 과정을 거치지만, 이 컴포넌트에서는 [ProfileAwarePropertySource] 어노테이션이 달린 빈에 대해서만
     * 추가 작업을 수행하므로, [ProfileAwarePropertySource] 어노테이션을 찾은 빈에 대해서만 [ProfileAwarePropertySourceHolder.order]
     * 값을 계산한 후 정렬을 진행한다.
     *
     * @see ConfigurationClassPostProcessor.processConfigBeanDefinitions
     * @see org.springframework.context.annotation.ConfigurationClassParser.processConfigurationClass
     * @see org.springframework.context.annotation.ConfigurationClassParser.doProcessConfigurationClass
     */
    private fun parse(
        beanName: String,
        registry: BeanDefinitionRegistry,
    ): ProfileAwarePropertySourceHolder? {
        val beanDefinition: BeanDefinition = registry.getBeanDefinition(beanName)
        if (!ProfileAwarePropertySourceUtil.isCandidate(beanDefinition, metadataReaderFactory)) {
            return null
        }

        val beanClassName = beanDefinition.beanClassName ?: return null
        val beanClass = runCatching { Class.forName(beanClassName) }
            .getOrElse { return null }
        if (processedConfigurationClasses.contains(beanClass)) return null

        val annotation = beanClass
            .annotations
            .asSequence()
            .filterIsInstance<ProfileAwarePropertySource>()
            .firstOrNull()
            ?: return null

        return ProfileAwarePropertySourceHolder(
            annotation = annotation,
            annotatedClass = beanClass,
            order = ProfileAwarePropertySourceUtil.getOrder(beanDefinition),
        )
    }

    /**
     * @see ConfigurationClassPostProcessor.getOrder
     */
    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }
}
