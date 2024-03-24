package com.github.wonsim02.infra.jpa.config

import com.github.womsim02.common.spring.ProfileAwarePropertySource
import org.springframework.context.annotation.Configuration

@Configuration
@ProfileAwarePropertySource(
    locations = [
        "/infra-jpa-config.yml",
        "/infra-jpa-config-*.yml",
    ],
)
class InfraJpaConfiguration
