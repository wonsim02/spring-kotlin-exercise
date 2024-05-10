package com.github.wonsim02.infra.redis.util

import org.springframework.data.redis.serializer.RedisSerializer

class ByteArrayRedisSerializer : RedisSerializer<ByteArray> {

    override fun serialize(t: ByteArray?): ByteArray? {
        return t
    }

    override fun deserialize(bytes: ByteArray?): ByteArray? {
        return bytes
    }
}
