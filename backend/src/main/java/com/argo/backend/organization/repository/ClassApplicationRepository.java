package com.argo.backend.organization.repository;

import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassApplicationRepository extends JpaRepository<ClassApplication, Long> {
    
    boolean existsByUserAndClassRoom(User user, ClassRoom classRoom);
    
}
