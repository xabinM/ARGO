package com.argo.backend.test.repository;

/**
 * 장소(Location) 엔티티에 대한 데이터 접근을 담당하는 리포지토리 인터페이스
 * 학습 장소 정보 조회, 이름으로 검색 등의 데이터베이스 작업을 처리
 */

import com.argo.backend.domain.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByName(String locationName);
}