package com.foodscheduler.kakaomapscheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("kakaoClient")
    public WebClient kakaoClient() {
        return WebClient.builder().build();
    }
} 