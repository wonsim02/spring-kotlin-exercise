package com.github.wonsim02.examples.rediscacheusinghash.config

import com.github.wonsim02.examples.rediscacheusinghash.model.PlayList
import com.github.wonsim02.examples.rediscacheusinghash.model.User
import com.github.wonsim02.examples.rediscacheusinghash.model.Video
import com.github.wonsim02.examples.rediscacheusinghash.repository.PlayListRepository
import com.github.wonsim02.examples.rediscacheusinghash.repository.UserRepository
import com.github.wonsim02.examples.rediscacheusinghash.repository.VideoRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * 테스트에 사용할 [User], [Video] 및 [PlayList]를 등록하는 [CommandLineRunner].
 */
@Component
class AddingTestDataRunner(
    private val playListRepository: PlayListRepository,
    private val userRepository: UserRepository,
    private val videoRepository: VideoRepository,
) : CommandLineRunner {

    private var _videoIds: List<Long>? = null
    val videoIds: List<Long>? get() = _videoIds

    private var _userId: Long? = null
    val userId: Long? get() = _userId

    override fun run(vararg args: String?) {
        _userId = userRepository.create("John Doe").id
        val createdVideoIds = (0 until NUM_VIDEOS).mapTo(mutableListOf()) { index ->
            val title = "Video #$index"
            videoRepository.create(title = title).id
        }
        _videoIds = createdVideoIds.toList()

        repeat(NUM_PLAY_LISTS) { index ->
            createdVideoIds.shuffle()
            val selected = createdVideoIds.subList(0, 50)
            playListRepository.create(
                title = "PlayList #$index",
                videoIds = selected,
            )
        }
    }

    companion object {

        const val NUM_VIDEOS = 1000
        const val NUM_PLAY_LISTS = 20
    }
}
