package com.foodscheduler.kakaomapscheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 카카오맵 API 호출을 위한 WebClient 설정 클래스.
 *
 * 카카오 REST API 키를 사용하여 인증 헤더가 포함된
 * {@link WebClient} 빈을 "kakaoClient"라는 이름으로 생성합니다.
 */
@Configuration
public class WebClientConfig {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    /**
     * 카카오맵 API 호출용 WebClient 빈을 생성합니다.
     * baseUrl은 카카오 개발자 API 도메인(https://dapi.kakao.com)으로 설정되며,
     * Authorization 헤더에 "KakaoAK" 접두사와 함께 API 키가 포함됩니다.
     *
     * @return 카카오맵 API용 {@link WebClient} 인스턴스
     */
    @Bean("kakaoClient")
    public WebClient kakaoClient() {
        return WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader("Authorization", "KakaoAK " + kakaoApiKey)
                .build();
    }
}
