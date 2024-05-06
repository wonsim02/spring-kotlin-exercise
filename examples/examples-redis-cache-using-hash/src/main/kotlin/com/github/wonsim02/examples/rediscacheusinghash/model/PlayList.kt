package com.github.wonsim02.examples.rediscacheusinghash.model

import java.io.Serializable

data class PlayList(
    val id: Long,
    val title: String,
    val videoIds: List<Long>,
) : Serializable
