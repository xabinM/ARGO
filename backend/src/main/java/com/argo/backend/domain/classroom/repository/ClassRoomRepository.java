package com.argo.backend.domain.classroom.repository;

import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.classroom.enums.ClassStatus;
import com.argo.backend.domain.user.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    boolean existsByInviteCode(String inviteCode);
    
    Optional<ClassRoom> findByInviteCode(String inviteCode);
    
    // 선생님의 반 목록 조회 (상태 필터 포함)
    Page<ClassRoom> findByTeacherAndStatus(Teacher teacher, ClassStatus status, Pageable pageable);
    
    // 선생님의 모든 반 목록 조회
    Page<ClassRoom> findByTeacher(Teacher teacher, Pageable pageable);
    
    // 학생이 참여 중인 반 목록 조회 (승인된 신청만)
    @Query("SELECT DISTINCT ca.classRoom FROM ClassApplication ca " +
           "WHERE ca.user.userId = :studentId " +
           "AND ca.status = 'APPROVED' " +
           "AND ca.classRoom.status = :status " +
           "ORDER BY ca.classRoom.createdAt DESC")
    Page<ClassRoom> findStudentClassesByStatus(@Param("studentId") Long studentId, 
                                               @Param("status") ClassStatus status, 
                                               Pageable pageable);
    
    // 학생이 참여 중인 모든 반 목록 조회 (승인된 신청만)
    @Query("SELECT DISTINCT ca.classRoom FROM ClassApplication ca " +
           "WHERE ca.user.userId = :studentId " +
           "AND ca.status = 'APPROVED' " +
           "ORDER BY ca.classRoom.createdAt DESC")
    Page<ClassRoom> findStudentClasses(@Param("studentId") Long studentId, Pageable pageable);
    
    // N+1 해결을 위한 선생님 반 목록 + 학생수 + 팀수 한번에 조회
    @Query("SELECT c, COUNT(DISTINCT ca), COUNT(DISTINCT t) " +
           "FROM ClassRoom c " +
           "LEFT JOIN c.applications ca ON ca.status = 'APPROVED' " +
           "LEFT JOIN c.teams t " +
           "WHERE c.teacher = :teacher " +
           "GROUP BY c")
    Page<Object[]> findClassRoomsWithCounts(@Param("teacher") Teacher teacher, Pageable pageable);
    
    // 선생님 반 목록 + 학생수 + 팀수 한번에 조회 (상태별)
    @Query("SELECT c, COUNT(DISTINCT ca), COUNT(DISTINCT t) " +
           "FROM ClassRoom c " +
           "LEFT JOIN c.applications ca ON ca.status = 'APPROVED' " +
           "LEFT JOIN c.teams t " +
           "WHERE c.teacher = :teacher AND c.status = :status " +
           "GROUP BY c")
    Page<Object[]> findClassRoomsWithCountsByStatus(@Param("teacher") Teacher teacher, 
                                                   @Param("status") ClassStatus status, 
                                                   Pageable pageable);
    
    // 학생이 참여 중인 반 목록 + 학생수 + 팀수 한번에 조회 (전체)
    @Query("SELECT c, COUNT(DISTINCT ca2), COUNT(DISTINCT t) " +
           "FROM ClassApplication ca " +
           "JOIN ca.classRoom c " +
           "LEFT JOIN ClassApplication ca2 ON c.classId = ca2.classRoom.classId AND ca2.status = 'APPROVED' " +
           "LEFT JOIN Team t ON c.classId = t.classRoom.classId " +
           "WHERE ca.user.userId = :studentId AND ca.status = 'APPROVED' " +
           "GROUP BY c " +
           "ORDER BY c.createdAt DESC")
    Page<Object[]> findStudentClassesWithCounts(@Param("studentId") Long studentId, Pageable pageable);
    
    // 학생이 참여 중인 반 목록 + 학생수 + 팀수 한번에 조회 (상태별)
    @Query("SELECT c, COUNT(DISTINCT ca2), COUNT(DISTINCT t) " +
           "FROM ClassApplication ca " +
           "JOIN ca.classRoom c " +
           "LEFT JOIN ClassApplication ca2 ON c.classId = ca2.classRoom.classId AND ca2.status = 'APPROVED' " +
           "LEFT JOIN Team t ON c.classId = t.classRoom.classId " +
           "WHERE ca.user.userId = :studentId AND ca.status = 'APPROVED' AND c.status = :status " +
           "GROUP BY c " +
           "ORDER BY c.createdAt DESC")
    Page<Object[]> findStudentClassesWithCountsByStatus(@Param("studentId") Long studentId, 
                                                       @Param("status") ClassStatus status, 
                                                       Pageable pageable);

    @Query("SELECT c FROM ClassRoom c " +
            "JOIN FETCH c.location " +
            "JOIN FETCH c.teacher " +
            "WHERE c.inviteCode = :inviteCode")
    Optional<ClassRoom> findByInviteCodeWithLocationAndTeacher(@Param("inviteCode") String inviteCode);
    
    // N+1 문제 해결: getClassDetail용 - ClassRoom과 관련 엔티티들을 한 번에 조회 (FETCH JOIN)
    @Query("SELECT c FROM ClassRoom c " +
           "LEFT JOIN FETCH c.location " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.classId = :classId")
    Optional<ClassRoom> findByIdWithLocationAndTeacher(@Param("classId") Long classId);
    
    // N+1 문제 해결: validateClassAccess용 - ClassRoom과 Teacher를 한 번에 조회 (FETCH JOIN)
    @Query("SELECT c FROM ClassRoom c " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.classId = :classId")
    Optional<ClassRoom> findByIdWithTeacher(@Param("classId") Long classId);
}
