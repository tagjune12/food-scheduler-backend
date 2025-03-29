package com.foodscheduler.kakaomapscheduler.scheduler;

import com.foodscheduler.kakaomapscheduler.service.KakaoMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KakaoPlaceScheduler {

    private final KakaoMapService kakaoMapService;

    public KakaoPlaceScheduler(KakaoMapService kakaoMapService) {
        this.kakaoMapService = kakaoMapService;
    }

    /**
     * 매일 자정에 실행되는 스케줄러
     * 식당 카테고리 (FD6)에 대한 장소 검색 수행
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    // @Scheduled(fixedDelay = 1000 * 60 * 60 * 24) // 매일 자정
    public void scheduleDailyRestaurantSearch() {
        // 식당 카테고리로 검색 (FD6: 음식점)
        int placesCount = kakaoMapService.searchPlacesByCategory("FD6");
        log.info("음식점 검색 끝. {}개 장소 검색함", placesCount);
        log.info("오늘 검색되지 않는 업체 삭제");
        kakaoMapService.deleteAll("FD6");
        log.info("오늘 검색되지 않는 업체 삭제 완료");
    }

    /**
     * 매주 월요일 오전 10시에 실행되는 스케줄러
     * 카페 카테고리 (CE7)에 대한 장소 검색 수행
     */
    @Scheduled(cron = "0 0 10 ? * MON") // 매주 월요일 오전 10시
    // @Scheduled(fixedDelay = 1000 * 60 * 60 * 24) // 매일 자정
    public void scheduleWeeklyCafeSearch() {
        log.info("카페 검색 시작");

        // 카페 카테고리로 검색 (CE7: 카페)
        int placesCount = kakaoMapService.searchPlacesByCategory("CE7");
        log.info("카페 검색 끝. {}개 장소 검색함", placesCount);
        log.info("오늘 검색되지 않는 카페 삭제");
        kakaoMapService.deleteAll("CE7");
        log.info("오늘 검색되지 않는 카페 삭제 완료");
    }
}