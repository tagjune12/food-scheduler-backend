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
     * 카테고리로 장소를 검색하고 데이터베이스에 저장 (격자 탐색 방식 적용)
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
            List<Center> centers = centerRepository.findAllCenters();
            for (Center center : centers) {
                double x = Double.parseDouble(center.getLongitudeX());
                double y = Double.parseDouble(center.getLatitudeY());
                double radiusMeters = Double.parseDouble(center.getRadius());

                // 반경을 기준으로 초기 검색 사각형(Rect) 생성
                // 대략적인 위경도 변환: 1도 ≈ 111km (위도), 경도는 위도에 따라 다름.
                double radiusDegree = radiusMeters / 111000.0; // 미터를 도(degree)로 변환

                // 경도 보정 (위도에 따른 경도 거리 차이 고려)
                double radiusDegreeX = radiusDegree / Math.cos(Math.toRadians(y));

                Rect initialRect = new Rect(
                        x - radiusDegreeX,
                        y - radiusDegree,
                        x + radiusDegreeX,
                        y + radiusDegree);

                log.info("격자 탐색 시작: {}, Rect: {}", center.getName(), initialRect);
                totalCount += recursiveSearch(categoryGroupCode, initialRect);
            }

            return totalCount;
        } catch (Exception e) {
            log.error("Error fetching places from Kakao API: {}", e.getMessage(), e);
            return totalCount;
        }
    }

    /**
     * 재귀적 격자 탐색
     * 결과가 45개(API 최대치)에 도달하면 영역을 4분할하여 재검색
     */
    private int recursiveSearch(String categoryGroupCode, Rect rect) {
        int totalCount = 0;
        int pageNo = 1;
        boolean hasNextPage = true;
        boolean isOverflow = false;

        // 1. 현재 영역 검색 (최대 3페이지 확인)
        while (hasNextPage) {
            KakaoPlaceResponse response = searchPlacesByRect(categoryGroupCode, rect, pageNo);

            if (response == null || response.getDocuments() == null) {
                break;
            }

            List<Place> places = response.getDocuments().stream()
                    .map(this::convertToPlace)
                    .collect(Collectors.toList());

            processPlacesInNewTransaction(places);

            int count = places.size();
            totalCount += count;

            // 메타 정보 확인
            if (response.getMeta().isEnd() || pageNo >= 3) { // 3페이지가 최대
                hasNextPage = false;

                // 3페이지 꽉 찼거나, 전체 개수가 45개 이상인 것으로 추정되면 분할 필요
                if (response.getMeta().getPageableCount() >= 45 || response.getMeta().getTotalCount() > 45) {
                    isOverflow = true;
                }
            } else {
                pageNo++;
            }
        }

        // 2. 데이터가 너무 많으면(45개 이상 추정) 4분할하여 재귀 호출
        if (isOverflow && rect.isSplittable()) {
            log.info("데이터 초과로 영역 분할: {}", rect);

            List<Rect> subRects = rect.split();
            for (Rect subRect : subRects) {
                totalCount += recursiveSearch(categoryGroupCode, subRect);
            }
        }

        return totalCount;
    }

    private KakaoPlaceResponse searchPlacesByRect(String categoryGroupCode, Rect rect, int page) {
        try {
            String rectParam = String.format("%f,%f,%f,%f", rect.minX, rect.minY, rect.maxX, rect.maxY);

            String uri = UriComponentsBuilder.fromPath("/v2/local/search/category.json")
                    .queryParam("category_group_code", categoryGroupCode)
                    .queryParam("rect", rectParam)
                    .queryParam("size", 15)
                    .queryParam("page", page)
                    .build()
                    .toUriString();

            return kakaoClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(KakaoPlaceResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("API call failed for rect {}: {}", rect, e.getMessage());
            return null;
        }
    }

    // 내부 클래스: 검색 사각형
    private static class Rect {
        double minX, minY, maxX, maxY;

        public Rect(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        // 영역을 4등분
        public List<Rect> split() {
            double midX = (minX + maxX) / 2;
            double midY = (minY + maxY) / 2;

            return List.of(
                    new Rect(minX, minY, midX, midY), // 좌하
                    new Rect(midX, minY, maxX, midY), // 우하
                    new Rect(minX, midY, midX, maxY), // 좌상
                    new Rect(midX, midY, maxX, maxY) // 우상
            );
        }

        // 너무 작게 쪼개지는지 확인 (예: 0.00005도 미만이면 중단)
        public boolean isSplittable() {
            return (maxX - minX) > 0.00005 && (maxY - minY) > 0.00005;
        }

        @Override
        public String toString() {
            return String.format("%.6f,%.6f,%.6f,%.6f", minX, minY, maxX, maxY);
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