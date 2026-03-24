package com.foodscheduler.kakaomapscheduler.service;

import com.foodscheduler.kakaomapscheduler.entity.Center;
import com.foodscheduler.kakaomapscheduler.entity.Place;
import com.foodscheduler.kakaomapscheduler.mapper.CenterMapper;
import com.foodscheduler.kakaomapscheduler.mapper.PlaceMapper;
import com.foodscheduler.kakaomapscheduler.model.KakaoPlaceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 카카오맵 API를 사용한 장소 검색 및 DB 저장 서비스.
 *
 * <p>센터 좌표를 기준으로 검색 영역(Rect)을 생성하고,
 * 카카오 로컬 검색 API를 호출하여 장소를 검색합니다.
 * 검색 결과가 API 최대치(45건)에 도달하면 영역을 4분할하여
 * 재귀적으로 탐색하는 격자 분할 방식을 사용합니다.</p>
 *
 * @see CenterMapper
 * @see PlaceMapper
 */
@Service
@Slf4j
public class KakaoMapService {

    private final PlaceMapper placeMapper;
    private final WebClient kakaoClient;
    private final CenterMapper centerMapper;

    /**
     * @param placeMapper  장소 DB 매퍼
     * @param kakaoClient  카카오맵 API WebClient
     * @param centerMapper 센터 좌표 DB 매퍼
     */
    public KakaoMapService(PlaceMapper placeMapper, @Qualifier("kakaoClient") WebClient kakaoClient,
            CenterMapper centerMapper) {
        this.placeMapper = placeMapper;
        this.kakaoClient = kakaoClient;
        this.centerMapper = centerMapper;
    }

    /**
     * 기본 센터(test, 판교역 제외)를 대상으로 카테고리별 장소를 검색하여 DB에 저장합니다.
     *
     * @param categoryGroupCode 카테고리 그룹 코드 (예: FD6-음식점, CE7-카페)
     * @return 검색된 장소 총 개수
     */
    public int searchPlacesByCategory(String categoryGroupCode) {
        return searchPlaces(categoryGroupCode, centerMapper.findAllCenters());
    }

    /**
     * test 센터만 대상으로 카테고리별 장소를 검색하여 DB에 저장합니다.
     *
     * @param categoryGroupCode 카테고리 그룹 코드
     * @return 검색된 장소 총 개수
     */
    public int searchPlacesByCategoryForTest(String categoryGroupCode) {
        return searchPlaces(categoryGroupCode, centerMapper.findTestCenters());
    }

    /**
     * 판교역 센터만 대상으로 카테고리별 장소를 검색하여 DB에 저장합니다.
     *
     * @param categoryGroupCode 카테고리 그룹 코드
     * @return 검색된 장소 총 개수
     */
    public int searchPlacesByCategoryForPangyo(String categoryGroupCode) {
        return searchPlaces(categoryGroupCode, centerMapper.findPangyoCenters());
    }

    /**
     * 주어진 센터 목록을 대상으로 격자 탐색 방식의 장소 검색을 수행합니다.
     * 각 센터의 좌표와 반경을 기반으로 검색 사각형을 생성한 후 재귀 탐색합니다.
     *
     * @param categoryGroupCode 카테고리 그룹 코드
     * @param centers           검색 대상 센터 목록
     * @return 검색된 장소 총 개수
     */
    private int searchPlaces(String categoryGroupCode, List<Center> centers) {
        int totalCount = 0;

        try {
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
     * 재귀적 격자 탐색을 수행합니다.
     * 현재 영역에서 최대 3페이지까지 검색하고, 결과가 45건(API 최대치)에
     * 도달하면 영역을 4분할하여 각각 재귀 검색합니다.
     *
     * @param categoryGroupCode 카테고리 그룹 코드
     * @param rect              검색 사각형 영역
     * @return 해당 영역에서 검색된 장소 총 개수
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

    /**
     * 카카오 로컬 검색 API를 호출하여 사각형 영역 내 장소를 검색합니다.
     *
     * @param categoryGroupCode 카테고리 그룹 코드
     * @param rect              검색 사각형 영역
     * @param page              페이지 번호 (1~3)
     * @return API 응답 객체, 실패 시 null
     */
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

    /**
     * 카카오 API의 rect 파라미터에 사용되는 검색 사각형 영역.
     * 위경도 좌표로 정의되며 4분할(split)이 가능합니다.
     */
    private static class Rect {
        double minX, minY, maxX, maxY;

        /**
         * @param minX 최소 경도
         * @param minY 최소 위도
         * @param maxX 최대 경도
         * @param maxY 최대 위도
         */
        public Rect(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        /**
         * 영역을 4등분하여 좌하, 우하, 좌상, 우상 사각형을 반환합니다.
         *
         * @return 4분할된 사각형 목록
         */
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

        /**
         * 영역이 추가 분할 가능한지 확인합니다.
         * 가로 또는 세로가 0.00005도 미만이면 분할을 중단합니다.
         *
         * @return 분할 가능 여부
         */
        public boolean isSplittable() {
            return (maxX - minX) > 0.00005 && (maxY - minY) > 0.00005;
        }

        @Override
        public String toString() {
            return String.format("%.6f,%.6f,%.6f,%.6f", minX, minY, maxX, maxY);
        }
    }

    /**
     * 장소 목록을 건별로 DB에 저장합니다.
     * 개별 저장 실패 시 해당 건만 건너뛰고 나머지는 계속 처리합니다.
     *
     * @param places 저장할 장소 목록
     */
    public void processPlacesInNewTransaction(List<Place> places) {
        for (Place place : places) {
            try {
                placeMapper.upsert(place);
            } catch (Exception e) {
                log.warn("장소 저장 실패 (id: {}): {}", place.getId(), e.getMessage());
            }
        }
    }

    /**
     * 특정 카테고리의 오래된 장소 정보를 삭제합니다.
     * updated_at이 7일 이상 지난 데이터가 삭제 대상입니다.
     *
     * @param categoryGroupCode 카테고리 그룹 코드 (예: FD6, CE7)
     */
    @Transactional
    public void deleteAll(String categoryGroupCode) {
        try {
            log.debug("오래된 {}(으)로 분류된 장소 삭제 중...", categoryGroupCode);
            placeMapper.deleteOldPlaces(categoryGroupCode);
            log.debug("오래된 {}(으)로 분류된 장소 삭제 완료", categoryGroupCode);
        } catch (Exception e) {
            log.error("카테고리 {} 장소 삭제 중 오류 발생: {}", categoryGroupCode, e.getMessage(), e);
        }
    }

    /**
     * 카카오 API 응답의 Document를 Place 엔티티로 변환합니다.
     * createdAt과 updatedAt은 현재 시각으로 설정됩니다.
     *
     * @param document 카카오 API 응답 Document
     * @return 변환된 Place 엔티티
     */
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
