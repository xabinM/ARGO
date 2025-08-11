package com.argo.backend.domain.spot.repository;

import com.argo.backend.domain.spot.entity.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long> {
    List<Spot> findByLocationLocationId(Long locationId);
}
