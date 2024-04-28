# `examples-use-hibernate-impl`

Hibernate 라이브러리로 생성된 `insert` 쿼리를 직접 참조한 후 `on conflict` 구문을 추가하여
constraint 위배 상황에 아무런 행동도 하지 않거나 이미 존재하는 행의 값을 수정할 수 있게 합니다.

Also see : [테이블에 행 삽입 시 constraint 위배 방지 예시 #10](https://github.com/wonsim02/spring-kotlin-exercise/pull/10)

## 예시 1

```kotlin
performConstraintViolationSafeInsertion(
    entity = SampleEntity(
        id = 2L,
        uniqueProperty = "bar",
        nonUniqueProperty = 2,
    ),
    entityManager = entityManager,
    entityIdGetter = SampleEntity::id,
)
```

```sql
insert into public.sample_entity (non_unique_property, unique_property, id)
values (2, 'bar', 2)
on conflict do nothing
```

## 예시 2

```kotlin
performConstraintViolationSafeInsertion(
    entity = SampleEntity(
        id = 4L,
        uniqueProperty = "bar",
        nonUniqueProperty = 4,
    ),
    entityManager = entityManager,
    onConflictStatement = "(unique_property) do update set " +
        "non_unique_property = excluded.non_unique_property",
    entityIdGetter = SampleEntity::id,
)
```

```sql
insert into public.sample_entity (non_unique_property, unique_property, id)
values (4, 'bar', 4)
on conflict (unique_property) do update set
non_unique_property = excluded.non_unique_property
```

## 테스트 실행 방법

### Windows

```shell
./gradlew.bat :examples:examples-use-hibernate-impl:test
```

### Linux / Mac

```shell
./gradlew :examples:examples-use-hibernate-impl:test
```
