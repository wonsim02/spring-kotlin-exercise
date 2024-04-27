package com.github.wonsim02.examples.sharerowidsequence.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration

@Configuration
@EntityScan(basePackages = ["com.github.wonsim02.examples.sharerowidsequence.entity"])
class ExamplesShareRowIdSequenceConfiguration
