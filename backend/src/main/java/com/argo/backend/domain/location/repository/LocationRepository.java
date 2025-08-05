package com.argo.backend.domain.location.repository;

import com.argo.backend.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByName(String locationName);

}
