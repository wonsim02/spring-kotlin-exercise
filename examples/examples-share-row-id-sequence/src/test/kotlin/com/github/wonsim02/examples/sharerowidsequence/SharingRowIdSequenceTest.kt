package com.github.wonsim02.examples.sharerowidsequence

import com.github.wonsim02.examples.sharerowidsequence.config.ExamplesShareRowIdSequenceConfiguration
import com.github.wonsim02.examples.sharerowidsequence.entity.Cat
import com.github.wonsim02.examples.sharerowidsequence.entity.Dog
import com.github.wonsim02.examples.sharerowidsequence.entity.QCat
import com.github.wonsim02.examples.sharerowidsequence.entity.QDog
import com.github.wonsim02.infra.jpa.CustomPostgresqlContainer
import com.querydsl.jpa.impl.JPAQueryFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.junit.jupiter.Container
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import kotlin.random.Random

/**
 * [Cat] 엔티티와 [Dog] 엔티티를 동시에 추가할 때 두 테이블 간 중복된 PK가 발생하지 않는지 검사한다.
 *
 * 테스트를 시작하면 우선 100회에 걸쳐 1회 동안 동시에 100개의 [Cat] 혹은 [Dog] 엔티티를 추가한다.
 * 즉, 엔티티 추가 과정을 거치고 나면 총 10000개의 [Cat] 혹은 [Dog] 엔티티가 추가된 상태이다.
 * 이후에 두 테이블 간에 동일한 `petId` 값을 가지는 [Cat] 혹은 [Dog] 행이 존재하는 지 검사한다.
 *
 * @see db.migration
 */
@SpringBootTest(classes = [ExamplesShareRowIdSequenceConfiguration::class])
@EnableAutoConfiguration
class SharingRowIdSequenceTest {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var jpaQueryFactory: JPAQueryFactory

    @Autowired
    private lateinit var transactionMananger: PlatformTransactionManager

    @Test
    fun `concurrently adding Cat or Dog rows does not cause duplicated petId`() {
        // given - add cat / dog rows
        repeat(100) {
            val threads = List(100) {
                Thread { addCatOrDogRow() }
                    .apply { name = "add-cat-or-dog-row-$it" }
            }

            threads.forEach { it.start() }
            threads.forEach { it.join() }
        }

        // given - check total counts
        val catTotalCount = jpaQueryFactory
            .select(QCat.cat.petId.count())
            .from(QCat.cat)
            .fetchFirst()
        val dogTotalCount = jpaQueryFactory
            .select(QDog.dog.petId.count())
            .from(QDog.dog)
            .fetchFirst()
        assertEquals(10000L, catTotalCount + dogTotalCount)

        // when - list duplicated pet IDs
        val duplicatedPetIds = jpaQueryFactory
            .select(QCat.cat.petId)
            .from(QCat.cat)
            .innerJoin(QDog.dog)
            .on(QCat.cat.petId.eq(QDog.dog.petId))
            .fetch()

        // then - no duplicated pet ID
        assertEquals(0, duplicatedPetIds.size)
    }

    private fun addCatOrDogRow() {
        val isCat = Random.nextBoolean()
        @Suppress("IMPLICIT_CAST_TO_ANY")
        val entity = if (isCat) {
            Cat(name = genName(), species = allCatSpecies.random())
        } else {
            Dog(name = genName(), species = allDogSpecies.random())
        }

        TransactionTemplate(transactionMananger).execute {
            entityManager.persist(entity)
        }
    }

    companion object {

        private val allCatSpecies = Cat.Species.values().toList()
        private val allDogSpecies = Dog.Species.values().toList()

        // A-Z
        private val characters = (1..26).map { Char(it + 64) }

        private fun genName(): String {
            return (1..3).map { characters.random() }.joinToString("")
        }

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
