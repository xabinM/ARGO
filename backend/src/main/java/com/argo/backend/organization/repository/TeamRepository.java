package com.argo.backend.organization.repository;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    
    // 반 내 팀 이름 중복 검사
    boolean existsByTeamNameAndClassRoom(String teamName, ClassRoom classRoom);
    
    // 반의 모든 팀 조회
    List<Team> findByClassRoomOrderByCreatedAtAsc(ClassRoom classRoom);
}