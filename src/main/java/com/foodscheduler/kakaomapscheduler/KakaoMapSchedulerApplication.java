package com.foodscheduler.kakaomapscheduler;

import com.foodscheduler.kakaomapscheduler.config.DotenvConfig;
import com.foodscheduler.kakaomapscheduler.scheduler.KakaoPlaceScheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KakaoMapSchedulerApplication implements CommandLineRunner {

    private final KakaoPlaceScheduler kakaoPlaceScheduler;
    private final ConfigurableApplicationContext context;

    public KakaoMapSchedulerApplication(KakaoPlaceScheduler kakaoPlaceScheduler, ConfigurableApplicationContext context) {
        this.kakaoPlaceScheduler = kakaoPlaceScheduler;
        this.context = context;
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(KakaoMapSchedulerApplication.class);
        application.addInitializers(new DotenvConfig());
        application.run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("GitHub Actions Scheduler 실행 시작");

        // 음식점 검색 실행
        kakaoPlaceScheduler.scheduleDailyRestaurantSearch();
        
        // 카페 검색 실행
        kakaoPlaceScheduler.scheduleWeeklyCafeSearch();

        System.out.println("모든 작업 완료. 애플리케이션을 종료합니다.");
        SpringApplication.exit(context, () -> 0);
    }
} 