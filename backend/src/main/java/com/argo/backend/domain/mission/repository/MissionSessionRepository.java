package com.argo.backend.domain.mission.repository;

import com.argo.backend.domain.mission.entity.MissionSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionSessionRepository extends JpaRepository<MissionSession, Long> {
}
