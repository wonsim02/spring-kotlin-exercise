# `infra-redis`

`spring-data-redis` 의존성 및 Redis 관련 추가적인 설정에 대한 모듈.
현 모듈을 `runtimeClasspath`에 추가한 후 다음 환경 변수를 설정하면 Redis 관련 기능을 사용할 수 있다.

| environment variable   | description |
|------------------------|-------------|
| `CONF_REDIS_HOST`      | Redis 호스트 |
| `CONF_REDIS_PORT`      | Redis 연결 포트 |
| `CONF_REDIS_PASSWORD`  | 인증에 사용할 패스워드 |
| `CONF_REDIS_CLUSTERED` | Redis 클러스터 연결 여부. `true`이면 `CONF_REDIS_HOST` 및 `CONF_REDIS_PORT`가 가리키는 엔드포인트를 단일 노드로 가지는 클러스터로 간주한다. ([infra-redis 모듈 추가 + clustered 스프링 속성 추가 #14](https://github.com/wonsim02/spring-kotlin-exercise/pull/14)) |

## `CustomRedisContainer`

현 모듈의 `testFixtures` 스코프에 추가된 테스트 Redis 컨테이너.

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
        val container = CustomRedisContainer(
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
@TestContainers
class SampleTest2 {
    
    @Test
    fun `sample test`() {
        runApplication<TestApp>(*container.provideTestContainerProperties())

        // omitted
    }
    
    @SpringBootApplication
    class TestApp
    
    companion object {
        
        @Container
        @JvmStatic
        val container = CustomRedisContainer(
            password = "password",
        )
    }
}
```
