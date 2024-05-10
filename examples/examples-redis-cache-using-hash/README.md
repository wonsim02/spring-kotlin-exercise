# `examples-redis-cache-using-hash`

Redis 캐시 및 Redis 자체 연산을 이용한 API 응답 시간 최적화 예시 소개용 모듈. ([API Spec](API_SPEC.md))

Also see: [Redis mget 및 hget/hset 명령어를 이용한 성능 최적화 예시 소개 #15](https://github.com/wonsim02/spring-kotlin-exercise/pull/15)

## `mget` 명령어를 이용한 응답 시간 최적화 (`VideoCache`)

```kotlin
fun multiGet(videoIds: Collection<Long>): Map<Long, Video> {
    if (videoIds.isEmpty()) return mapOf()

    val cacheKeys = videoIds.map { "$prefix$it" }
    return redisTemplate
        .opsForValue()
        .multiGet(cacheKeys)
        ?.asSequence()
        ?.mapNotNull { redisValueSerializer.deserialize(it) as? Video }
        ?.associateBy { it.id }
        ?: mapOf()
}
```

## `hget` 및 `hset` 명렁어를 이용한 응답 시간 최적화 (`WatchedVideosCountsCache`)

```kotlin
fun get(playListIds: Collection<Long>): WatchedVideosCounts {
    if (playListIds.isEmpty()) return mapOf()

    return redisTemplate
        .opsForHash<Long, WatchedVideosCountsEntry>()
        .multiGet(cacheKey, playListIds)
        .asSequence()
        .filterNotNull()
        .associate { it.playListId to it.count }
}

fun put(watchHistoryCounts: WatchedVideosCounts) {
    if (watchHistoryCounts.isEmpty()) return
    val entries = watchHistoryCounts.mapValues { (playListId, count) ->
        WatchedVideosCountsEntry(playListId = playListId, count = count)
    }

    redisTemplate
        .opsForHash<Long, WatchedVideosCountsEntry>()
        .putAll(cacheKey, entries)
    redisTemplate.expire(cacheKey, cacheDuration)
}
```

## 테스트 실행 방법

`WatchedVideosCountsCacheConfigurationTest`의 서브클래스로 `WatchedVideosCountsCache` 빈이
등록되어 있을 때와 없을 때 2가지 경우에 대한 테스트 클래스가 작성되어 있다:

- `WatchHistoryCountsCache` 빈 등록됨 :
  - class : `Enabled`
  - gradle task : `:watchedVideosCountsCacheEnabledTest`
- `WatchHistoryCountsCache` 빈이 등록되지 않음 :
  - class : `Disabled`
  - gradle task : `:watchedVideosCountsCacheDisabledTest`

실행할 테스트를 선택한 다음 다음 명령어로 테스트 실행이 가능하다:


### Windows

```shell
./gradlew.bat :examples:examples-redis-cache-using-hash:${gradle_task}
```

### Linux / Mac

```shell
./gradlew :examples:examples-redis-cache-using-hash:${gradle_task}
```

## 테스트 실행 결과 (평균 API 응답 시간)

- [Enabled Test Log](logs/enabled.log)
- [Disabled Test Log](logs/disabled.log)

| `WatchedVideosCountsCache` enabled  | GET `/playlists`  | PUT `/watch-histories`  |
|-------------------------------------|-------------------|-------------------------|
| true                                | 16.854 ms         | 10.024 ms               |
| false                               | 43.430 ms         | 7.108 ms                |
