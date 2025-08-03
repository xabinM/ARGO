package com.argo.backend.organization.repository;

import com.argo.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassStudentRepository extends JpaRepository<User, Long> {
    
    // 반의 승인된 학생들 중 특정 ID 목록으로 조회
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.team " +
           "JOIN u.applications ca " +
           "WHERE u.userId IN :studentIds " +
           "AND ca.classRoom.classId = :classId " +
           "AND ca.status = 'APPROVED' " +
           "AND u.role = 'ROLE_STUDENT'")
    List<User> findApprovedStudentsByIdsAndClassId(
        @Param("studentIds") List<Long> studentIds, 
        @Param("classId") Long classId
    );
    
    // 팀의 현재 멤버 수 조회
    @Query("SELECT COUNT(u) FROM User u WHERE u.team.teamId = :teamId")
    long countByTeamId(@Param("teamId") Long teamId);
    
    // 반의 팀 미배정 학생들 조회
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.team " +
           "JOIN u.applications ca " +
           "WHERE ca.classRoom.classId = :classId " +
           "AND ca.status = 'APPROVED' " +
           "AND u.role = 'ROLE_STUDENT' " +
           "AND u.team IS NULL")
    List<User> findUnassignedStudentsByClassId(@Param("classId") Long classId);
    
    // 반의 승인된 학생들과 팀 정보, 가입일 조회 (반 상세정보용)
    @Query("SELECT u, ca.updatedAt FROM User u " +
           "LEFT JOIN FETCH u.team " +
           "JOIN u.applications ca " +
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
}