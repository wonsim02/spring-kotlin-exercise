package com.github.wonsim02.examples.rediscacheusinghash.service

import com.github.wonsim02.examples.rediscacheusinghash.cache.VideoCache
import com.github.wonsim02.examples.rediscacheusinghash.model.Video
import com.github.wonsim02.examples.rediscacheusinghash.repository.VideoRepository
import org.springframework.stereotype.Service

@Service
class VideoService(
    private val videoCache: VideoCache,
    private val videoRepository: VideoRepository,
) {

    fun existsByVideoId(videoId: Long): Boolean {
        if (videoCache.get(videoId) != null) return true
        return videoRepository.existsById(videoId)
    }

    fun fetch(videoIds: Collection<Long>): Map<Long, Video> {
        if (videoIds.isEmpty()) return mapOf()

        val cached = videoCache.multiGet(videoIds)
        val notCachedIds = videoIds.toSet() - cached.keys
        if (notCachedIds.isEmpty()) return cached

        val fromRepository = videoRepository.fetch(notCachedIds)
        fromRepository.values.forEach(videoCache::put)

        return cached + fromRepository
    }
}
