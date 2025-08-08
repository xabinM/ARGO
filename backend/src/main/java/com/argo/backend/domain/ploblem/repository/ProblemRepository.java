package com.argo.backend.domain.ploblem.repository;

import com.argo.backend.domain.ploblem.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("SELECT p FROM Problem p ORDER BY function('RAND')")
    List<Problem> findRandomProblems();

    List<Problem> findAllBySpotId(Long spotId);
}
