package com.foodscheduler.kakaomapscheduler;

import com.foodscheduler.kakaomapscheduler.config.DotenvConfig;
import com.foodscheduler.kakaomapscheduler.scheduler.KakaoPlaceScheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 카카오맵 장소 검색 스케줄러 애플리케이션의 진입점.
 *
 * <p>{@link CommandLineRunner}를 구현하여 애플리케이션 시작 시
 * 음식점(FD6)과 카페(CE7) 검색을 즉시 실행한 후 종료합니다.
 * 이 방식은 GitHub Actions에서 일회성 실행을 위해 사용됩니다.</p>
 *
 * <p>로컬 환경에서는 {@link EnableScheduling}에 의해 cron 기반 스케줄링도 동작합니다.</p>
 *
 * @see KakaoPlaceScheduler
 * @see DotenvConfig
 */
@SpringBootApplication
@EnableScheduling
public class KakaoMapSchedulerApplication implements CommandLineRunner {

    private final KakaoPlaceScheduler kakaoPlaceScheduler;
    private final ConfigurableApplicationContext context;

    /**
     * @param kakaoPlaceScheduler 장소 검색 스케줄러
     * @param context             애플리케이션 컨텍스트 (작업 완료 후 종료용)
     */
    public KakaoMapSchedulerApplication(KakaoPlaceScheduler kakaoPlaceScheduler, ConfigurableApplicationContext context) {
        this.kakaoPlaceScheduler = kakaoPlaceScheduler;
        this.context = context;
    }

    /**
     * 애플리케이션 메인 진입점.
     * {@link DotenvConfig}를 초기화하여 .env 파일의 환경변수를 Spring Environment에 로드합니다.
     *
     * @param args 커맨드라인 인자
     */
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(KakaoMapSchedulerApplication.class);
        application.addInitializers(new DotenvConfig());
        application.run(args);
    }

    /**
     * 애플리케이션 시작 후 즉시 실행되는 메서드.
     * 음식점과 카페 검색을 순차적으로 실행한 후 애플리케이션을 종료합니다.
     *
     * @param args 커맨드라인 인자
     * @throws Exception 실행 중 발생할 수 있는 예외
     */
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
