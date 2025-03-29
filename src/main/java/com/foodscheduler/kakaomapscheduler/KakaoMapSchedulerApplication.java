package com.foodscheduler.kakaomapscheduler;

import com.foodscheduler.kakaomapscheduler.config.DotenvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KakaoMapSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(KakaoMapSchedulerApplication.class);
        application.addInitializers(new DotenvConfig());
        application.run(args);
    }
} 