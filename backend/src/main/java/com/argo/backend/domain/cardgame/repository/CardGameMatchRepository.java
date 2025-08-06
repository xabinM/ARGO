package com.argo.backend.domain.cardgame.repository;

import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardGameMatchRepository extends JpaRepository<CardGameMatch, Long> {
    
    @Query("SELECT m FROM CardGameMatch m WHERE m.challengerTeam.teamId = :teamId OR m.challengedTeam.teamId = :teamId ORDER BY m.createdAt DESC")
    List<CardGameMatch> findByTeamIdOrderByCreatedAtDesc(@Param("teamId") Long teamId);
}