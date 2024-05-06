package com.github.wonsim02.examples.rediscacheusinghash.repository

import com.github.wonsim02.examples.rediscacheusinghash.entity.JpaUserEntity
import com.github.wonsim02.examples.rediscacheusinghash.model.User
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.Transactional

@Repository
class UserRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional
    fun create(name: String): User {
        val entity = JpaUserEntity(name = name)
        entityManager.persist(entity)
        return entity.toModel()
    }

    fun findById(id: Long): User? {
        return entityManager
            .find(JpaUserEntity::class.java, id)
            ?.toModel()
    }
}
