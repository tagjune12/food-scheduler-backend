package com.foodscheduler.kakaomapscheduler.mapper;

import com.foodscheduler.kakaomapscheduler.entity.Place;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * places 테이블에 대한 MyBatis Mapper 인터페이스.
 *
 * <p>장소 INSERT 및 오래된 데이터 삭제 기능을 제공하며,
 * SQL 정의는 {@code resources/mapper/PlaceMapper.xml}에 있습니다.</p>
 */
@Mapper
public interface PlaceMapper {

    /**
     * 장소를 INSERT합니다. 이미 존재하는 id는 무시합니다.
     * ({@code INSERT ... ON CONFLICT (id) DO NOTHING})
     *
     * @param place 저장할 장소
     */
    void upsert(Place place);

    /**
     * 특정 카테고리의 오래된 장소 정보를 삭제합니다.
     * updated_at이 7일 이상 지난 데이터를 삭제합니다.
     *
     * @param categoryGroupCode 카테고리 그룹 코드 (예: FD6, CE7)
     */
    void deleteOldPlaces(@Param("categoryGroupCode") String categoryGroupCode);
}
