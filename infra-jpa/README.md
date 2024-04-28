# `infra-jpa`

Spring JPA 및 DB에 대한 설정을 모아 둔 모듈이다.
현 모듈을 `runtimeClassPath`에 추가하고 다음 환경 변수를 설정하면 Spring JPA 관련 기능을 사용할 수 있다.

| Environment Variables | description           |
|-----------------------|-----------------------|
| `CONF_RDB_HOST`       | RDB host              |
| `CONF_RDB_PORT`       | RDB port              |
| `CONF_RDB_DATABASE`   | RDB database          |
| `CONF_RDB_USERNAME`   | RDB user              |
| `CONF_RDB_PASSWORD`   | password for RDB user |

## `CustomPostgresDialect`

Hibernate 라이브러리에서 postgres DB에 대해 설정한 기본 `Dialect`에 다음 SQL 함수를 추가한 `Dialect` 구현체.
현 모듈을 `runtimeClasspath`에 추가하면 Hibernate에서 `Dialect`로 `CustomPostgresDialect`을 사용한다.

- `array_agg`
- `array_append`

Also see : [Hibernate의 Dialect에 SQL 함수 추가 #9](https://github.com/wonsim02/spring-kotlin-exercise/pull/9)

## `CustomPostgresqlContainer`

현 모듈의 `testFixtures` 스코프에 추가된 테스트 DB 컨테이너.

### 사용 예시 1 - `setTestContainerProperties()` 함수 사용

```kotlin
@SpringBootTest(
    // configurations omitted
)
@Testcontainers
class SampleTest1 {
    
    // test codes omitted
    
    companion object {

        @Container
        @JvmStatic
        val container = CustomPostgresqlContainer(
            database = "database",
            username = "username",
            password = "password",
        )

        @DynamicPropertySource
        @JvmStatic
        @Suppress("unused")
        fun setTestDatabaseProperties(registry: DynamicPropertyRegistry) {
            container.setTestContainerProperties(registry)
        }
    }
}
```

### 사용 예시 2 - `provideTestContainerProperties()` 함수 사용

```kotlin
class SampleTest2 {
    
    @Test
    fun `sample test`() {
        container.start()
        runApplication<TestApp>(*container.provideTestContainerProperties())

        // omitted
    }
    
    @SpringBootApplication
    class TestApp
    
    companion object {
        
        @Container
        val container = CustomPostgresqlContainer(
            database = "database",
            username = "username",
            password = "password",
        )
    }
}
```
