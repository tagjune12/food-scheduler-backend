package com.foodscheduler.kakaomapscheduler.repository;

import com.foodscheduler.kakaomapscheduler.entity.Center;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CenterRepository extends JpaRepository<Center, Integer> {

  /*
   * centers 테이블에서 중심 좌표 조회
   */
  @Query(value = "SELECT * FROM centers", nativeQuery = true)
  List<Center> findAllCenters();
}
