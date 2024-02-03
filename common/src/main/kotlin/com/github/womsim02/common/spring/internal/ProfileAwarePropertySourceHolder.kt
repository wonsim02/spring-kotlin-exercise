package com.github.womsim02.common.spring.internal

import com.github.womsim02.common.spring.ProfileAwarePropertySource

/**
 * [ProfileAwarePropertySourceRegistrar]에서 [ProfileAwarePropertySource] 어노테이션이 달린 빈을 해석한 결과 DTO.
 * @property annotation 원본 빈에 달린 [ProfileAwarePropertySource] 어노테이션.
 * @property annotatedClass 원본 빈의 타입.
 * @property order 원본 빈의 순서. 해당 순서에 따라 [ProfileAwarePropertySourceRegistrar]에서 스프링 부트 속성으로 등록한다.
 * @see org.springframework.context.annotation.ConfigurationClassParser.SourceClass
 */
internal data class ProfileAwarePropertySourceHolder(
    val annotation: ProfileAwarePropertySource,
    val annotatedClass: Class<*>,
    val order: Int,
) {
    val propertySourceName: String
        get() = ProfileAwarePropertySource.PROPERTY_SOURCE_NAME_PREFIX + " attached to " + annotatedClass.name
}
