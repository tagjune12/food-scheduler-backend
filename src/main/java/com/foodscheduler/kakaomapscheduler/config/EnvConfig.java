package com.foodscheduler.kakaomapscheduler.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

/**
 * 환경설정 관련 클래스
 * .env 파일에서 환경 변수를 로드합니다.
 */
@Configuration
public class EnvConfig {
    
    @PostConstruct
    public void loadEnv() {
        File envFile = new File(".env");
        if (envFile.exists()) {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .filename(".env")
                    .load();
            
            // Supabase 데이터베이스 설정
            setEnvIfPresent(dotenv, "SUPABASE_HOST");
            setEnvIfPresent(dotenv, "SUPABASE_PORT");
            setEnvIfPresent(dotenv, "SUPABASE_DB");
            setEnvIfPresent(dotenv, "SUPABASE_USER");
            setEnvIfPresent(dotenv, "SUPABASE_PASSWORD");
            
            // Supabase API 설정
            setEnvIfPresent(dotenv, "SUPABASE_URL");
            setEnvIfPresent(dotenv, "SUPABASE_KEY");
            
            // 카카오맵 API 설정
            setEnvIfPresent(dotenv, "KAKAO_API_KEY");
        }
    }
    
    private void setEnvIfPresent(Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            System.setProperty(key, value);
        }
    }
} 