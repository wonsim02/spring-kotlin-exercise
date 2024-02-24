package com.github.wonsim02.infra.mongodb.support

import com.github.wonsim02.infra.mongodb.MongoIndexDefinitionSource
import org.springframework.data.mongodb.core.index.IndexDefinition

internal data class IndexDefinitionFromResource(
    val parseFilenameResult: MongoIndexDefinitionSource.Companion.ParseFilenameResult,
    val indexDefinition: IndexDefinition,
) : IndexDefinition by indexDefinition
