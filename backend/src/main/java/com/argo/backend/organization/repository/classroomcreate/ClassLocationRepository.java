package com.argo.backend.organization.repository.classroomcreate;

/**
 * 반-장소 연결(ClassLocation) 엔티티에 대한 데이터 접근을 담당하는 리포지토리 인터페이스
 * 반과 학습 장소 간의 연결 정보를 관리하는 데이터베이스 작업을 처리
 */

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassLocationRepository extends JpaRepository<ClassLocation, ClassLocation.ClassLocationId> {
}