package com.argo.backend.domain.team.repository;

import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.team.entity.Team;
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
    
    // 반의 모든 팀과 멤버들 조회 (반 상세정보용)
    @Query("SELECT t FROM Team t " +
           "LEFT JOIN FETCH t.classRoom " +
           "WHERE t.classRoom.classId = :classId " +
           "ORDER BY t.createdAt ASC")
    List<Team> findTeamsByClassId(@Param("classId") Long classId);
    
    // 팀 정보와 멤버 수를 한 번에 조회 (N+1 쿼리 방지)
    @Query("SELECT t.teamId, t.teamName, t.maxMembers, COUNT(u) " +
           "FROM Team t LEFT JOIN User u ON u.team.teamId = t.teamId " +
           "WHERE t.classRoom.classId = :classId " +
           "GROUP BY t.teamId, t.teamName, t.maxMembers " +
           "ORDER BY t.createdAt ASC")
    List<Object[]> findTeamStatusByClassId(@Param("classId") Long classId);
}