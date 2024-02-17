package com.github.wonsim02.infra.mongodb

import org.springframework.data.mongodb.core.mapping.Document
import java.lang.annotation.Inherited

/**
 * @[Document]로 지정된 엔티티에 어떤 Mongo 데이터베이스에 기록될 지 명시할 수 있는 어노테이션.
 * @[Document]에 해당 어노테이션이 존재하지 않으면 기본 Mongo 데이터베이스에만 기록된다.
 * @property databases 해당 엔티티를 기록할 Mongo 데이터베이스
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class UseMongoDatabase(vararg val databases: String)
