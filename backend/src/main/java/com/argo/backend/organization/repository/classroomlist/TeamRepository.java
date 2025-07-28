package com.argo.backend.organization.repository.classroomlist;

/**
 * 팀(Team) 엔티티에 대한 데이터 접근을 담당하는 리포지토리 인터페이스
 * 팀 정보 조회, 특정 반의 팀 수 계산 등의 데이터베이스 작업을 처리
 */

import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    /**
     * 특정 반에 팀명이 존재하는지 확인
     * @param classRoom 반 정보
     * @param teamName 팀명
     * @return 존재 여부
     */
    boolean existsByClassRoomAndTeamName(ClassRoom classRoom, String teamName);

    /**
     * 팀 ID와 반 ID로 팀 조회 (N+1 문제 방지)
     * @param teamId 팀 ID
     * @param classRoom 반 정보
     * @return 팀 정보
     */
    @Query("SELECT t FROM Team t " +
           "LEFT JOIN FETCH t.classRoom " +
           "WHERE t.teamId = :teamId AND t.classRoom = :classRoom")
    Optional<Team> findByIdAndClassRoom(@Param("teamId") Long teamId, @Param("classRoom") ClassRoom classRoom);

}