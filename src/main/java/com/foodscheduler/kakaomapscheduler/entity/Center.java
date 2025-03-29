package com.foodscheduler.kakaomapscheduler.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "centers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Center {

  @Id
  private Integer id;

  @Column(name = "longitude_y")
  private String longitudeY;

  @Column(name = "latitude_x")
  private String latitudeX;

  private String radius;
  private String name;
}
