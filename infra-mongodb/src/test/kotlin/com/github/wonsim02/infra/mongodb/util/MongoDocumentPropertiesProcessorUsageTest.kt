package com.github.wonsim02.infra.mongodb.util

import com.github.wonsim02.infra.mongodb.config.InfraMongodbConfiguration
import com.github.wonsim02.infra.mongodb.testcase.document.Document0
import com.github.wonsim02.infra.mongodb.testcase.document.Document0Constants
import com.github.wonsim02.infra.mongodb.testutil.CustomMongoDBContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.junit.jupiter.Container
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.stream.Stream
import kotlin.random.Random

@SpringBootTest(
    classes = [
        InfraMongodbConfiguration::class,
        MongoDocumentPropertiesProcessorUsageTest.App::class,
    ],
)
@EnableAutoConfiguration
@TestPropertySource(
    properties = ["CONF_MONGODB_RETRY_WRITES=false"],
)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class MongoDocumentPropertiesProcessorUsageTest : ArgumentsProvider {

    private val targetObject = Document0(
        id = Random.nextLong(),
        a = listOf(
            Document0.A(
                b = listOf(
                    Document0.B(d = "hello", e = Instant.now().truncatedTo(ChronoUnit.SECONDS)),
                    Document0.B(d = "world", e = Instant.now().truncatedTo(ChronoUnit.SECONDS)),
                ),
                c = 0,
            ),
            Document0.A(
                b = listOf(
                    Document0.B(d = "foo", e = Instant.now().truncatedTo(ChronoUnit.SECONDS)),
                    Document0.B(d = "bar", e = Instant.now().truncatedTo(ChronoUnit.SECONDS)),
                ),
                c = 1,
            ),
        ),
        f = 2.0F,
    )

    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
        return Stream
            .of(
                // query on Document0.f
                Criteria.where(Document0Constants.f).`is`(2.0F),
                // query on Document0.a.c
                Criteria.where(Document0Constants.A.c).`is`(0),
                Criteria.where(Document0Constants.A.c).`is`(1),
                // query on Document0.a.b.d
                Criteria.where(Document0Constants.A.B.d).`is`("hello"),
                Criteria.where(Document0Constants.A.B.d).`is`("world"),
                Criteria.where(Document0Constants.A.B.d).`is`("foo"),
                Criteria.where(Document0Constants.A.B.d).`is`("bar"),
            )
            .map { Arguments.of(it) }
    }

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @BeforeAll
    fun insertTargetObject() {
        mongoTemplate.insert(targetObject)
    }

    @ParameterizedTest
    @ArgumentsSource(MongoDocumentPropertiesProcessorUsageTest::class)
    fun `building mongo query using class generated by MongoDocumentPropertiesProcessor works as expected`(
        criteria: Criteria,
    ) {
        val query = Query()
            .addCriteria(Criteria.where(Document0Constants.id).`is`(targetObject.id))
            .addCriteria(criteria)
            .limit(1)

        val findResult = mongoTemplate.find(
            query,
            Document0::class.java,
            Document0.COLLECTION_NAME,
        )

        assertEquals(listOf(targetObject), findResult)
    }

    @Configuration
    @EntityScan(basePackages = ["com.github.wonsim02.infra.mongodb.testcase.document"])
    class App

    companion object {

        @JvmStatic
        @Container
        val testContainer: CustomMongoDBContainer = CustomMongoDBContainer()

        @JvmStatic
        @DynamicPropertySource
        @Suppress("unused")
        fun setTestContainerProperties(registry: DynamicPropertyRegistry) {
            testContainer.start()
            testContainer.setTestContainerProperties(registry)
        }
    }
}
