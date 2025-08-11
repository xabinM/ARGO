package com.argo.backend.domain.mission.repository;

import com.argo.backend.domain.mission.entity.MissionSession;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MissionSessionRepository extends JpaRepository<MissionSession, Long> {
    Optional<MissionSession> findByTeamAndSpot(Team team, Spot spot);
}
