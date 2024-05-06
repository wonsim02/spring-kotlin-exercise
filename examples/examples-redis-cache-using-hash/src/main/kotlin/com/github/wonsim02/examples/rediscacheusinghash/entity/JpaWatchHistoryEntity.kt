package com.github.wonsim02.examples.rediscacheusinghash.entity

import com.github.wonsim02.examples.rediscacheusinghash.model.WatchHistory
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity(name = "JpaWatchHistoryEntity")
@Table(
    schema = "public",
    name = "watch_history",
)
class JpaWatchHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "user_id")
    val userId: Long,

    @Column(name = "video_id")
    val videoId: Long,
) {

    fun toModel(): WatchHistory {
        return WatchHistory(
            id = id,
            userId = userId,
            videoId = videoId,
        )
    }
}
