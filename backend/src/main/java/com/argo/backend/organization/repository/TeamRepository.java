package com.argo.backend.organization.repository;

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    
    // 반 내 팀 이름 중복 검사
    boolean existsByTeamNameAndClassRoom(String teamName, ClassRoom classRoom);
    
    // 반의 모든 팀 조회
    List<Team> findByClassRoomOrderByCreatedAtAsc(ClassRoom classRoom);
    
    // 팀 ID와 반 ID로 팀 조회 (권한 검증용)
    Team findByTeamIdAndClassRoom(Long teamId, ClassRoom classRoom);
    
    // 반의 모든 팀 조회 (멤버 수와 함께)
    @Query("SELECT t FROM Team t " +
           "LEFT JOIN FETCH t.classRoom " +
           "WHERE t.classRoom = :classRoom " +
           "ORDER BY t.createdAt ASC")
    List<Team> findAllTeamsWithDetailsByClassRoom(@Param("classRoom") ClassRoom classRoom);
}