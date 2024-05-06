package com.github.wonsim02.examples.rediscacheusinghash.dto

import com.github.wonsim02.examples.rediscacheusinghash.model.PlayList
import com.github.wonsim02.examples.rediscacheusinghash.model.Video

data class PlayListWithDetails(
    val id: Long,
    val title: String,
    val videos: List<Video>,
    val watchedVideosCount: Int,
) {

    constructor(
        playList: PlayList,
        videoMap: Map<Long, Video>,
        watchHistoryCounts: WatchHistoryCounts,
    ) : this(
        id = playList.id,
        title = playList.title,
        videos = playList
            .videoIds
            .map { videoMap[it] ?: throw NoSuchElementException() },
        watchedVideosCount = playList
            .videoIds
            .count { videoId -> watchHistoryCounts[videoId]?.let { it > 0 } ?: false },
    )
}
