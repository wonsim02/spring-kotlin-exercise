package com.github.wonsim02.examples.rediscacheusinghash.entity

import com.github.wonsim02.examples.rediscacheusinghash.model.PlayList
import com.vladmihalcea.hibernate.type.array.ListArrayType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity(name = "JpaPlayListEntity")
@Table(
    schema = "public",
    name = "play_list",
)
@TypeDef(
    name = "list-array",
    typeClass = ListArrayType::class,
)
class JpaPlayListEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "title")
    val title: String,

    videoIds: List<Long>,
) {

    @Column(name = "video_ids", columnDefinition = "bigint[]")
    @Type(type = "list-array")
    val videoIds: List<Long> = videoIds.distinct()

    fun toModel(): PlayList {
        return PlayList(
            id = id,
            title = title,
            videoIds = videoIds,
        )
    }
}
