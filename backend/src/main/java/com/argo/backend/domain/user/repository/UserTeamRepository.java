package com.argo.backend.domain.user.repository;

import com.argo.backend.domain.user.entity.User;
import com.argo.backend.domain.user.entity.UserTeam;
import com.argo.backend.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserTeamRepository extends JpaRepository<UserTeam, Long> {
    
    // 특정 유저의 특정 클래스에서의 활성 팀 조회 (FETCH JOIN으로 N+1 방지)
    @Query("SELECT ut FROM UserTeam ut " +
           "JOIN FETCH ut.team t " +
           "JOIN FETCH t.classRoom " +
           "WHERE ut.user.userId = :userId " +
           "AND t.classRoom.classId = :classId " +
           "AND ut.isActive = true")
    Optional<UserTeam> findActiveByUserIdAndClassId(@Param("userId") Long userId, @Param("classId") Long classId);
    
    // 특정 팀의 활성 멤버들 조회 (FETCH JOIN으로 N+1 방지)
    @Query("SELECT ut FROM UserTeam ut " +
           "JOIN FETCH ut.user " +
           "WHERE ut.team.teamId = :teamId " +
           "AND ut.isActive = true " +
           "ORDER BY ut.joinedAt ASC")
    List<UserTeam> findActiveByTeamId(@Param("teamId") Long teamId);
    
    // 특정 팀의 활성 멤버 수 조회 (COUNT 쿼리로 최적화)
    @Query("SELECT COUNT(ut) FROM UserTeam ut " +
           "WHERE ut.team.teamId = :teamId " +
           "AND ut.isActive = true")
    long countActiveByTeamId(@Param("teamId") Long teamId);
    
    // 유저와 팀으로 UserTeam 찾기 (ID 기반으로 최적화)
    @Query("SELECT ut FROM UserTeam ut " +
           "WHERE ut.user.userId = :userId " +
           "AND ut.team.teamId = :teamId")
    Optional<UserTeam> findByUserIdAndTeamId(@Param("userId") Long userId, @Param("teamId") Long teamId);
    
    // 특정 클래스의 모든 활성 멤버 조회 (필요한 필드만 DTO 프로젝션)
    @Query("SELECT ut.user.userId, ut.user.name, ut.team.teamId, ut.team.teamName, ut.joinedAt " +
           "FROM UserTeam ut " +
           "WHERE ut.team.classRoom.classId = :classId " +
           "AND ut.isActive = true " +
           "ORDER BY ut.team.teamName, ut.joinedAt ASC")
    List<Object[]> findActiveUserTeamDataByClassId(@Param("classId") Long classId);
    
    // 특정 유저의 모든 활성 팀 조회 (FETCH JOIN)
    @Query("SELECT ut FROM UserTeam ut " +
           "JOIN FETCH ut.team t " +
           "JOIN FETCH t.classRoom " +
           "WHERE ut.user.userId = :userId " +
           "AND ut.isActive = true " +
           "ORDER BY ut.joinedAt DESC")
    List<UserTeam> findActiveByUserId(@Param("userId") Long userId);
    
    // 특정 유저가 특정 클래스의 어떤 팀에든 속해있는지 확인 (EXISTS 사용)
    @Query("SELECT CASE WHEN COUNT(ut) > 0 THEN true ELSE false END " +
           "FROM UserTeam ut " +
           "WHERE ut.user.userId = :userId " +
           "AND ut.team.classRoom.classId = :classId " +
           "AND ut.isActive = true")
    boolean existsActiveByUserIdAndClassId(@Param("userId") Long userId, @Param("classId") Long classId);
    
    // 특정 팀에 특정 유저가 활성 상태로 속해있는지 확인 (ID 기반)
    @Query("SELECT CASE WHEN COUNT(ut) > 0 THEN true ELSE false END " +
           "FROM UserTeam ut " +
           "WHERE ut.user.userId = :userId " +
           "AND ut.team.teamId = :teamId " +
           "AND ut.isActive = true")
    boolean existsActiveByUserIdAndTeamId(@Param("userId") Long userId, @Param("teamId") Long teamId);
    
    // 특정 클래스에서 팀 미배정 학생들의 ID 조회 (서브쿼리로 최적화)
    @Query("SELECT u.userId FROM User u " +
           "JOIN u.applications ca " +
           "WHERE ca.classRoom.classId = :classId " +
           "AND ca.status = 'APPROVED' " +
           "AND u.role = 'ROLE_STUDENT' " +
           "AND NOT EXISTS (" +
           "    SELECT 1 FROM UserTeam ut " +
           "    WHERE ut.user = u " +
           "    AND ut.team.classRoom.classId = :classId " +
           "    AND ut.isActive = true" +
           ")")
    List<Long> findUnassignedStudentIdsByClassId(@Param("classId") Long classId);
    
    // 특정 클래스에서 팀 배정된 학생들의 정보 조회 (JOIN 최적화)
    @Query("SELECT u.userId, u.name, t.teamId, t.teamName, ut.joinedAt " +
           "FROM UserTeam ut " +
           "JOIN ut.user u " +
           "JOIN ut.team t " +
           "WHERE t.classRoom.classId = :classId " +
           "AND ut.isActive = true " +
           "AND u.role = 'ROLE_STUDENT' " +
           "ORDER BY t.teamName, u.name")
    List<Object[]> findAssignedStudentDataByClassId(@Param("classId") Long classId);
    
    // N+1 문제 해결: 여러 학생 ID 중에서 특정 클래스에 배정된 학생들의 ID만 조회 (배치 처리)
    @Query("SELECT ut.user.userId " +
           "FROM UserTeam ut " +
           "WHERE ut.user.userId IN :studentIds " +
           "AND ut.team.classRoom.classId = :classId " +
           "AND ut.isActive = true")
    List<Long> findAssignedStudentIdsByStudentIdsAndClassId(@Param("studentIds") List<Long> studentIds, 
                                                           @Param("classId") Long classId);
}