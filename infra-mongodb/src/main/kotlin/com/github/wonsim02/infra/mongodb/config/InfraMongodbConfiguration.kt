package com.github.wonsim02.infra.mongodb.config

import com.github.womsim02.common.spring.ProfileAwarePropertySource
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@EnableConfigurationProperties(
    AdditionalMongodbProperties::class,
)
@Import(
    AdditionalMongoDatabasesConfiguration::class,
    CustomizingMongoClientConfiguration::class,
    PrimaryMongoDatabaseConfiguration::class,
)
@ProfileAwarePropertySource(
    locations = [
        "classpath:/infra-mongodb-config.yml",
        "classpath:/infra-mongodb-config-*.yml",
    ]
)
class InfraMongodbConfiguration
