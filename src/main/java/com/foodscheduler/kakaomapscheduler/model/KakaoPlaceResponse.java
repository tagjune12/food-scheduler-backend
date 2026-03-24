package com.foodscheduler.kakaomapscheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 카카오 로컬 검색 API의 카테고리별 장소 검색 응답 DTO.
 *
 * <p>카카오 API {@code /v2/local/search/category.json} 엔드포인트의
 * JSON 응답을 매핑합니다.</p>
 *
 * @see <a href="https://developers.kakao.com/docs/latest/ko/local/dev-guide#search-by-category">카카오 카테고리 검색 API 문서</a>
 */
@Data
public class KakaoPlaceResponse {

    /** 검색 메타 정보 (총 개수, 페이지 가능 개수, 마지막 페이지 여부) */
    @JsonProperty("meta")
    private Meta meta;

    /** 검색 결과 장소 목록 */
    @JsonProperty("documents")
    private List<Document> documents;

    /**
     * 검색 결과의 메타 정보.
     * 페이징 처리 및 격자 분할 판단에 사용됩니다.
     */
    @Data
    public static class Meta {

        /** 검색된 전체 장소 수 */
        @JsonProperty("total_count")
        private int totalCount;

        /** 노출 가능한 장소 수 (최대 45) */
        @JsonProperty("pageable_count")
        private int pageableCount;

        /** 마지막 페이지 여부 */
        @JsonProperty("is_end")
        private boolean isEnd;
    }

    /**
     * 검색 결과의 개별 장소 정보.
     * {@link com.foodscheduler.kakaomapscheduler.entity.Place} 엔티티로 변환되어 DB에 저장됩니다.
     */
    @Data
    public static class Document {

        /** 장소 고유 ID */
        @JsonProperty("id")
        private String id;

        /** 장소명 */
        @JsonProperty("place_name")
        private String placeName;

        /** 카테고리 전체 이름 (예: "음식점 > 한식 > 육류,고기") */
        @JsonProperty("category_name")
        private String categoryName;

        /** 카테고리 그룹 코드 (예: FD6-음식점, CE7-카페) */
        @JsonProperty("category_group_code")
        private String categoryGroupCode;

        /** 카테고리 그룹 이름 (예: "음식점", "카페") */
        @JsonProperty("category_group_name")
        private String categoryGroupName;

        /** 전화번호 */
        @JsonProperty("phone")
        private String phone;

        /** 지번 주소 */
        @JsonProperty("address_name")
        private String addressName;

        /** 도로명 주소 */
        @JsonProperty("road_address_name")
        private String roadAddressName;

        /** 경도 (X좌표) */
        @JsonProperty("x")
        private String longitude;

        /** 위도 (Y좌표) */
        @JsonProperty("y")
        private String latitude;

        /** 카카오맵 장소 상세 페이지 URL */
        @JsonProperty("place_url")
        private String placeUrl;

        /** 중심 좌표로부터의 거리 (미터) */
        @JsonProperty("distance")
        private String distance;
    }
}
