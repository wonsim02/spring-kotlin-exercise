package com.github.wonsim02.examples.usehibernateimpl

import com.github.wonsim02.examples.usehibernateimpl.config.ExamplesUseHibernateImplConfiguration
import com.github.wonsim02.examples.usehibernateimpl.entity.SampleEntity
import com.github.wonsim02.examples.usehibernateimpl.entity.SampleEntity.Companion.NON_UNIQUE_PROPERTY
import com.github.wonsim02.examples.usehibernateimpl.entity.SampleEntity.Companion.UNIQUE_PROPERTY
import com.github.wonsim02.examples.usehibernateimpl.entity.SampleEntityJpaRepository
import com.github.wonsim02.examples.usehibernateimpl.util.performConstraintViolationSafeInsertion
import com.github.wonsim02.infra.jpa.CustomPostgresqlContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@SpringBootTest(classes = [HibernateExtensionsTest.App::class])
class HibernateExtensionsTest {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var sampleEntityJpaRepository: SampleEntityJpaRepository

    @Test
    @Transactional
    fun `performConstraintViolationSafeInsertion() works as expected`() {
        // given - insert one entity
        sampleEntityJpaRepository.saveAndFlush(
            SampleEntity(
                id = 1L,
                uniqueProperty = "foo",
                nonUniqueProperty = 1,
            )
        )

        // when - insert another entity with performConstraintViolationSafeInsertion()
        val inserted1 = performConstraintViolationSafeInsertion(
            entity = SampleEntity(
                id = 2L,
                uniqueProperty = "bar",
                nonUniqueProperty = 2,
            ),
            entityManager = entityManager,
            entityIdGetter = SampleEntity::id,
        )

        // then - inserted
        assertTrue(inserted1)

        // when - findAll()
        val allEntities1 = sampleEntityJpaRepository
            .findAll()
            .sortedBy { it.id }

        // then - verify findAll() result
        // two entities found (one inserted by save(), another inserted by performConstraintViolationSafeInsertion())
        assertEquals(2, allEntities1.size)
        with(allEntities1[0]) {
            assertEquals(1L, id)
            assertEquals("foo", uniqueProperty)
            assertEquals(1, nonUniqueProperty)
        }
        with(allEntities1[1]) {
            assertEquals(2L, id)
            assertEquals("bar", uniqueProperty)
            assertEquals(2, nonUniqueProperty)
        }

        // when - insert entity with already-existing uniqueProperty value
        val inserted2 = performConstraintViolationSafeInsertion(
            entity = SampleEntity(
                id = 3L,
                uniqueProperty = "foo",
                nonUniqueProperty = 3,
            ),
            entityManager = entityManager,
            entityIdGetter = SampleEntity::id,
        )

        // then - not inserted
        assertFalse(inserted2)

        // when - findAll()
        val allEntities2 = sampleEntityJpaRepository
            .findAll()
            .onEach(entityManager::refresh)
            .sortedBy { it.id }

        // then - verify findAll() result
        // no entities inserted or updated
        assertEquals(2, allEntities2.size)
        with(allEntities2[0]) {
            assertEquals(1L, id)
            assertEquals("foo", uniqueProperty)
            assertEquals(1, nonUniqueProperty)
        }
        with(allEntities2[1]) {
            assertEquals(2L, id)
            assertEquals("bar", uniqueProperty)
            assertEquals(2, nonUniqueProperty)
        }

        // when - insert entity with on conflict update clause
        val inserted3 = performConstraintViolationSafeInsertion(
            entity = SampleEntity(
                id = 4L,
                uniqueProperty = "bar",
                nonUniqueProperty = 4,
            ),
            entityManager = entityManager,
            onConflictStatement = "($UNIQUE_PROPERTY) do update set $NON_UNIQUE_PROPERTY = excluded.$NON_UNIQUE_PROPERTY",
            entityIdGetter = SampleEntity::id,
        )

        // then - inserted (actually upserted)
        assertTrue(inserted3)

        // when - findAll()
        val allEntities3 = sampleEntityJpaRepository
            .findAll()
            .onEach(entityManager::refresh)
            .sortedBy { it.id }

        // then - verify findAll() result
        // number of entities not increased, but entity with id=2L updated
        assertEquals(2, allEntities3.size)
        with(allEntities3[0]) {
            assertEquals(1L, id)
            assertEquals("foo", uniqueProperty)
            assertEquals(1, nonUniqueProperty)
        }
        with(allEntities3[1]) {
            assertEquals(2L, id)
            assertEquals("bar", uniqueProperty)
            assertEquals(4, nonUniqueProperty)
        }
    }

    @SpringBootApplication
    @Import(ExamplesUseHibernateImplConfiguration::class)
    class App

    companion object {

        @Container
        @JvmStatic
        val testContainer = CustomPostgresqlContainer(
            database = "database",
            username = "username",
            password = "password",
        )

        @DynamicPropertySource
        @JvmStatic
        @Suppress("unused")
        fun setTestDatabaseProperties(registry: DynamicPropertyRegistry) {
            testContainer.start()
            testContainer.setTestContainerProperties(registry)
        }
    }
}
