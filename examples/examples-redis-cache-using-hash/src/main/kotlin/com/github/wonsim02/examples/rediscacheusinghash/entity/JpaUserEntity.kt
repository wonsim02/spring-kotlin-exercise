package com.github.wonsim02.examples.rediscacheusinghash.entity

import com.github.wonsim02.examples.rediscacheusinghash.model.User
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity(name = "JpaUserEntity")
@Table(
    schema = "public",
    name = "user",
)
class JpaUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "name")
    val name: String,
) {

    fun toModel(): User {
        return User(
            id = id,
            name = name,
        )
    }
}
