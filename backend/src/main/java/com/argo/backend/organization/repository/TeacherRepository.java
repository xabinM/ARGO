package com.argo.backend.organization.repository;

import com.argo.backend.domain.user.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

}
