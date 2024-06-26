package com.github.wonsim02.examples.jpapolymorphism.entity2

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@ComponentScan(
    basePackages = ["com.github.wonsim02.examples.jpapolymorphism.entity2"],
)
@EntityScan(
    basePackages = ["com.github.wonsim02.examples.jpapolymorphism.entity2"],
)
@EnableJpaRepositories(
    basePackages = ["com.github.wonsim02.examples.jpapolymorphism.entity2"],
)
class ExamplesJpaPolymorphismEntity2Configuration
