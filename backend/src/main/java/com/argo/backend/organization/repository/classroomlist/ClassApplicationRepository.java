package com.argo.backend.organization.repository.classroomlist;
/**
 * 반 신청(ClassApplication) 엔티티에 대한 데이터 접근을 담당하는 리포지토리 인터페이스
 * 반 신청 정보 조회, 승인된 학생 수 계산 등의 데이터베이스 작업을 처리
 */

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
import java.util.Optional;

public interface ClassApplicationRepository extends JpaRepository<ClassApplication, Long> {
    /**
     * 특정 반의 승인된 학생 수를 조회
     * @param classRoom 반 정보
     * @param status 신청 상태 (APPROVED)
     * @return 승인된 학생 수
     */
    @Query("SELECT COUNT(ca) FROM ClassApplication ca " +
           "WHERE ca.classRoom = :classRoom AND ca.status = :status")
    Long countByClassRoomAndStatus(@Param("classRoom") ClassRoom classRoom,
                                   @Param("status") ApplicationStatus status);

    /**
     * 특정 사용자와 반의 신청을 조회
     * @param user 사용자
     * @param classRoom 반 정보
     * @return 신청 정보
     */
    Optional<ClassApplication> findByUserAndClassRoom(User user, ClassRoom classRoom);

    /**
     * 특정 반에 사용자가 신청했는지 확인
     * @param user 사용자
     * @param classRoom 반 정보
     * @return 신청 여부
     */
    boolean existsByUserAndClassRoom(User user, ClassRoom classRoom);

    /**
     * 반별 신청 목록 조회 (페이징) - N+1 문제 방지
     * @param classRoom 반 정보
     * @param status 신청 상태 (null이면 모든 상태)
     * @param pageable 페이징 정보
     * @return 신청 목록
     */
    @Query("SELECT ca FROM ClassApplication ca " +
           "LEFT JOIN FETCH ca.user " +
           "WHERE ca.classRoom = :classRoom " +
           "AND (:status IS NULL OR ca.status = :status) " +
           "ORDER BY ca.createdAt DESC")
    Page<ClassApplication> findByClassRoomAndStatusWithUser(
            @Param("classRoom") ClassRoom classRoom,
            @Param("status") ApplicationStatus status,
            Pageable pageable);

    /**
     * 반별 전체 신청 목록 조회 (통계용) - N+1 문제 방지  
     * @param classRoom 반 정보
     * @return 모든 신청 목록
     */
    @Query("SELECT ca FROM ClassApplication ca " +
           "LEFT JOIN FETCH ca.user " +
           "WHERE ca.classRoom = :classRoom " +
           "ORDER BY ca.createdAt DESC")
    List<ClassApplication> findByClassRoomWithUser(@Param("classRoom") ClassRoom classRoom);

    /**
     * 신청 ID로 신청 조회 (User, ClassRoom 포함) - N+1 문제 방지
     * @param applicationId 신청 ID
     * @return 신청 정보
     */
    @Query("SELECT ca FROM ClassApplication ca " +
           "LEFT JOIN FETCH ca.user " +
           "LEFT JOIN FETCH ca.classRoom cr " +
           "LEFT JOIN FETCH cr.teacher " +
           "WHERE ca.applicationId = :applicationId")
    Optional<ClassApplication> findByIdWithUserAndClassRoom(@Param("applicationId") Long applicationId);
}