package com.github.wonsim02.infra.jpa.config

import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Configuration
class QueryDslConfiguration {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Bean
    @ConditionalOnMissingBean(JPAQueryFactory::class)
    fun jpaQueryFactory(): JPAQueryFactory {
        return JPAQueryFactory(entityManager)
    }
}
