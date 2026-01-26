# 푸드 스케줄러 백엔드 (Food Scheduler Backend) Gemini 컨텍스트

이 `GEMINI.md` 파일은 AI 에이전트가 프로젝트 구조, 목적 및 개발 규칙을 이해하기 위한 컨텍스트를 제공합니다.

## 에이전트 지침

-   **언어 설정:** 사용자와의 모든 상호작용 및 응답은 **한국어**로 진행합니다.

## 프로젝트 개요

**Food Scheduler Backend**는 KakaoMap API를 사용하여 특정 장소 카테고리(음식점, 카페)를 주기적으로 검색하고 결과를 Supabase (PostgreSQL) 데이터베이스에 저장하는 Java Spring Boot 애플리케이션입니다.

### 기술 스택 (Tech Stack)
-   **언어:** Java 18
-   **프레임워크:** Spring Boot 3.1.5
-   **데이터베이스:** PostgreSQL (Supabase)
-   **HTTP 클라이언트:** Spring WebFlux (WebClient)
-   **스케줄러:** Spring Scheduling
-   **설정 관리:** `dotenv-java` (환경 변수 관리)
-   **빌드 도구:** Gradle

## 환경 설정 (Environment Setup)

이 애플리케이션은 프로젝트 루트에 `.env` 파일이 필요합니다. `.env.example` 파일을 복사하여 생성하세요.

```bash
cp .env.example .env
```

**필수 환경 변수:**
-   `SUPABASE_HOST`, `SUPABASE_PORT`, `SUPABASE_DB`, `SUPABASE_USER`, `SUPABASE_PASSWORD`: 데이터베이스 연결 정보.
-   `SUPABASE_URL`, `SUPABASE_KEY`: Supabase API 자격 증명.
-   `KAKAO_API_KEY`: 카카오맵 서비스 API 키.

## 빌드 및 실행 (Building and Running)

### 빌드 (Build)
프로젝트를 빌드하려면:
```bash
./gradlew build
```

### 실행 (Run)
애플리케이션을 실행하려면:
```bash
./gradlew bootRun
```

또는 빌드된 JAR 파일을 사용:
```bash
java -jar build/libs/food-scheduler-backend-0.0.1-SNAPSHOT.jar
```

## 주요 컴포넌트 (Key Components)

-   **`KakaoMapSchedulerApplication.java`**: 메인 진입점입니다. 스케줄링(`@EnableScheduling`)을 활성화하고 `DotenvConfig`를 통해 `.env` 설정을 초기화합니다.
-   **`KakaoPlaceScheduler.java`**: 스케줄링 작업을 정의합니다.
    -   `scheduleDailyRestaurantSearch()`: 음식점(`FD6`)을 검색합니다. `fixedDelay`(약 1일)로 설정되어 있습니다.
    -   `scheduleWeeklyCafeSearch()`: 카페(`CE7`)를 검색합니다. `@Scheduled(cron = "0 0 1 * * ?")`로 설정되어 있어 메서드 이름과 달리 **매일 오전 1시**에 실행됩니다.
-   **`KakaoMapService.java`**: KakaoMap API 호출, 응답 처리, 데이터베이스 저장/삭제와 관련된 비즈니스 로직을 처리합니다.

## 데이터베이스 스키마 (Database Schema)

애플리케이션은 PostgreSQL 데이터베이스에 `places` 테이블이 존재할 것으로 예상합니다.

```sql
CREATE TABLE places (
    id TEXT PRIMARY KEY,
    place_name TEXT,
    category_name TEXT,
    category_group_code TEXT,
    category_group_name TEXT,
    phone TEXT,
    address_name TEXT,
    road_address_name TEXT,
    longitude TEXT,
    latitude TEXT,
    place_url TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## 개발 참고사항 (Development Notes)

-   **로그 (Logging):** 로그는 `logs/application.log` 파일에 기록됩니다.
-   **스케줄링 불일치:** `scheduleWeeklyCafeSearch` 메서드는 이름과 달리 현재 **매일 오전 1시**에 실행되도록 설정되어 있습니다.
-   **데이터 정리:** 스케줄러는 현재 검색 실행에서 발견되지 않은 장소를 삭제하는 로직(`kakaoMapService.deleteAll(categoryCode)`)을 포함하고 있어, 데이터베이스가 최신 API 결과를 반영하도록 합니다.