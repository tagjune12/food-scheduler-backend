package com.foodscheduler.kakaomapscheduler.repository;

import com.foodscheduler.kakaomapscheduler.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, String> {
} 