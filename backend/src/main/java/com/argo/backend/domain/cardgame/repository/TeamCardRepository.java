package com.argo.backend.domain.cardgame.repository;

import com.argo.backend.domain.cardgame.entity.TeamCard;
import com.argo.backend.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamCardRepository extends JpaRepository<TeamCard, Long> {
    List<TeamCard> findByTeamOrderByCreatedAtDesc(Team team);

    List<TeamCard> findByTeamOrderByTierAsc(Team team);
}