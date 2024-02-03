package com.github.wonsim02.infra.mongodb.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    AdditionalMongodbProperties::class,
)
class InfraMongodbConfiguration
