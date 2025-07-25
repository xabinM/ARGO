package com.argo.backend.test.repository;

import com.argo.backend.domain.classroom.ClassRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {
    boolean existsByInviteCode(String inviteCode);

}
