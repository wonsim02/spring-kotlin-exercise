package com.github.wonsim02.infra.jpa.config

import com.github.wonsim02.infra.jpa.InfraJpaIntegrationTestBase
import com.github.wonsim02.infra.jpa.entity.QTestEntity
import com.github.wonsim02.infra.jpa.entity.TestEntity
import com.github.wonsim02.infra.jpa.util.CustomSqlFunctionTemplates
import com.querydsl.core.types.ExpressionUtils
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.hibernate.cfg.AvailableSettings
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

class CustomPostgresDialectTest : InfraJpaIntegrationTestBase() {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var jpaProperties: JpaProperties

    @Autowired
    private lateinit var jpaQueryFactory: JPAQueryFactory

    @Test
    fun `dialect should be overwritten`() {
        assertEquals(
            CustomPostgresDialect::class.java.name,
            jpaProperties.properties[AvailableSettings.DIALECT],
        )
    }

    @Transactional
    @Test
    fun `newly added functions in CustomPostgresDialect works well`() {
        // given - save entities
        val entityId1 = TestEntity(stringProperty = "foo", arrayProperty = listOf("a"))
            .also(entityManager::persist)
            .id
        val entityId2 = TestEntity(stringProperty = "bar", arrayProperty = listOf("b"))
            .also(entityManager::persist)
            .id
        entityManager.flush()
        entityManager.clear()

        // when - array_agg
        val stringListType = listOf("a")::class.java
        val arrayAggResult = jpaQueryFactory
            .select(
                ExpressionUtils.template(
                    stringListType,
                    CustomSqlFunctionTemplates.arrayAgg(),
                    QTestEntity.testEntity.stringProperty,
                )
            )
            .from(QTestEntity.testEntity)
            .fetch()

        // then - verify array_agg result
        assertEquals(1, arrayAggResult.size)
        assertEquals(setOf("foo", "bar"), arrayAggResult[0].toSet())

        // when - array_append
        val arrayAppendResults = jpaQueryFactory
            .select(
                Projections.constructor(
                    IdAndArrayAppendResult::class.java,
                    QTestEntity.testEntity.id,
                    ExpressionUtils.template(
                        stringListType,
                        CustomSqlFunctionTemplates.arrayAppend(),
                        QTestEntity.testEntity.arrayProperty,
                        QTestEntity.testEntity.stringProperty,
                    )
                )
            )
            .from(QTestEntity.testEntity)
            .orderBy(QTestEntity.testEntity.id.asc())
            .fetch()

        // then - verify array_append result
        assertEquals(2, arrayAppendResults.size)
        assertEquals(
            IdAndArrayAppendResult(id = entityId1, arrayAppendResult = listOf("a", "foo")),
            arrayAppendResults[0],
        )
        assertEquals(
            IdAndArrayAppendResult(id = entityId2, arrayAppendResult = listOf("b", "bar")),
            arrayAppendResults[1],
        )
    }

    data class IdAndArrayAppendResult(
        val id: Long,
        val arrayAppendResult: List<String>,
    )
}
