package com.foodscheduler.kakaomapscheduler.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring 애플리케이션 초기화 시 .env 파일을 로드하여 환경변수를 Spring Environment에 등록하는 설정 클래스.
 *
 * {@link ApplicationContextInitializer}를 구현하여 컨텍스트 생성 전에 실행되며,
 * Supabase 연결 정보와 카카오맵 API 키 등의 환경변수를 PropertySource로 등록합니다.
 *
 * .env 파일이 존재하지 않아도 에러 없이 진행됩니다 (GitHub Actions 환경 등).
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * .env 파일에서 환경변수를 읽어 Spring Environment의 PropertySource로 등록합니다.
     *
     * @param applicationContext Spring 애플리케이션 컨텍스트
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // .env 파일 로드
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();

            // 환경 변수를 Map으로 변환
            Map<String, Object> envMap = new HashMap<>();

            // 필요한 환경 변수 추가
            addEnvVar(envMap, dotenv, "SUPABASE_HOST");
            addEnvVar(envMap, dotenv, "SUPABASE_PORT");
            addEnvVar(envMap, dotenv, "SUPABASE_DB");
            addEnvVar(envMap, dotenv, "SUPABASE_USER");
            addEnvVar(envMap, dotenv, "SUPABASE_PASSWORD");
            addEnvVar(envMap, dotenv, "SUPABASE_URL");
            addEnvVar(envMap, dotenv, "SUPABASE_KEY");
            addEnvVar(envMap, dotenv, "KAKAO_API_KEY");

            // 환경 변수가 있는 경우에만 PropertySource 추가
            if (!envMap.isEmpty()) {
                environment.getPropertySources().addFirst(
                        new MapPropertySource("dotenvProperties", envMap)
                );
            }
        } catch (Exception e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            // 에러가 있어도 애플리케이션은 계속 실행
        }
    }

    /**
     * .env에서 읽은 환경변수 값이 존재하면 Map에 추가합니다.
     *
     * @param envMap 환경변수를 저장할 Map
     * @param dotenv Dotenv 인스턴스
     * @param key    환경변수 키
     */
    private void addEnvVar(Map<String, Object> envMap, Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            envMap.put(key, value);
        }
    }
}
