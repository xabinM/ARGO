package com.argo.backend.mission.repository;

import com.argo.backend.domain.ploblem.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query("SELECT p FROM Problem p ORDER BY function('RAND')")
    List<Problem> findRandomProblems();
}
