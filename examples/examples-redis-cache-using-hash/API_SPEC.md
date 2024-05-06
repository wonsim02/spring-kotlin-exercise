# API Spec

## 모델 목록

### User

서비스 사용자

| property  | type      | description                       |
|-----------|-----------|-----------------------------------|
| `id`      | int64     | 사용자 고유 ID. API 인증에 사용된다.     |
| `name`    | string    | 사용자 이름.                         |

### Video

시청 가능한 영상

| property  | type      | description       |
|-----------|-----------|-------------------|
| `id`      | int64     | 영상 고유 ID.        |
| `title`   | string    | 영상 제목.           |

### PlayListWithDetails

여러 영상을 취합한 재생 목록

| property              | type      | description                                   |
|-----------------------|-----------|-----------------------------------------------|
| `id`                  | int64     | 재생 목록 고유 ID                                 |
| `title`               | string    | 재생 목록 제목.                                   |
| `videos`              | array     | 재생 목록에 속한 영상 목록                          |
| `watchedVideosCount`  | int32     | 재생 목록에 속한 영상 중 한 번이라도 시청한 영상의 개수    |

### WatchHistory

사용자의 영상 시청 기록

| property      | type      | description               |
|---------------|-----------|---------------------------|
| `id`          | int64     | 시청 기록 고유 ID             |
| `user_id`     | int64     | 사용자 ID                   |
| `video_id`    | int64     | 영상 ID                     |

### 

## API 목록

공통적으로 인증은 `x-user-id` 헤더에 사용자 ID를 설정하는 방식으로 진행된다. (see: `UserIdHeaderParameterConfiguration`)

### GET `/playlists`

- 요청

  | name      | type          | required  | description           |
  |-----------|---------------|-----------|-----------------------|
  | `cursor`  | request param | false     | 재생 목록 ID에 대한 커서.  |
  | `limit`   | request param | true      | 조회할 재생 목록 최대 개수. |

- 응답 : `ListPlayListsResponse`

  | property      | type      | description                   |
  |---------------|-----------|-------------------------------|
  | `playLists`   | array     | `PlayListWithDetails` 목록     |

### PUT `/watch-histories`

- 요청

  | name          | type          | required  | description     |
  |---------------|---------------|-----------|-----------------|
  | `video_id`    | request param | true      | 시청한 영상의 ID    |

- 응답 : `WatchHistory`
