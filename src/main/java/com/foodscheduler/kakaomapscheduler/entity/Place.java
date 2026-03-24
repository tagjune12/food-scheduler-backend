package com.foodscheduler.kakaomapscheduler.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 카카오맵에서 검색된 장소 정보를 나타내는 엔티티.
 * DB의 {@code places} 테이블과 매핑됩니다.
 *
 * 카카오 API 응답({@link com.foodscheduler.kakaomapscheduler.model.KakaoPlaceResponse.Document})에서
 * 변환되어 저장되며, 동일한 {@code id}가 이미 존재하면 INSERT를 건너뜁니다 (ON CONFLICT DO NOTHING).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {

    /** 카카오맵 장소 고유 ID (PK) */
    private String id;

    /** 장소명 */
    private String placeName;

    /** 카테고리 전체 이름 */
    private String categoryName;

    /** 카테고리 그룹 코드 (FD6: 음식점, CE7: 카페) */
    private String categoryGroupCode;

    /** 카테고리 그룹 이름 */
    private String categoryGroupName;

    /** 전화번호 */
    private String phone;

    /** 지번 주소 */
    private String addressName;

    /** 도로명 주소 */
    private String roadAddressName;

    /** 경도 (X좌표) */
    private String longitude;

    /** 위도 (Y좌표) */
    private String latitude;

    /** 카카오맵 장소 상세 페이지 URL */
    private String placeUrl;

    /** 최초 저장 시각 */
    private LocalDateTime createdAt;

    /** 마지막 업데이트 시각 (7일 이상 갱신되지 않으면 삭제 대상) */
    private LocalDateTime updatedAt;
}
