package com.github.wonsim02.examples.usehibernateimpl.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EntityScan(basePackages = ["com.github.wonsim02.examples.usehibernateimpl.entity"])
@EnableJpaRepositories(basePackages = ["com.github.wonsim02.examples.usehibernateimpl.entity"])
class ExamplesUseHibernateImplConfiguration
