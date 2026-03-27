# 카카오맵 장소 검색 스케줄러

카카오맵 API로 주변 음식점/카페를 주기적으로 검색하여 Supabase PostgreSQL에 저장하는 스케줄러 애플리케이션.

## 기술 스택

- Java 18
- Spring Boot 3.1.5
- MyBatis (mapper XML)
- WebFlux (WebClient)
- Supabase
- Lombok

## 아키텍처

### 실행 방식

**GitHub Actions** — `--task=<작업명>` 인자로 특정 작업만 실행 후 종료
**로컬 실행** — `@Scheduled` cron으로 시간대별 자동 실행

### 핵심 흐름

```
KakaoPlaceScheduler
  → KakaoMapService.searchPlaces()
    → CenterMapper: centers 테이블에서 대상 센터 조회
    → 센터 좌표 + 반경으로 초기 검색 사각형(Rect) 생성
    → recursiveSearch(): 카카오 API 호출 (최대 3페이지)
      → 결과 45건 초과 시 Rect를 4분할하여 재귀 탐색
    → PlaceMapper.upsert(): 건별 DB 저장
```

### 격자 분할 탐색

카카오 로컬 API는 한 번에 최대 45건(15건 × 3페이지)만 반환합니다.
결과가 45건에 도달하면 검색 영역을 4등분하여 재귀 탐색합니다.
영역이 0.00005도 미만으로 좁아지면 분할을 중단합니다.

### 센터 분리 구조

`centers` 테이블의 `name` 기준으로 스케줄을 분리 실행합니다.


## 스케줄

### GitHub Actions (매일 KST 기준)

| 시각 | 작업                 | task 인자 |
|------|--------------------|-----------|
| 00:00 | 기본 센터 음식점 검색       | `restaurant` |
| 01:00 | 기본 센터 카페 검색        | `cafe` |
| 01:30 | 유스페이스 센터 음식점+카페 검색 | `uspace-center` |
| 02:00 | 판교역 센터 음식점+카페 검색   | `pangyo-center` |
| 02:30 | 7일 이상 미갱신 데이터 삭제   | `delete-old` |

`workflow_dispatch`로 수동 실행도 가능합니다.

### 로컬 (`@Scheduled` cron)

로컬 실행 시 위와 동일한 시각에 자동으로 동작합니다.

## 환경 설정

`.env.example`을 `.env`로 복사 후 값을 입력합니다.

```bash
cp .env.example .env
```

```env
# Supabase 데이터베이스
SUPABASE_HOST=aws-0-ap-northeast-2.pooler.supabase.com
SUPABASE_PORT=6543
SUPABASE_DB=postgres
SUPABASE_USER=your_supabase_user
SUPABASE_PASSWORD=your_supabase_password

# Supabase API
SUPABASE_URL=https://your_project_id.supabase.co
SUPABASE_KEY=your_supabase_api_key

# 카카오맵 API
KAKAO_API_KEY=your_kakao_api_key
```

> **Supabase 연결 주의:** PgBouncer 충돌 방지를 위해 `prepareThreshold=0`이 필수 설정되어 있습니다.
> HikariCP `maximum-pool-size=3`으로 Supabase 무료 플랜 연결 수를 제한합니다.

## 빌드 & 실행

```bash
# 빌드
./gradlew build

# 로컬 실행 (cron 스케줄 모드)
./gradlew bootRun

# 특정 작업만 즉시 실행 후 종료 (GitHub Actions 방식)
./gradlew bootRun --args='--task=restaurant'
./gradlew bootRun --args='--task=cafe'
./gradlew bootRun --args='--task=uspace-center'
./gradlew bootRun --args='--task=pangyo-center'
./gradlew bootRun --args='--task=delete-old'

# 테스트
./gradlew test
```

## 사용 카테고리 코드

| 코드 | 분류 |
|------|------|
| `FD6` | 음식점 |
| `CE7` | 카페 |

카카오맵 API가 지원하는 전체 카테고리는 [카카오 로컬 API 문서](https://developers.kakao.com/docs/latest/ko/local/dev-guide)를 참고하세요.
