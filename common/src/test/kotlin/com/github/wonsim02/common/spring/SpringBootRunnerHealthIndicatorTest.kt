package com.github.wonsim02.common.spring

import com.github.womsim02.common.spring.UseSpringBootRunnerHealthIndicator
import com.github.womsim02.common.spring.internal.SpringBootRunnerHealthIndicator
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean

/**
 * [UseSpringBootRunnerHealthIndicator]로 [SpringBootRunnerHealthIndicator]를 활성화했을 때 runner의 동작이 종료되기 전까지 health
 * 체크 검사 결과가 `DOWN`인지, 그리고 runner가 모두 동작하면 health 체크 결과가 `UP`인지 검증하는 테스트.
 */
class SpringBootRunnerHealthIndicatorTest {

    private val okHttpClient = OkHttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `health check fails until runners are completed`() {
        // given - start app
        val appThread = Thread {
            runApplication<App>(
                "--management.server.port=$ACTUATOR_PORT",
                "--spring.main.web-application-type=servlet",
            )
        }
        appThread.start()

        // given - wait until application is started
        while (!TestApplicationStartedEventListener.applicationStarted) {
            Thread.sleep(100L)
        }

        // when - send health check request
        val healthCheckResult1 = doHealthCheck()

        // then - status down
        Assertions.assertNotNull(healthCheckResult1)
        Assertions.assertEquals("DOWN", healthCheckResult1!!.status)

        // given - complete command line runner
        TestCommandLineRunner.isCompleted = true
        Thread.sleep(100L)

        // when - send health check request
        val healthCheckResult2 = doHealthCheck()

        // then - status UP
        Assertions.assertNotNull(healthCheckResult2)
        Assertions.assertEquals("UP", healthCheckResult2!!.status)

        // after test - join thread
        appThread.join()
    }

    private fun doHealthCheck(): HealthCheckResult? {
        val request = Request.Builder()
            .get()
            .url("http://localhost:$ACTUATOR_PORT/actuator/health/")
            .build()
        val response = okHttpClient.newCall(request).execute()

        return try {
            response
                .body
                ?.bytes()
                ?.let { json.decodeFromString(HealthCheckResult.serializer(), String(it)) }
        } catch (_: Throwable) {
            null
        }
    }

    @Serializable
    data class HealthCheckResult(val status: String)

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @UseSpringBootRunnerHealthIndicator
    class App {

        @Bean
        fun testCommandLineRunner(): TestCommandLineRunner {
            return TestCommandLineRunner()
        }

        @Bean
        fun testApplicationStartedEventListener(): TestApplicationStartedEventListener {
            return TestApplicationStartedEventListener()
        }
    }

    /**
     * [isCompleted]가 `true`가 되기 전까지 종료되지 않는 [CommandLineRunner]의 구현체.
     */
    class TestCommandLineRunner : CommandLineRunner {

        override fun run(vararg args: String?) {
            val newThread = Thread {
                while (!isCompleted) { Thread.sleep(100L) }
            }

            newThread.start()
            newThread.join()
        }

        companion object {

            @Volatile var isCompleted = false
        }
    }

    /**
     * 스프링 부트 어플리케이션이 실행되고 나서 runner를 실행하기 전에 발생하는 [ApplicationStartedEvent]에 대한 리스너.
     * @see org.springframework.boot.SpringApplication.run
     * @see org.springframework.boot.SpringApplicationRunListeners.started
     */
    class TestApplicationStartedEventListener : ApplicationListener<ApplicationStartedEvent> {

        override fun onApplicationEvent(event: ApplicationStartedEvent) {
            applicationStarted = true
        }

        companion object {

            @Volatile var applicationStarted = false
        }
    }

    companion object {

        private const val ACTUATOR_PORT = 8585
    }
}
