# `common`

주로 Spring Boot 관련 확장 기능들이 구현되어 있다.

## `@ProfileAwarePropertySource`

```yaml
# property.yml at resources
foo: bar
```

```yaml
# property-a.yml at resources
foo: baz
```

```kotlin
@Configuration
@ProfileAwarePropertySource(
    locations = [
        "classpath:/property.yaml",
        "classpath:/property-*.yaml",
    ],
)
class SampleConfiguration(
    /**
     * Value of foo becomes `baz` if active profiles contain `a`
     * and `bar` otherwise.
     */
    @Value("\${foo:}") private val foo: String,
)
```

Also see : [ProfileAwarePropertySource 어노테이션 추가 #1](https://github.com/wonsim02/spring-kotlin-exercise/pull/1)

## `@UseSpringBootRunnerHealthIndicator`

```kotlin
/**
 * If the application runtime classpath contains `spring-boot-starter-actuator`
 * library, calling `/actuator/health` API responses `{"status":"DOWN"}` until
 * all of registered [ApplicationRunner] and [CommandLineRunner] beans are executed. 
 */
@SpringBootApplication
@UseSpringBootRunnerHealthIndicator
class App
```

Also see : [Actuator를 통한 health 검사 시 Runner 동작 완료 여부도 검사 #8](https://github.com/wonsim02/spring-kotlin-exercise/pull/8)
