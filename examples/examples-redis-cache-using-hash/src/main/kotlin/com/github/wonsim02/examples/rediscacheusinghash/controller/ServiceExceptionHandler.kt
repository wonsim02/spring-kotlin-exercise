package com.github.wonsim02.examples.rediscacheusinghash.controller

import com.github.wonsim02.examples.rediscacheusinghash.service.PlayListService
import com.github.wonsim02.examples.rediscacheusinghash.service.WatchHistoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ServiceExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(PlayListService.InvalidUserIdException::class)
    fun handleException(ex: PlayListService.InvalidUserIdException): ResponseEntity<String> {
        return buildBadRequest(ex)
    }

    @ExceptionHandler(WatchHistoryService.InvalidUserIdException::class)
    fun handleException(ex: WatchHistoryService.InvalidUserIdException): ResponseEntity<String> {
        return buildBadRequest(ex)
    }

    @ExceptionHandler(WatchHistoryService.InvalidVideoIdException::class)
    fun handleException(ex: WatchHistoryService.InvalidVideoIdException): ResponseEntity<String> {
        return buildBadRequest(ex)
    }

    private fun buildBadRequest(ex: Throwable): ResponseEntity<String> {
        return ResponseEntity.badRequest().body(ex.message)
    }
}
