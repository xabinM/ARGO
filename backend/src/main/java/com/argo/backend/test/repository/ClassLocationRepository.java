package com.argo.backend.test.repository;

import com.argo.backend.domain.location.ClassLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassLocationRepository extends JpaRepository<ClassLocation, ClassLocation.ClassLocationId> {
}
