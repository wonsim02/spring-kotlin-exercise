package com.github.wonsim02.examples.rediscacheusinghash.repository

import com.github.womsim02.common.util.applyIfNotNull
import com.github.wonsim02.examples.rediscacheusinghash.entity.JpaPlayListEntity
import com.github.wonsim02.examples.rediscacheusinghash.entity.QJpaPlayListEntity.jpaPlayListEntity
import com.github.wonsim02.examples.rediscacheusinghash.model.PlayList
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

@Repository
class PlayListRepository(
    private val jpaQueryFactory: JPAQueryFactory,
) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun create(title: String, videoIds: List<Long>): PlayList {
        val entity = JpaPlayListEntity(title = title, videoIds = videoIds)
        entityManager.persist(entity)
        return entity.toModel()
    }

    fun list(cursor: Long?, limit: Long): List<PlayList> {
        if (limit <= 0L) return listOf()

        return jpaQueryFactory
            .selectFrom(jpaPlayListEntity)
            .applyIfNotNull(cursor) { where(jpaPlayListEntity.id.gt(it)) }
            .orderBy(jpaPlayListEntity.id.asc())
            .fetch()
            .map { it.toModel() }
    }
}
