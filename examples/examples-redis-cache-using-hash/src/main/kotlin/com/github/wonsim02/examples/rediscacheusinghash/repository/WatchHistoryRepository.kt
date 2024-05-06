package com.github.wonsim02.examples.rediscacheusinghash.repository

import com.github.wonsim02.examples.rediscacheusinghash.dto.WatchHistoryCounts
import com.github.wonsim02.examples.rediscacheusinghash.entity.JpaWatchHistoryEntity
import com.github.wonsim02.examples.rediscacheusinghash.entity.QJpaWatchHistoryEntity.jpaWatchHistoryEntity
import com.github.wonsim02.examples.rediscacheusinghash.model.WatchHistory
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

@Repository
class WatchHistoryRepository(
    private val jpaQueryFactory: JPAQueryFactory,
) {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun create(userId: Long, videoId: Long): WatchHistory {
        val entity = JpaWatchHistoryEntity(userId = userId, videoId = videoId)
        entityManager.persist(entity)
        return entity.toModel()
    }

    fun countByUserIdAndVideoIds(
        userId: Long,
        videoIds: Collection<Long>,
    ): WatchHistoryCounts {
        if (videoIds.isEmpty()) return mapOf()

        return jpaQueryFactory
            .select(
                Projections.constructor(
                    VideoIdAndCount::class.java,
                    jpaWatchHistoryEntity.videoId,
                    jpaWatchHistoryEntity.count(),
                )
            )
            .from(jpaWatchHistoryEntity)
            .where(jpaWatchHistoryEntity.userId.eq(userId))
            .where(jpaWatchHistoryEntity.videoId.`in`(videoIds))
            .groupBy(jpaWatchHistoryEntity.videoId)
            .fetch()
            .asSequence()
            .mapNotNull { it.count?.to(it.videoId) }
            .associate { it.second to it.first }
    }

    data class VideoIdAndCount(
        val videoId: Long,
        val count: Long?,
    )
}
