# `infra-mongodb`

`spring-data-mongodb` 의존성 및 MongoDB 관련 추가적인 설정에 대한 모듈입니다.

## 환경 변수

| environment variable                   | description |
|----------------------------------------|-------------|
| `CONF_MONGODB_HOST`                    | MongoDB 호스트 |
| `CONF_MONGODB_PORT`                    | MongoDB 연결 포트 |
| `CONF_MONGODB_DATABASE`                | 연결할 데이터베이스 |
| `CONF_MONGODB_USERNAME`                | 인증에 사용할 사용자 이름 |
| `CONF_MONGODB_PASSWORD`                | 인증에 사용할 패스워드 |
| `CONF_MONGODB_AUTHENTICATION_DATABASE` | 인증에 사용할 데이터베이스. 지정하지 않으면 연결할 데이터베이스와 동일한 값을 사용한다. |
| `CONF_MONGODB_RETRY_WRITES`            | MongoDB 연결 시 `retryWrites` 옵션에 대한 값 |
