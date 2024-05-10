package com.github.wonsim02.examples.rediscacheusinghash.model

import java.io.Serializable

data class User(
    val id: Long,
    val name: String,
) : Serializable
