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

## 로그 관리

이 애플리케이션은 다음과 같이 로그를 설정하고 있습니다:

- 로그 레벨: 루트 로거는 INFO, 애플리케이션 로거는 DEBUG
- 로그 파일 위치: `logs/application.log`
- 로그 파일 형식: 날짜, 시간, 스레드, 레벨, 로거, 메시지 포함

### Gradle 실행 시 로그 파일로 저장하기

#### Gradle이 설치된 경우

Gradle로 실행할 때 로그를 파일로 저장하려면 다음과 같이 실행합니다:

```bash
# Linux/Mac OS
./gradlew bootRun > logs/gradle-output.log 2>&1
```

또는 로그를 파일로 저장하면서 콘솔에도 출력하려면:

```bash
# Linux/Mac OS
./gradlew bootRun | tee logs/gradle-output.log
```

Windows의 경우:

```cmd
# Windows CMD
gradlew.bat bootRun > logs\gradle-output.log 2>&1

# Windows PowerShell
.\gradlew.bat bootRun | Out-File -FilePath .\logs\gradle-output.log
```

#### Gradle이 설치되지 않은 경우

Gradle이 설치되어 있지 않거나 Gradle 래퍼가 없는 경우, 빌드된 JAR 파일을 사용하여 직접 실행할 수 있습니다:

```bash
# Linux/Mac OS
java -jar build/libs/food-scheduler-backend-0.0.1-SNAPSHOT.jar > logs/application-output.log 2>&1
```

Windows의 경우:

```cmd
# Windows CMD
java -jar build\libs\food-scheduler-backend-0.0.1-SNAPSHOT.jar > logs\application-output.log 2>&1

# Windows PowerShell
java -jar build\libs\food-scheduler-backend-0.0.1-SNAPSHOT.jar | Out-File -FilePath .\logs\application-output.log
```

### 편리한 실행 스크립트 사용하기

프로젝트 루트 디렉토리에 다음 두 개의 실행 스크립트가 준비되어 있습니다:

1. **Windows CMD 사용자**를 위한 배치 파일:

```cmd
# run-with-logs.bat 실행
run-with-logs.bat
```

2. **Windows PowerShell 사용자**를 위한 스크립트:

```powershell
# PowerShell에서 실행 (스크립트 실행 권한 필요)
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run-with-logs.ps1
```

이 스크립트들은 자동으로 logs 디렉토리를 생성하고 애플리케이션을 실행한 후 로그를 저장합니다.

### Gradle 래퍼 설치하기

프로젝트에 Gradle 래퍼가 없는 경우, 다음과 같이 설치할 수 있습니다:

1. [Gradle 공식 사이트](https://gradle.org/install/)에서 Gradle을 다운로드하고 설치합니다.
2. 프로젝트 디렉토리에서 다음 명령어를 실행합니다:

```bash
gradle wrapper
```

이렇게 하면 `gradlew`와 `gradlew.bat` 파일이 생성됩니다.

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
