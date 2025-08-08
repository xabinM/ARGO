package com.argo.backend.domain.classroom.repository;

import com.argo.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClassStudentRepository extends JpaRepository<User, Long> {
    
    // 반의 승인된 학생들 중 특정 ID 목록으로 조회 (DTO Projection으로 N+1 방지)
    @Query("SELECT u, ca.updatedAt, " +
           "(SELECT t FROM Team t JOIN UserTeam ut ON ut.team = t " +
           " WHERE ut.user = u AND ut.isActive = true AND t.classRoom.classId = :classId) as team " +
           "FROM User u JOIN u.applications ca " +
           "WHERE u.userId IN :studentIds " +
           "AND ca.classRoom.classId = :classId " +
           "AND ca.status = 'APPROVED' " +
           "AND u.role = 'ROLE_STUDENT'")
    List<Object[]> findApprovedStudentsByIdsAndClassIdWithTeam(
        @Param("studentIds") List<Long> studentIds, 
        @Param("classId") Long classId
    );
    
    // 팀의 현재 활성 멤버 수 조회 (UserTeam 기반)
    @Query("SELECT COUNT(ut) FROM UserTeam ut WHERE ut.team.teamId = :teamId AND ut.isActive = true")
    long countByTeamId(@Param("teamId") Long teamId);
    
    // 반의 팀 미배정 학생들 조회 (UserTeam 기반)
    @Query("SELECT u FROM User u " +
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
    List<User> findUnassignedStudentsByClassId(@Param("classId") Long classId);
    
    // 반의 승인된 학생들과 팀 정보, 가입일 조회 (UserTeam 기반)
    @Query("SELECT u, ca.updatedAt, ut.team, ut.joinedAt FROM User u " +
           "JOIN u.applications ca " +
           "LEFT JOIN UserTeam ut ON u = ut.user AND ut.isActive = true AND ut.team.classRoom.classId = :classId " +
           "WHERE ca.classRoom.classId = :classId " +
           "AND ca.status = 'APPROVED' " +
           "AND u.role = 'ROLE_STUDENT' " +
           "ORDER BY u.name ASC")
    List<Object[]> findApprovedStudentsWithTeamAndJoinDateByClassId(@Param("classId") Long classId);
    
    // 학생이 특정 반에 참여하고 있는지 확인
    @Query("SELECT COUNT(ca) > 0 FROM ClassApplication ca " +
           "WHERE ca.user.userId = :studentId " +
           "AND ca.classRoom.classId = :classId " +
           "AND ca.status = 'APPROVED'")
    boolean isStudentInClass(@Param("studentId") Long studentId, @Param("classId") Long classId);
    
    // 반의 모든 승인된 학생들 조회 (페이징, UserTeam 기반)
    @Query("SELECT u, ca.updatedAt, ut.team, ut.joinedAt FROM User u " +
           "JOIN u.applications ca " +
           "LEFT JOIN UserTeam ut ON u = ut.user AND ut.isActive = true AND ut.team.classRoom.classId = :classId " +
           "WHERE ca.classRoom.classId = :classId " +
           "AND ca.status = 'APPROVED' " +
           "AND u.role = 'ROLE_STUDENT' " +
           "ORDER BY ca.updatedAt ASC")
    Page<Object[]> findApprovedStudentsWithTeamAndJoinDateByClassIdPaged(@Param("classId") Long classId, Pageable pageable);
    
    // 반의 팀 배정된 학생들 조회 (페이징, UserTeam 기반)
    @Query("SELECT u, ca.updatedAt, ut.team, ut.joinedAt FROM User u " +
           "JOIN u.applications ca " +
           "JOIN UserTeam ut ON u = ut.user AND ut.isActive = true AND ut.team.classRoom.classId = :classId " +
           "WHERE ca.classRoom.classId = :classId " +
           "AND ca.status = 'APPROVED' " +
           "AND u.role = 'ROLE_STUDENT' " +
           "ORDER BY ca.updatedAt ASC")
    Page<Object[]> findAssignedStudentsWithTeamAndJoinDateByClassIdPaged(@Param("classId") Long classId, Pageable pageable);
    
    // 반의 팀 미배정 학생들 조회 (페이징, UserTeam 기반)
    @Query("SELECT u, ca.updatedAt, null, null FROM User u " +
           "JOIN u.applications ca " +
           "WHERE ca.classRoom.classId = :classId " +
           "AND ca.status = 'APPROVED' " +
           "AND u.role = 'ROLE_STUDENT' " +
           "AND NOT EXISTS (" +
           "    SELECT 1 FROM UserTeam ut " +
           "    WHERE ut.user = u " +
           "    AND ut.team.classRoom.classId = :classId " +
           "    AND ut.isActive = true" +
           ") " +
           "ORDER BY ca.updatedAt ASC")
    Page<Object[]> findUnassignedStudentsWithTeamAndJoinDateByClassIdPaged(@Param("classId") Long classId, Pageable pageable);
    
}