package com.argo.backend.domain.user.repository;

import com.argo.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String userName);

    User findByUsername(String username);
    
    // 특정 팀에 속한 유저들 조회는 UserTeamRepository나 Team 헬퍼 메서드 사용
}
