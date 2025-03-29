package com.foodscheduler.kakaomapscheduler.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring 시작 시 .env 파일을 로드하는 클래스
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

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

    private void addEnvVar(Map<String, Object> envMap, Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            envMap.put(key, value);
        }
    }
} 