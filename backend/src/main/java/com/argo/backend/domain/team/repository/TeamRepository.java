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
    
    // 반의 모든 팀 조회 (FETCH JOIN으로 N+1 방지)
    @Query("SELECT t FROM Team t JOIN FETCH t.classRoom WHERE t.classRoom = :classRoom ORDER BY t.createdAt ASC")
    List<Team> findByClassRoomOrderByCreatedAtAsc(@Param("classRoom") ClassRoom classRoom);
    
    // N+1 문제 해결: getBattleOpponents용 - 팀과 리더를 한 번에 조회 (FETCH JOIN)
    @Query("SELECT t FROM Team t " +
           "LEFT JOIN FETCH t.leader " +
           "LEFT JOIN FETCH t.classRoom " +
           "WHERE t.classRoom = :classRoom " +
           "ORDER BY t.createdAt ASC")
    List<Team> findByClassRoomWithLeaderOrderByCreatedAtAsc(@Param("classRoom") ClassRoom classRoom);
    
    // 클래스의 팀 개수 조회 (성능 최적화)
    @Query("SELECT COUNT(t) FROM Team t WHERE t.classRoom = :classRoom")
    long countByClassRoom(@Param("classRoom") ClassRoom classRoom);
    
    // 팀 ID와 반 ID로 팀 조회 (권한 검증용, FETCH JOIN 추가)
    @Query("SELECT t FROM Team t JOIN FETCH t.classRoom WHERE t.teamId = :teamId AND t.classRoom = :classRoom")
    Team findByTeamIdAndClassRoom(@Param("teamId") Long teamId, @Param("classRoom") ClassRoom classRoom);
    
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
    
    // 팀 정보와 활성 멤버 수를 한 번에 조회 (UserTeam 기반, N+1 쿼리 방지)
    @Query("SELECT t.teamId, t.teamName, t.maxMembers, COUNT(ut) " +
           "FROM Team t LEFT JOIN UserTeam ut ON ut.team = t AND ut.isActive = true " +
           "WHERE t.classRoom.classId = :classId " +
           "GROUP BY t.teamId, t.teamName, t.maxMembers " +
           "ORDER BY t.createdAt ASC")
    List<Object[]> findTeamStatusByClassId(@Param("classId") Long classId);
    
    // N+1 문제 해결: 팀과 리더, 활성 멤버들을 한 번에 조회 (FETCH JOIN)
    @Query("SELECT DISTINCT t FROM Team t " +
           "LEFT JOIN FETCH t.leader " +
           "LEFT JOIN FETCH t.userTeams ut " +
           "LEFT JOIN FETCH ut.user " +
           "WHERE t.classRoom.classId = :classId " +
           "AND (ut.isActive = true OR ut IS NULL) " +
           "ORDER BY t.createdAt ASC")
    List<Team> findTeamsByClassIdWithActiveMembersAndLeader(@Param("classId") Long classId);
}