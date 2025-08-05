package com.argo.backend.mission.repository;

import com.argo.backend.domain.spot.Spot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotRepository extends JpaRepository<Spot, Long> {

}
