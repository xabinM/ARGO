package com.argo.backend.mission.repository;

import com.argo.backend.domain.ploblem.QuizProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizProblemRepository extends JpaRepository<QuizProblem, Long> {
    List<QuizProblem> findBySpotId(Long spotId);
}
