package com.argo.backend.organization.repository;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.ApplicationStatus;
import com.argo.backend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassApplicationRepository extends JpaRepository<ClassApplication, Long> {
    
    boolean existsByUserAndClassRoom(User user, ClassRoom classRoom);
    
    // 특정 반의 모든 신청 조회 (페이징)
    @Query("SELECT ca FROM ClassApplication ca JOIN FETCH ca.user WHERE ca.classRoom.classId = :classId")
    Page<ClassApplication> findByClassRoomClassId(@Param("classId") Long classId, Pageable pageable);
    
    // 특정 반의 특정 상태 신청 조회 (페이징)
    @Query("SELECT ca FROM ClassApplication ca JOIN FETCH ca.user WHERE ca.classRoom.classId = :classId AND ca.status = :status")
    Page<ClassApplication> findByClassRoomClassIdAndStatus(@Param("classId") Long classId, @Param("status") ApplicationStatus status, Pageable pageable);
    
    // 통계 정보를 한 번에 조회 (성능 최적화)
    @Query("SELECT " +
           "COUNT(*), " +
           "SUM(CASE WHEN ca.status = 'PENDING' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN ca.status = 'APPROVED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN ca.status = 'REJECTED' THEN 1 ELSE 0 END) " +
           "FROM ClassApplication ca WHERE ca.classRoom.classId = :classId")
    Object[] findStatisticsByClassId(@Param("classId") Long classId);
    
    // 다중 신청 조회 (처리용)
    @Query("SELECT ca FROM ClassApplication ca JOIN FETCH ca.user JOIN FETCH ca.classRoom " + 
           "WHERE ca.applicationId IN :applicationIds AND ca.classRoom.classId = :classId")
    List<ClassApplication> findByApplicationIdsAndClassId(
        @Param("applicationIds") List<Long> applicationIds, 
        @Param("classId") Long classId
    );
    
    // 승인된 학생 수 조회 (반 목록 조회용)
    long countByClassRoomClassIdAndStatus(@Param("classId") Long classId, @Param("status") ApplicationStatus status);
}
