package com.argo.backend.domain.mission.repository;

import com.argo.backend.domain.mission.entity.MissionSession;
import com.argo.backend.domain.mission.enums.MissionSessionStatus;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.team.entity.Team;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MissionSessionRepository extends JpaRepository<MissionSession, Long> {
    Optional<MissionSession> findByTeamAndSpot(Team team, Spot spot);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ms FROM MissionSession ms WHERE ms.sessionId = :sessionId AND ms.status = :status")
    Optional<MissionSession> findBySessionIdAndStatusWithLock(@Param("sessionId") Long sessionId, @Param("status") MissionSessionStatus status);
}
