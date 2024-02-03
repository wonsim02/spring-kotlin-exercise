package com.github.wonsim02.infra.mongodb.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@EnableConfigurationProperties(
    AdditionalMongodbProperties::class,
)
@Import(
    CustomizingMongoClientConfiguration::class,
)
class InfraMongodbConfiguration
