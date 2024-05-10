package com.github.wonsim02.examples.rediscacheusinghash.entity

import com.github.wonsim02.examples.rediscacheusinghash.model.Video
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity(name = "JpaVideoEntity")
@Table(
    schema = "public",
    name = "video",
)
class JpaVideoEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "title")
    val title: String,
) {

    fun toModel(): Video {
        return Video(
            id = id,
            title = title,
        )
    }
}
