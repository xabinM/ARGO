package com.argo.backend.domain.ploblem.repository;

import com.argo.backend.domain.ploblem.entity.QuizProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizProblemRepository extends JpaRepository<QuizProblem, Long> {
    List<QuizProblem> findBySpotId(Long spotId);
}
