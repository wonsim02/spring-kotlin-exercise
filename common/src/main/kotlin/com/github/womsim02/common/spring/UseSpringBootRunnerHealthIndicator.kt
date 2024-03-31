package com.github.womsim02.common.spring

import com.github.womsim02.common.spring.internal.SpringBootRunnerHealthIndicator
import com.github.womsim02.common.spring.internal.SpringBootRunnerHealthIndicatorRegistrar
import org.springframework.context.annotation.Import

/**
 * [SpringBootRunnerHealthIndicator]를 bean으로 등록하여 health 체크 시 runner 동작 완료 여부도 함께 체크하도록 한다.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(SpringBootRunnerHealthIndicatorRegistrar::class)
annotation class UseSpringBootRunnerHealthIndicator
