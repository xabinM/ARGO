package com.argo.backend.domain.ploblem.repository;

import com.argo.backend.domain.ploblem.entity.SelfieProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SelfieProblemRepository extends JpaRepository<SelfieProblem, Long> {
    List<SelfieProblem> findBySpotId(Long spotId);
}
