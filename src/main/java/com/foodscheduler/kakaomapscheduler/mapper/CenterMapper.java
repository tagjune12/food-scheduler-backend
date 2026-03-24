package com.foodscheduler.kakaomapscheduler.mapper;

import com.foodscheduler.kakaomapscheduler.entity.Center;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * centers 테이블에 대한 MyBatis Mapper 인터페이스.
 *
 * <p>센터 이름(name) 기준으로 필터링하여 조회하며,
 * SQL 정의는 {@code resources/mapper/CenterMapper.xml}에 있습니다.</p>
 */
@Mapper
public interface CenterMapper {

    /**
     * test, 판교역을 제외한 기본 센터 목록을 조회합니다.
     * ({@code WHERE name NOT LIKE 'test%' AND name NOT LIKE '판교역%'})
     *
     * @return 기본 센터 목록
     */
    List<Center> findAllCenters();

    /**
     * name이 'test'로 시작하는 센터 목록을 조회합니다.
     *
     * @return test 센터 목록
     */
    List<Center> findTestCenters();

    /**
     * name이 '판교역'으로 시작하는 센터 목록을 조회합니다.
     *
     * @return 판교역 센터 목록
     */
    List<Center> findPangyoCenters();
}
