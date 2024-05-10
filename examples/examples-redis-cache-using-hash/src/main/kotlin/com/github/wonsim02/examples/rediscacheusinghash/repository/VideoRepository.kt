package com.github.wonsim02.examples.rediscacheusinghash.repository

import com.github.wonsim02.examples.rediscacheusinghash.entity.JpaVideoEntity
import com.github.wonsim02.examples.rediscacheusinghash.entity.QJpaVideoEntity.jpaVideoEntity
import com.github.wonsim02.examples.rediscacheusinghash.model.Video
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

@Repository
class VideoRepository(
    private val jpaQueryFactory: JPAQueryFactory,
) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun create(title: String): Video {
        val entity = JpaVideoEntity(title = title)
        entityManager.persist(entity)
        return entity.toModel()
    }

    fun existsById(id: Long): Boolean {
        return entityManager.find(JpaVideoEntity::class.java, id) != null
    }

    fun fetch(ids: Collection<Long>): Map<Long, Video> {
        if (ids.isEmpty()) return mapOf()

        return jpaQueryFactory
            .selectFrom(jpaVideoEntity)
            .where(jpaVideoEntity.id.`in`(ids))
            .fetch()
            .associate { it.id to it.toModel() }
    }
}
