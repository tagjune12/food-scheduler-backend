package com.foodscheduler.kakaomapscheduler.scheduler;

import com.foodscheduler.kakaomapscheduler.entity.Place;
import com.foodscheduler.kakaomapscheduler.service.KakaoMapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class KakaoPlaceScheduler {

    private final KakaoMapService kakaoMapService;
    
    @Value("${longitude}")
    private String longitude;
    
    @Value("${latitude}")
    private String latitude;
    
    @Value("${radius}")
    private int radius;
    
    public KakaoPlaceScheduler(KakaoMapService kakaoMapService) {
        this.kakaoMapService = kakaoMapService;
    }

    /**
     * 매일 자정에 실행되는 스케줄러
     * 식당 카테고리 (FD6)에 대한 장소 검색 수행
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void scheduleDailyRestaurantSearch() {
        log.info("Starting scheduled restaurant search...");
        
        // 식당 카테고리로 검색 (FD6: 음식점)
        List<Place> places = kakaoMapService.searchPlacesByCategory("FD6", longitude, latitude, radius);
        log.info("Scheduled restaurant search completed. Found {} places", places.size());
    }
    
    /**
     * 매주 월요일 오전 10시에 실행되는 스케줄러
     * 카페 카테고리 (CE7)에 대한 장소 검색 수행
     */
    @Scheduled(cron = "0 0 10 ? * MON") // 매주 월요일 오전 10시
    public void scheduleWeeklyCafeSearch() {
        log.info("Starting scheduled cafe search...");
        
        // 카페 카테고리로 검색 (CE7: 카페)
        List<Place> places = kakaoMapService.searchPlacesByCategory("CE7", longitude, latitude, radius);
        log.info("Scheduled cafe search completed. Found {} places", places.size());
    }
} 