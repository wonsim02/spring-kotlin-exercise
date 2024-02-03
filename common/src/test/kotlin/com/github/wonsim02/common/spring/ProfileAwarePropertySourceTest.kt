package com.github.wonsim02.common.spring

import com.github.womsim02.common.spring.ProfileAwarePropertySource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.runApplication
import java.util.stream.Stream

/**
 * [ProfileAwarePropertySource]에 대한 테스트 코드.
 */
class ProfileAwarePropertySourceTest {

    @TestFactory
    fun `property value differs according to profile`(): Stream<out DynamicTest> {
        return Stream
            .of(
                TestCase(
                    displayName = "No profile given",
                    profiles = "",
                    expectedPropertyValue = "bar",
                ),
                TestCase(
                    displayName = "Profile 'a' given",
                    profiles = "a",
                    expectedPropertyValue = "baz",
                ),
                TestCase(
                    displayName = "Profile 'b' given",
                    profiles = "b",
                    expectedPropertyValue = "bar",
                ),
            )
            .map { it.getDynamicTest() }
    }

    data class TestCase(
        val displayName: String,
        val profiles: String,
        val expectedPropertyValue: String,
    ) {
        fun getDynamicTest(): DynamicTest {
            return DynamicTest.dynamicTest(displayName) {
                val applicationContext = runApplication<App>("--spring.profiles.active=$profiles")
                assertEquals(expectedPropertyValue, applicationContext.getBean(App::class.java).foo)
            }
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ProfileAwarePropertySource(
        "classpath:/property.yaml",
        "classpath:/property-*.yaml",
    )
    class App {

        @Value("\${foo}")
        var foo: String = ""
    }
}
