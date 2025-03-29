package com.foodscheduler.kakaomapscheduler.service;

import com.foodscheduler.kakaomapscheduler.entity.Center;
import com.foodscheduler.kakaomapscheduler.entity.Place;
import com.foodscheduler.kakaomapscheduler.model.KakaoPlaceResponse;
import com.foodscheduler.kakaomapscheduler.repository.CenterRepository;
import com.foodscheduler.kakaomapscheduler.repository.PlaceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KakaoMapService {

    private final PlaceRepository placeRepository;
    private final WebClient kakaoClient;
    private final CenterRepository centerRepository;

    public KakaoMapService(PlaceRepository placeRepository, @Qualifier("kakaoClient") WebClient kakaoClient,
            CenterRepository centerRepository) {
        this.placeRepository = placeRepository;
        this.kakaoClient = kakaoClient;
        this.centerRepository = centerRepository;
    }

    /**
     * 카테고리로 장소를 검색하고 데이터베이스에 저장
     * 
     * @param categoryGroupCode 카테고리 그룹 코드
     * @param x                 중심 좌표의 X값 (경도)
     * @param y                 중심 좌표의 Y값 (위도)
     * @param radius            검색 반경 (미터 단위)
     * @return 검색된 장소 총 개수
     */
    public int searchPlacesByCategory(String categoryGroupCode) {
        int totalCount = 0;

        try {
            // 중심 좌표 조회
            List<Center> centers = centerRepository.findAllCenters();
            for (Center center : centers) {
                String x = center.getLatitudeX();
                String y = center.getLongitudeY();
                String radius = center.getRadius();

                int pageNo = 1;
                boolean hasNextPage = true;

                log.info("장소명: {}    중심 좌표: {}, {}, {}", center.getName(), x, y, radius);
                // 카테고리로 조회 시작
                while (hasNextPage) {
                    String uri = UriComponentsBuilder.fromPath("/v2/local/search/category.json")
                            .queryParam("category_group_code", categoryGroupCode)
                            .queryParam("x", x)
                            .queryParam("y", y)
                            .queryParam("radius", radius)
                            .queryParam("size", 15)
                            .queryParam("page", pageNo)
                            .build()
                            .toUriString();

                    KakaoPlaceResponse response = kakaoClient.get()
                            .uri(uri)
                            .retrieve()
                            .bodyToMono(KakaoPlaceResponse.class)
                            .block();

                    if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
                        log.info("No places found for category: {}", categoryGroupCode);
                        return totalCount;
                    }

                    List<Place> places = response.getDocuments().stream()
                            .map(this::convertToPlace)
                            .collect(Collectors.toList());

                    // 각 페이지를 개별적으로 처리하여 Prepared Statement 문제 방지
                    processPlacesInNewTransaction(places);

                    totalCount += places.size();

                    // 페이지가 더 있는지 확인하고 페이지 번호 증가
                    if (response.getMeta().isEnd() || pageNo >= response.getMeta().getPageableCount()) {
                        hasNextPage = false;
                    } else {
                        log.info(pageNo + "번 페이지 끝. 다음 페이지로 이동");
                        pageNo++; // 다음 페이지로 이동
                    }
                }
            }

            return totalCount;
        } catch (Exception e) {
            log.error("Error fetching places from Kakao API: {}", e.getMessage(), e);
            return totalCount;
        }
    }

    /**
     * 새로운 트랜잭션에서 장소 정보를 처리합니다.
     * 이렇게 하면 각 페이지마다 새로운 트랜잭션이 생성되어
     * prepared statement 오류를 방지할 수 있습니다.
     * 
     * @param places 저장할 장소 목록
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void processPlacesInNewTransaction(List<Place> places) {
        placeRepository.upsert(places);
    }

    /**
     * 특정 카테고리의 오래된 장소 정보를 삭제합니다.
     * 1일 이상 지난 데이터만 삭제됩니다.
     * 
     * @param categoryGroupCode 카테고리 그룹 코드
     */
    @Transactional
    public void deleteAll(String categoryGroupCode) {
        try {
            log.debug("오래된 {}(으)로 분류된 장소 삭제 중...", categoryGroupCode);
            placeRepository.deleteAll(categoryGroupCode);
            log.debug("오래된 {}(으)로 분류된 장소 삭제 완료", categoryGroupCode);
        } catch (Exception e) {
            log.error("카테고리 {} 장소 삭제 중 오류 발생: {}", categoryGroupCode, e.getMessage(), e);
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