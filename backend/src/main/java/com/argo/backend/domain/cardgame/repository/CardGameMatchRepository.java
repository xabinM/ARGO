package com.argo.backend.domain.cardgame.repository;

import com.argo.backend.domain.cardgame.entity.CardGameMatch;
import com.argo.backend.domain.cardgame.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CardGameMatchRepository extends JpaRepository<CardGameMatch, Long> {
    
    @Query("SELECT m FROM CardGameMatch m WHERE m.challengerTeam.teamId = :teamId OR m.challengedTeam.teamId = :teamId ORDER BY m.createdAt DESC")
    List<CardGameMatch> findByTeamIdOrderByCreatedAtDesc(@Param("teamId") Long teamId);
    
    // N+1 문제 해결: 배틀 기록과 관련 팀들을 한 번에 조회 (FETCH JOIN)
    @Query("SELECT DISTINCT m FROM CardGameMatch m " +
           "LEFT JOIN FETCH m.challengerTeam " +
           "LEFT JOIN FETCH m.challengedTeam " +
           "LEFT JOIN FETCH m.winnerTeam " +
           "LEFT JOIN FETCH m.loserTeam " +
           "WHERE (m.challengerTeam.teamId = :teamId OR m.challengedTeam.teamId = :teamId) " +
           "ORDER BY m.createdAt DESC")
    Page<CardGameMatch> findByTeamIdOrderByCreatedAtDescWithTeams(@Param("teamId") Long teamId, Pageable pageable);

    List<CardGameMatch> findAllByStatusAndCreatedAtBefore(MatchStatus status, LocalDateTime cutoff);

    List<CardGameMatch> findAllByStatus(MatchStatus status);
}
