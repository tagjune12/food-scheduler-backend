package com.foodscheduler.kakaomapscheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Supabase REST API 호출을 위한 WebClient 설정 클래스.
 *
 * Supabase URL과 서비스 롤 키를 사용하여 인증 헤더가 포함된
 * {@link WebClient} 빈을 생성합니다.
 */
@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    /**
     * Supabase REST API 호출용 WebClient 빈을 생성합니다.
     * Authorization Bearer 토큰과 apikey 헤더가 기본으로 설정됩니다.
     *
     * @return Supabase API용 {@link WebClient} 인스턴스
     */
    @Bean
    public WebClient supabaseClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                .defaultHeader("apikey", supabaseKey)
                .build();
    }
}
