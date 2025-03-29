package com.foodscheduler.kakaomapscheduler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Bean
    public WebClient supabaseClient() {
        return WebClient.builder()
                .baseUrl(supabaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseKey)
                .defaultHeader("apikey", supabaseKey)
                .build();
    }
} 