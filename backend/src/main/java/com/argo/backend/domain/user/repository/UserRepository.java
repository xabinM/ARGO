package com.argo.backend.domain.user.repository;

import com.argo.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String userName);

    User findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE u.team.teamId = :teamId")
    List<User> findByTeamId(@Param("teamId") Long teamId);
}
