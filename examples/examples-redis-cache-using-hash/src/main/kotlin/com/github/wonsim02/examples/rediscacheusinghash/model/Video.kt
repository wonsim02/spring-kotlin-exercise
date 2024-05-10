package com.github.wonsim02.examples.rediscacheusinghash.model

import java.io.Serializable

data class Video(
    val id: Long,
    val title: String,
) : Serializable
