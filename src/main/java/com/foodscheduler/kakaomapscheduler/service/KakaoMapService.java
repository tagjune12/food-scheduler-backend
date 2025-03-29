package com.foodscheduler.kakaomapscheduler.service;

import com.foodscheduler.kakaomapscheduler.entity.Place;
import com.foodscheduler.kakaomapscheduler.model.KakaoPlaceResponse;
import com.foodscheduler.kakaomapscheduler.repository.PlaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KakaoMapService {

    private final PlaceRepository placeRepository;
    private final WebClient kakaoClient;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Value("${kakao.api.url}")
    private String kakaoApiUrl;

    public KakaoMapService(PlaceRepository placeRepository, @Qualifier("kakaoClient") WebClient kakaoClient) {
        this.placeRepository = placeRepository;
        this.kakaoClient = kakaoClient;
    }

    /**
     * 카테고리로 장소를 검색하고 데이터베이스에 저장
     * @param categoryGroupCode 카테고리 그룹 코드
     * @param x 중심 좌표의 X값 (경도)
     * @param y 중심 좌표의 Y값 (위도)
     * @param radius 검색 반경 (미터 단위)
     * @return 검색된 장소 목록
     */
    public List<Place> searchPlacesByCategory(String categoryGroupCode, String x, String y, int radius) {
        try {
            KakaoPlaceResponse response = kakaoClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(kakaoApiUrl)
                            .queryParam("category_group_code", categoryGroupCode)
                            .queryParam("x", x)
                            .queryParam("y", y)
                            .queryParam("radius", radius)
                            .queryParam("size", 15)
                            .build())
                    .header("Authorization", "KakaoAK " + kakaoApiKey)
                    .retrieve()
                    .bodyToMono(KakaoPlaceResponse.class)
                    .block();

            if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
                log.info("No places found for category: {}", categoryGroupCode);
                return Collections.emptyList();
            }

            List<Place> places = response.getDocuments().stream()
                    .map(this::convertToPlace)
                    .collect(Collectors.toList());

            return placeRepository.saveAll(places);

        } catch (Exception e) {
            log.error("Error fetching places from Kakao API: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private Place convertToPlace(KakaoPlaceResponse.Document document) {
        LocalDateTime now = LocalDateTime.now();
        return Place.builder()
                .id(document.getId())
                .placeName(document.getPlaceName())
                .categoryName(document.getCategoryName())
                .categoryGroupCode(document.getCategoryGroupCode())
                .categoryGroupName(document.getCategoryGroupName())
                .phone(document.getPhone())
                .addressName(document.getAddressName())
                .roadAddressName(document.getRoadAddressName())
                .longitude(document.getLongitude())
                .latitude(document.getLatitude())
                .placeUrl(document.getPlaceUrl())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
} 