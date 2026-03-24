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
     * 매일 자정(00:00)에 실행되는 스케줄러
     * 기본 센터(test, 판교역 제외)를 대상으로 음식점(FD6) 카테고리의 장소를 검색하여 DB에 저장합니다.
     */
     @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void scheduleDailyRestaurantSearch() {
        // 식당 카테고리로 검색 (FD6: 음식점)
        int placesCount = kakaoMapService.searchPlacesByCategory("FD6");
        log.info("음식점 검색 끝. {}개 장소 검색함", placesCount);

    }

    /**
     * 매일 오전 1시(01:00)에 실행되는 스케줄러
     * 기본 센터(test, 판교역 제외)를 대상으로 카페(CE7) 카테고리의 장소를 검색하여 DB에 저장합니다.
     */
    @Scheduled(cron = "0 0 1 * * ?") // 매일 오전1시
    public void scheduleWeeklyCafeSearch() {
        log.info("카페 검색 시작");

        // 카페 카테고리로 검색 (CE7: 카페)
        int placesCount = kakaoMapService.searchPlacesByCategory("CE7");
        log.info("카페 검색 끝. {}개 장소 검색함", placesCount);

    }

    /**
     * 매일 오전 1시 30분(01:30)에 실행되는 test 센터 전용 스케줄러
     * centers 테이블에서 name이 'test'로 시작하는 센터만 대상으로
     * 음식점(FD6)과 카페(CE7)를 검색하여 DB에 저장합니다.
     */
    @Scheduled(cron = "0 30 1 * * ?") // 매일 오전1시 30분
    public void scheduleTestCenterSearch() {
        log.info("test 센터 음식점 검색 시작");
        int restaurantCount = kakaoMapService.searchPlacesByCategoryForTest("FD6");
        log.info("test 센터 음식점 검색 끝. {}개 장소 검색함", restaurantCount);

        log.info("test 센터 카페 검색 시작");
        int cafeCount = kakaoMapService.searchPlacesByCategoryForTest("CE7");
        log.info("test 센터 카페 검색 끝. {}개 장소 검색함", cafeCount);
    }

    /**
     * 매일 오전 2시(02:00)에 실행되는 판교역 센터 전용 스케줄러
     * centers 테이블에서 name이 '판교역'으로 시작하는 센터만 대상으로
     * 음식점(FD6)과 카페(CE7)를 검색하여 DB에 저장합니다.
     */
    @Scheduled(cron = "0 0 2 * * ?") // 매일 오전2시
    public void schedulePangyoCenterSearch() {
        log.info("판교역 센터 음식점 검색 시작");
        int restaurantCount = kakaoMapService.searchPlacesByCategoryForPangyo("FD6");
        log.info("판교역 센터 음식점 검색 끝. {}개 장소 검색함", restaurantCount);

        log.info("판교역 센터 카페 검색 시작");
        int cafeCount = kakaoMapService.searchPlacesByCategoryForPangyo("CE7");
        log.info("판교역 센터 카페 검색 끝. {}개 장소 검색함", cafeCount);
    }

    /**
     * 매일 오전 2시 30분(02:30)에 실행되는 오래된 데이터 정리 스케줄러
     * places 테이블에서 updated_at이 7일 이상 지난 카페(CE7)와 음식점(FD6) 데이터를 삭제합니다.
     */
    @Scheduled(cron = "0 30 2 * * ?") // 매일 오전2시 30분
    public void deleteOldPlaces(){
        log.info("7일동안 검색되지 않은 장소 삭제");
        kakaoMapService.deleteAll("CE7");
        log.info("오늘 검색되지 않는 카페 삭제 완료");

        kakaoMapService.deleteAll("FD6");
        log.info("오늘 검색되지 않는 식당 삭제 완료");
    }
}
