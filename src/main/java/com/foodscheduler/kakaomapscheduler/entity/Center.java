package com.foodscheduler.kakaomapscheduler.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장소 검색의 중심 좌표를 나타내는 엔티티.
 * DB의 {@code centers} 테이블과 매핑됩니다.
 *
 * <p>각 센터는 위도/경도 좌표와 검색 반경을 가지며,
 * {@code name} 필드에 따라 스케줄이 분리됩니다.</p>
 * <ul>
 *   <li>{@code name LIKE 'test%'} → test 전용 스케줄</li>
 *   <li>{@code name LIKE '판교역%'} → 판교역 전용 스케줄</li>
 *   <li>그 외 → 기본 스케줄</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Center {

    /** 센터 고유 ID */
    private Integer id;

    /** 중심 좌표의 위도 (Y) */
    private String latitudeY;

    /** 중심 좌표의 경도 (X) */
    private String longitudeX;

    /** 검색 반경 (미터 단위) */
    private String radius;

    /** 센터 이름 (스케줄 분리 기준) */
    private String name;
}
