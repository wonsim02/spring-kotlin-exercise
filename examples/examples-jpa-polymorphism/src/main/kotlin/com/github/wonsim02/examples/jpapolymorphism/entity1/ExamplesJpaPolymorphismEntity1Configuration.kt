package com.github.wonsim02.examples.jpapolymorphism.entity1

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@ComponentScan(
    basePackages = ["com.github.wonsim02.examples.jpapolymorphism.entity1"],
)
@EntityScan(
    basePackages = ["com.github.wonsim02.examples.jpapolymorphism.entity1"],
)
@EnableJpaRepositories(
    basePackages = ["com.github.wonsim02.examples.jpapolymorphism.entity1"],
)
class ExamplesJpaPolymorphismEntity1Configuration
