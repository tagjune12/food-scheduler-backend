package com.foodscheduler.kakaomapscheduler;

import com.foodscheduler.kakaomapscheduler.config.DotenvConfig;
import com.foodscheduler.kakaomapscheduler.scheduler.KakaoPlaceScheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;

/**
 * 카카오맵 장소 검색 스케줄러 애플리케이션의 진입점.
 *
 * <p>GitHub Actions에서는 {@code --task=} 인자로 특정 작업만 실행 후 종료합니다.</p>
 * <p>로컬 환경에서는 {@link EnableScheduling}에 의해 cron 기반 스케줄링이 동작합니다.</p>
 *
 * @see KakaoPlaceScheduler
 * @see DotenvConfig
 */
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
        List<String> argList = Arrays.asList(args);

        String task = argList.stream()
                .filter(a -> a.startsWith("--task="))
                .map(a -> a.substring("--task=".length()))
                .findFirst()
                .orElse(null);

        // --task 인자가 없으면 로컬 @Scheduled 모드로 동작 (CommandLineRunner 즉시 종료하지 않음)
        if (task == null) {
            return;
        }

        System.out.println("GitHub Actions Scheduler 실행: task=" + task);

        switch (task) {
            case "restaurant":
                kakaoPlaceScheduler.scheduleDailyRestaurantSearch();
                break;
            case "cafe":
                kakaoPlaceScheduler.scheduleWeeklyCafeSearch();
                break;
            case "uspace-center":
                kakaoPlaceScheduler.scheduleTestCenterSearch();
                break;
            case "pangyo-center":
                kakaoPlaceScheduler.schedulePangyoCenterSearch();
                break;
            case "delete-old":
                kakaoPlaceScheduler.deleteOldPlaces();
                break;
            default:
                System.out.println("알 수 없는 task: " + task);
                break;
        }

        System.out.println("작업 완료. 애플리케이션을 종료합니다.");
        SpringApplication.exit(context, () -> 0);
    }
}
