package com.github.womsim02.common.spring

import com.github.womsim02.common.spring.internal.ProfileAwarePropertySourceRegistrar
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource

/**
 * [PropertySource]를 개선하여 리소스로부터 스프링 부트 속성을 불러올 때 스프링 부트 프로필에 따라 다르게 불러올 수 있도록 한 어노테이션.
 * [locations]에 위치한 리소스를 불러오기 전에 각 문자열의 `*`이 스프링 부트 프로필 값으로 대체되며, 불러온 리소스를 읽어들일 때는 YAML 형식을 따른다.
 * [locations]가 가리키는 두 개 이상의 리소스가 하나의 속성 값을 지정하면 [locations] 내에서 순서가 뒤인 리소스의 값으로 덮어씌워진다.
 *
 * 예를 들어, `resources` 경로에 `property.yaml`이
 * ```yaml
 * foo: bar
 * ```
 * 와 같이 등록되어 있고, `property-a.yaml`이
 * ```yaml
 * foo: baz
 * ```
 * 와 같이 등록되어 있을 때, [ProfileAwarePropertySource] 어노테이션을
 * ```kotlin
 * @ProfileAwarePropertySource(
 *     locations = [
 *         "classpath:/property.yaml",
 *         "classpath:/property-*.yaml",
 *     ],
 * )
 * ```
 * 와 같이 추가하면, 스프링 부트 프로필이 `a`일 때에는 `property-a.yaml`에서 지정된 값을 따라 `foo` 속성의 값이 `baz`이고, 그 외의 경우에는
 * `property-*.yaml`에 해당하는 리소스가 존재하지 않으므로 `property.yaml`에서 지정된 값을 따라 `foo` 속성의 값이 `bar`이 된다.
 */
@Import(ProfileAwarePropertySourceRegistrar::class)
annotation class ProfileAwarePropertySource(vararg val locations: String) {

    companion object {

        const val PROFILE_WILDCARD = "*"
        const val PROPERTY_SOURCE_NAME_PREFIX = "profileAwarePropertySource"
    }
}
