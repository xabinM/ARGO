package com.argo.backend.domain.user.repository;

import com.argo.backend.domain.user.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

}
