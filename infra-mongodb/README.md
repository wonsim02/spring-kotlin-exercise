# `infra-mongodb`

`spring-data-mongodb` 의존성 및 MongoDB 관련 추가적인 설정에 대한 모듈.
현 모듈을 `runtimeClasspath`에 추가한 후 다음 환경 변수를 설정하면 MongoDB 관련 기능을 사용할 수 있다.

| environment variable                   | description |
|----------------------------------------|-------------|
| `CONF_MONGODB_HOST`                    | MongoDB 호스트 |
| `CONF_MONGODB_PORT`                    | MongoDB 연결 포트 |
| `CONF_MONGODB_DATABASE`                | 연결할 데이터베이스 |
| `CONF_MONGODB_USERNAME`                | 인증에 사용할 사용자 이름 |
| `CONF_MONGODB_PASSWORD`                | 인증에 사용할 패스워드 |
| `CONF_MONGODB_AUTHENTICATION_DATABASE` | 인증에 사용할 데이터베이스. 지정하지 않으면 연결할 데이터베이스와 동일한 값을 사용한다. |
| `CONF_MONGODB_RETRY_WRITES`            | MongoDB 연결 시 `retryWrites` 옵션에 대한 값 ([MongoClient 빈 생성 시 스프링 속성으로 retryWrites 값 설정 #2](https://github.com/wonsim02/spring-kotlin-exercise/pull/2)) |

## `CONF_MONGODB_DATABASE` 환경변수로 설정된 DB 외 DB 등록

Also see : [기본 Mongo 데이터베이스 이외의 데이터베이스로의 연결 지원 #3](https://github.com/wonsim02/spring-kotlin-exercise/pull/3)

### `application*.yml` 사용

```yaml
com:
  github:
    wonsim02:
      infra:
        mongodb:
          additional-databases: foo,bar,baz
```

### `AdditionalMongoDatabaseNamesSupplier` 사용

```kotlin
@Configuration
class SampleConfiguration {

    @Bean
    fun fooBarBazMongoDatabaseNamesSupplier(): AdditionalMongoDatabaseNamesSupplier =
        AdditionalMongoDatabaseNamesSupplier { listOf("foo", "bar", "baz") }
}
```

## `@UseMongoDatabase`

Also see : [Mongo 데이터베이스 별로 @Document를 지정할 수 있는 어노테이션 추가 #4](https://github.com/wonsim02/spring-kotlin-exercise/pull/4)

```kotlin
/**
 * `foo` 및 `bar` Mongo 데이터베이스에만 `sample` 콜렉션이 설정된다.
 */
@UseMongoDatabase("foo", "bar")
@Document(collection = "sample")
data class SampleDocument(
    val id: Long,
    val a: String,
    val b: Int,
)
```

## `@MongoIndexDefinitionSource`

Also see : [Mongo 콜렉션에 대한 인덱스 정의를 JSON 파일로 지정할 수 있는 어노테이션 추가 #5](https://github.com/wonsim02/spring-kotlin-exercise/pull/5)

### `resources` 경로에 JSON 파일 추가

- `sample/idx000__a_asc__b_desc.json`
    ```json
    {
      "keys": {
        "a": 1,
        "b": -1
      }
    }
    ```
- `sample/idx001__b_asc__a_desc.json`
    ```json
    {
      "keys": {
        "b": 1,
        "a": -1
      }
    }
    ```

### 샘플 코드

```kotlin
@Document(collection = "sample")
@MongoIndexDefinitionSource("classpath:/sample/*.json")
data class SampleDocument(
    val id: Long,
    val a: String,
    val b: Int,
)
```

### 생성된 Mongo 인덱스 (mongo CLI)

```shell
> db.sample.getIndexes()
[
	{
		"v" : 2,
		"key" : {
			"_id" : 1
		},
		"name" : "_id_",
		"ns" : "database.sample"
	},
	{
		"v" : 2,
		"key" : {
			"a" : 1,
			"b" : -1
		},
		"name" : "idx000",
		"ns" : "database.sample",
		"background" : true
	},
	{
		"v" : 2,
		"key" : {
			"b" : 1,
			"a" : -1
		},
		"name" : "idx001",
		"ns" : "database.sample",
		"background" : true
	}
]
```

## `@Document` 어노테이션에 대한 Annotation Processing

Also see : [@Document 클래스에 대한 어노테이션 프로세서 추가 #6](https://github.com/wonsim02/spring-kotlin-exercise/pull/6)

### gradle 의존성 추가

```kotlin
// build.gradle.kts
dependencies {
    // omitted
    kapt(project(":infra-mongodb"))
}
```

### `@Document` 어노테이션이 첨부된 클래스 추가

```kotlin
@Document
class A(
    val id: Long,
    val a: Int,
    val bValue: B,
) {
    class B(val c: List<C>)

    class C(val d: Instant)
}
```

### 어노테이션 프로세싱으로 생성된 JAVA 코드

```java
public class AConstants {

  public static final string id = "id";

  public static final string a = "a";

  public static final string bValue = "bValue.";

  public static class BValue {

    public static final string c = "bValue.c.";

    public static class C {

      public static final string d = "bValue.c.d";

    }

  }

}
```
