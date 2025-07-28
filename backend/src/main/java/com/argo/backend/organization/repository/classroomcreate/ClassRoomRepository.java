package com.argo.backend.organization.repository.classroomcreate;

/**
 * 반(ClassRoom) 엔티티에 대한 데이터 접근을 담당하는 리포지토리 인터페이스
 * 반 정보 조회, 저장, 초대 코드 중복 검사 등의 데이터베이스 작업을 처리
 */

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.classroom.ClassStatus;
import com.argo.backend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    boolean existsByInviteCode(String inviteCode);

    /**
     * 초대 코드로 반 조회 - 신청용 (연관 데이터 포함)
     */
    @Query("SELECT DISTINCT c FROM ClassRoom c " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH c.classLocations cl " +
           "LEFT JOIN FETCH cl.location " +
           "WHERE c.inviteCode = :inviteCode")
    Optional<ClassRoom> findByInviteCodeWithDetails(@Param("inviteCode") String inviteCode);
    
    /**
     * 반 기본 정보 조회용 - 기본적인 연관 데이터만 로드
     */
    @Query("SELECT c FROM ClassRoom c " +
           "LEFT JOIN FETCH c.teacher " +
           "WHERE c.classId = :classId")
    Optional<ClassRoom> findByIdWithBasicDetails(@Param("classId") Long classId);
    
    /**
     * 반 상세 조회용 - applications만 로드 (MultipleBagFetchException 방지)
     */
    @Query("SELECT c FROM ClassRoom c " +
           "LEFT JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH c.applications app " +
           "LEFT JOIN FETCH app.user u " +
           "LEFT JOIN FETCH u.team " +
           "WHERE c.classId = :classId")
    Optional<ClassRoom> findByIdWithApplications(@Param("classId") Long classId);
    
    /**
     * 반 상세 조회용 - teams만 별도 로드 (MultipleBagFetchException 방지)
     */
    @Query("SELECT c FROM ClassRoom c " +
           "LEFT JOIN FETCH c.teams " +
           "WHERE c.classId = :classId")
    Optional<ClassRoom> findByIdWithTeams(@Param("classId") Long classId);
    
    /**
     * 반 상세 조회용 - classLocations만 별도 로드
     */
    @Query("SELECT c FROM ClassRoom c " +
           "LEFT JOIN FETCH c.classLocations cl " +
           "LEFT JOIN FETCH cl.location " +
           "WHERE c.classId = :classId")
    Optional<ClassRoom> findByIdWithLocations(@Param("classId") Long classId);

    /**
     * 선생님이 생성한 클래스를 연관 데이터와 함께 조회 (JOIN FETCH) - classStatus가 active나 inactive에 대해서 처리
     * @param teacher 선생님 사용자
     * @param status 클래스 상태 (null인 경우 모든 상태)
     * @param pageable 페이지네이션 정보
     * @return 연관 데이터가 로드된 클래스 목록
     */
    @Query("SELECT c FROM ClassRoom c " +
           "LEFT JOIN FETCH c.applications app " +
           "WHERE c.teacher = :teacher " +
           "AND (:status IS NULL OR c.status = :status) " +
           "ORDER BY c.createdAt DESC")
    Page<ClassRoom> findByTeacherAndStatusWithDetails(@Param("teacher") User teacher, 
                                                    @Param("status") ClassStatus status, 
                                                    Pageable pageable);

    /**
     * 학생이 신청한 클래스를 연관 데이터와 함께 조회 (JOIN FETCH) - 특정 classStatus가 active나 inactive에 대해서 처리
     * @param student 학생 사용자
     * @param classStatus 클래스 상태 (null인 경우 모든 상태)
     * @param pageable 페이지네이션 정보
     * @return 연관 데이터가 로드된 클래스 목록
     */
    @Query("SELECT ca.classRoom FROM ClassApplication ca " +
           "LEFT JOIN FETCH ca.classRoom.applications app " + //신청서 fetch join
           "WHERE ca.user = :student " +
           "AND (:classStatus IS NULL OR ca.classRoom.status = :classStatus) " +
            "AND ca.status = 'APPROVED' " +
            "ORDER BY ca.createdAt DESC")
    Page<ClassRoom> findByStudentAndStatusWithDetails(@Param("student") User student,
                                                    @Param("classStatus") ClassStatus classStatus,
                                                    Pageable pageable);

}
