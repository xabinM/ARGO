package com.argo.backend.mission.repository;

import com.argo.backend.domain.mission.MissionSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionSessionRepository extends JpaRepository<MissionSession, Long> {
}
