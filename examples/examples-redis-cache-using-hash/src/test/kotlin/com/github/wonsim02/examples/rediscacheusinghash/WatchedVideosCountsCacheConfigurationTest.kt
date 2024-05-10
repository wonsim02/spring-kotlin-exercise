package com.github.wonsim02.examples.rediscacheusinghash

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.github.wonsim02.examples.rediscacheusinghash.cache.WatchedVideosCountsCache
import com.github.wonsim02.examples.rediscacheusinghash.client.RedisCacheUsingHashApiClient
import com.github.wonsim02.examples.rediscacheusinghash.config.AddingTestDataRunner
import com.github.wonsim02.examples.rediscacheusinghash.config.WatchedVideosCountCacheConfiguration.Companion.WATCHED_VIDEOS_COUNT_CACHE_PROPERTY_PREFIX
import com.github.wonsim02.examples.rediscacheusinghash.repository.WatchHistoryRepository
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.web.context.WebServerApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * [WatchedVideosCountsCache] 빈 등록 여부에 따른 성능 테스트.
 * 테스트 전에 다음 사전 작업을 진행한다:
 * 1. [AddingTestDataRunner]를 통해 테스트에서 사용할 `User`, `Video` 및 `PlayList` 등록
 * 2. 랜덤 `userId` 및 `videoId`로 `WatchHistory` 행 [NUM_WATCH_HISTORIES]개 추가
 *
 * 테스트는 [RedisCacheUsingHashApiClient.createWatchHistory] 및 [RedisCacheUsingHashApiClient.listPlayLists] API를
 * 호출하는 형식으로 진행된다. 이 때, API 호출 시나리오는 다음과 같다:
 * 1. [RedisCacheUsingHashApiClient.createWatchHistory] API 1회 호출
 * 2. [RedisCacheUsingHashApiClient.listPlayLists] API ([CALL_CREATE_WATCH_HISTORY_PERIOD] - 1)회 호출
 * 3. 1과 2를 두 API 호출 횟수가 [NUM_API_CALLS]회가 될 때까지 반복
 *
 * 테스트 이후 다음 값을 출력한다:
 * 1. 두 종류 API 응답 시간의 총합
 * 2. [RedisCacheUsingHashApiClient.createWatchHistory] 응답 시간 총합
 * 3. [RedisCacheUsingHashApiClient.listPlayLists] 응답 시간 총합
 *
 * @property watchHistoryCacheEnabled [WatchedVideosCountsCache] 빈 등록 여부
 */
abstract class WatchedVideosCountsCacheConfigurationTest : ExamplesRedisCacheUsingHashTestBase() {

    protected abstract val watchHistoryCacheEnabled: Boolean
    private val logger = LoggerFactory.getLogger(this::class.java)

    final override val args: Array<out String>
        get() = arrayOf(
            "--$WATCHED_VIDEOS_COUNT_CACHE_PROPERTY_PREFIX.enabled=$watchHistoryCacheEnabled",
            "--server.port=0"
        )

    @Test
    fun test() = testWithApplication { app ->
        val runner = app.getBean(AddingTestDataRunner::class.java)
        val userId = runner.userId!!
        val videoIds = runner.videoIds!!

        val runningPort = (app as WebServerApplicationContext).webServer.port
        val client = Retrofit.Builder()
            .client(
                OkHttpClient.Builder()
                    .callTimeout(0, TimeUnit.SECONDS)
                    .connectTimeout(0, TimeUnit.SECONDS)
                    .readTimeout(0, TimeUnit.SECONDS)
                    .writeTimeout(0, TimeUnit.SECONDS)
                    .build()
            )
            .baseUrl("http://localhost:$runningPort")
            .addConverterFactory(
                JacksonConverterFactory.create(
                    jsonMapper { addModule(kotlinModule()) }
                )
            )
            .build()
            .create(RedisCacheUsingHashApiClient::class.java)

        val watchHistoryRepository = app.getBean(WatchHistoryRepository::class.java)
        repeat(NUM_WATCH_HISTORIES) {
            watchHistoryRepository.create(
                userId = Random.nextLong(from = 1, until = 100),
                videoId = videoIds.random(),
            )
        }

        var createWatchHistoryElapsedTimeMs = 0L
        var listPlayListsElapsedTimeMs = 0L
        val elapsedTimeMs = measureTimeMillis {
            repeat(NUM_API_CALLS) { index ->
                if (index % CALL_CREATE_WATCH_HISTORY_PERIOD == 0) {
                    createWatchHistoryElapsedTimeMs += doCreateWatchHistory(client, userId, videoIds)
                } else {
                    listPlayListsElapsedTimeMs += doListPlayLists(client, userId)
                }
            }
        }

        logger.info("Total elapsed time is {} ms.", elapsedTimeMs)
        logger.info("Total elapsed time of createWatchHistory is {} ms.", createWatchHistoryElapsedTimeMs)
        logger.info("Total elapsed time of listPlyLists is {} ms.", listPlayListsElapsedTimeMs)
    }

    private fun doListPlayLists(
        client: RedisCacheUsingHashApiClient,
        userId: Long,
    ): Long {
        return measureTimeMillis {
            client
                .listPlayLists(userId = userId, cursor = null, limit = 20L)
                .execute()
                .also { assertTrue(it.isSuccessful) }
        }
    }

    private fun doCreateWatchHistory(
        client: RedisCacheUsingHashApiClient,
        userId: Long,
        videoIds: Collection<Long>,
    ): Long {
        val selectedVideoId = videoIds.random()
        return measureTimeMillis {
            client
                .createWatchHistory(userId = userId, videoId = selectedVideoId)
                .execute()
                .also { assertTrue(it.isSuccessful) }
        }
    }

    class Enabled : WatchedVideosCountsCacheConfigurationTest() {

        override val watchHistoryCacheEnabled = true
    }

    class Disabled : WatchedVideosCountsCacheConfigurationTest() {

        override val watchHistoryCacheEnabled = false
    }

    companion object {

        const val CALL_CREATE_WATCH_HISTORY_PERIOD = 20
        const val NUM_API_CALLS = 20000
        const val NUM_WATCH_HISTORIES = 100000
    }
}
