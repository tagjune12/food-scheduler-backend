# 카카오맵 장소 검색 스케줄러

이 프로젝트는 카카오맵 API를 사용하여 카테고리별 장소를 주기적으로 검색하고 Supabase 데이터베이스에 저장하는 스케줄러입니다.

## 기술 스택

- Java 18.0.2.1
- Spring Boot 3.1.5
- Spring Data JPA
- WebFlux (WebClient)
- Supabase (PostgreSQL)
- Lombok

## 주요 기능

- 카카오맵 API를 사용한 카테고리별 장소 검색
- 스케줄링을 통한 주기적인 데이터 업데이트
  - 매일 자정: 식당 카테고리 검색
  - 매주 월요일 오전 10시: 카페 카테고리 검색
- 검색된 장소 정보를 Supabase PostgreSQL 데이터베이스에 저장

## 설정 방법

1. `.env.example` 파일을 `.env` 파일로 복사하고 필요한 정보를 입력합니다.

```
# Supabase 데이터베이스 설정
SUPABASE_HOST=aws-0-ap-northeast-2.pooler.supabase.com
SUPABASE_PORT=6543
SUPABASE_DB=postgres
SUPABASE_USER=your_supabase_user
SUPABASE_PASSWORD=your_supabase_password

# Supabase API 설정
SUPABASE_URL=https://your_project_id.supabase.co
SUPABASE_KEY=your_supabase_api_key

# 카카오맵 API 설정
KAKAO_API_KEY=your_kakao_api_key
```

2. `application.properties` 파일은 다음과 같이 설정되어 있어 .env 파일의 환경 변수를 참조합니다.

```properties
# Supabase PostgreSQL 데이터베이스 설정
spring.datasource.url=jdbc:postgresql://${SUPABASE_HOST}:${SUPABASE_PORT}/${SUPABASE_DB}?user=${SUPABASE_USER}&password=${SUPABASE_PASSWORD}

# 카카오맵 API 설정
kakao.api.key=${KAKAO_API_KEY}
```

3. 애플리케이션을 빌드하고 실행:

```bash
./gradlew build
java -jar build/libs/food-scheduler-backend-0.0.1-SNAPSHOT.jar
```

## Supabase 설정

1. [Supabase](https://supabase.io/) 계정 생성 및 새 프로젝트 생성
2. 프로젝트의 SQL 편집기에서 다음 SQL을 실행하여 장소 테이블 생성:

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

3. Supabase 프로젝트 설정에서 데이터베이스 연결 정보 및 API 키를 확인하여 `.env` 파일에 입력

## 카카오맵 API 카테고리 그룹 코드

스케줄러에서 사용 가능한 카테고리 그룹 코드 목록:

- `MT1`: 대형마트
- `CS2`: 편의점
- `PS3`: 어린이집, 유치원
- `SC4`: 학교
- `AC5`: 학원
- `PK6`: 주차장
- `OL7`: 주유소, 충전소
- `SW8`: 지하철역
- `BK9`: 은행
- `CT1`: 문화시설
- `AG2`: 중개업소
- `PO3`: 공공기관
- `AT4`: 관광명소
- `AD5`: 숙박
- `FD6`: 음식점
- `CE7`: 카페
- `HP8`: 병원
- `PM9`: 약국

## 참고 문서

- [카카오맵 API 문서](https://developers.kakao.com/docs/latest/ko/local/dev-guide)
- [Spring Boot 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Supabase 문서](https://supabase.io/docs)
