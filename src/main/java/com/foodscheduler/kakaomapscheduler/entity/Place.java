package com.foodscheduler.kakaomapscheduler.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "places")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {
    
    @Id
    private String id;
    
    private String placeName;
    private String categoryName;
    private String categoryGroupCode;
    private String categoryGroupName;
    private String phone;
    private String addressName;
    private String roadAddressName;
    private String longitude;
    private String latitude;
    private String placeUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 