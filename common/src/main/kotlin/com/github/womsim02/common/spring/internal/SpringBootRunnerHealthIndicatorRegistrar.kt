package com.github.womsim02.common.spring.internal

import org.springframework.context.annotation.Bean

internal class SpringBootRunnerHealthIndicatorRegistrar {

    @Bean
    fun springBootRunnerHealthIndicator(): SpringBootRunnerHealthIndicator {
        return SpringBootRunnerHealthIndicator()
    }
}
