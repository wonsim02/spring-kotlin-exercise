package com.github.wonsim02.examples.rediscacheusinghash.model

import java.io.Serializable

data class WatchHistory(
    val id: Long,
    val userId: Long,
    val videoId: Long,
) : Serializable
