package com.github.wonsim02.examples.rediscacheusinghash.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.Throws

@Configuration
class UserIdHeaderParameterConfiguration {

    @Bean
    fun requestContext(): UserRequestContext = UserRequestContext()

    @Bean
    fun userRequestContextFilter(
        userRequestContext: UserRequestContext,
    ): OncePerRequestFilter = object : OncePerRequestFilter() {

        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain,
        ) {
            val userId = request.getHeader(USER_ID_HEADER)?.toLong()
            if (userId != null) { userRequestContext.userId = userId }

            try {
                filterChain.doFilter(request, response)
            } catch (t: Throwable) {
                logger.error("Handle exception", t)
                throw t
            } finally {
                userRequestContext.clear()
            }
        }
    }

    class UserRequestContext {

        private val _userId: ThreadLocal<Long?> = ThreadLocal.withInitial { null }

        @get:Throws(UserIdNotSetException::class)
        var userId: Long
            get() = _userId.get() ?: throw UserIdNotSetException()
            set(value) { _userId.set(value) }

        fun clear() {
            _userId.remove()
        }

        class UserIdNotSetException : RuntimeException("\"$USER_ID_HEADER\" header not set.")
    }

    @RestControllerAdvice
    class UserIdNotSetExceptionHandler : ResponseEntityExceptionHandler() {

        @ExceptionHandler(UserRequestContext.UserIdNotSetException::class)
        fun handleException(ex: UserRequestContext.UserIdNotSetException): ResponseEntity<String> {
            return ResponseEntity.badRequest().body(ex.message)
        }
    }

    companion object {

        const val USER_ID_HEADER = "x-user-id"
    }
}
