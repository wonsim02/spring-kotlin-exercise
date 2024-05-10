package com.github.wonsim02.examples.rediscacheusinghash.service

import com.github.wonsim02.examples.rediscacheusinghash.cache.UserCache
import com.github.wonsim02.examples.rediscacheusinghash.model.User
import com.github.wonsim02.examples.rediscacheusinghash.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userCache: UserCache,
    private val userRepository: UserRepository,
) {

    @Throws(UserNotFoundException::class)
    fun getUser(userId: Long): User {
        userCache.get(userId)?.let { return it }

        return userRepository
            .findById(userId)
            ?.let(userCache::put)
            ?: throw UserNotFoundException(userId)
    }

    class UserNotFoundException(userId: Long) : RuntimeException("User with id=$userId not found.")
}
