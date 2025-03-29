package com.foodscheduler.kakaomapscheduler.repository;

import com.foodscheduler.kakaomapscheduler.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Repository
public interface PlaceRepository extends JpaRepository<Place, String> {
  // @Query("SELECT p FROM Place p WHERE p.placeName = :placeName")
  // Optional<Place> findByPlaceName(@Param("placeName") String placeName);

  /**
   * 특정 카테고리의 오래된 장소 정보를 삭제합니다.
   * 하루 이상 지난 데이터를 삭제합니다.
   *
   * @param categoryGroupCode 카테고리 그룹 코드
   */
  @Modifying
  @Transactional
  @Query(value = "DELETE FROM places WHERE updated_at < (CURRENT_TIMESTAMP - INTERVAL '1 day') AND category_group_code = :categoryGroupCode", nativeQuery = true)
  void deleteAll(@Param("categoryGroupCode") String categoryGroupCode);

  /**
   * 엔티티 목록을 저장하고, 이미 존재하는 엔티티는 업데이트합니다.
   * 
   * @param places 저장할 엔티티 목록
   * @return 저장된 엔티티 목록
   */
  @Transactional
  default List<Place> upsert(List<Place> places) {
    // 기존 엔티티의 ID 목록을 먼저 조회하여 한 번에 가져옴
    List<String> placeIds = places.stream().map(Place::getId).collect(Collectors.toList());
    List<Place> existingPlaces = findAllById(placeIds);

    // ID를 키로 하는 맵 생성
    Map<String, Place> existingPlacesMap = existingPlaces.stream()
        .collect(Collectors.toMap(Place::getId, place -> place));

    // 업데이트할 데이터 준비
    places.forEach(place -> {
      if (existingPlacesMap.containsKey(place.getId())) {
        // 기존 엔티티가 있으면 생성 시간 유지
        place.setCreatedAt(existingPlacesMap.get(place.getId()).getCreatedAt());
      }
    });

    // 한 번에 저장
    int batchSize = 10; // 배치 크기 설정
    List<Place> result = new ArrayList<>();

    for (int i = 0; i < places.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, places.size());
      List<Place> batch = places.subList(i, endIndex);
      List<Place> savedBatch = saveAll(batch);
      result.addAll(savedBatch);
    }

    return result;
  }
}