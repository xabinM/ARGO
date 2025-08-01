package com.argo.backend.domain.user;

import com.argo.backend.domain.classroom.ClassRoom;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Teacher extends User {

    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<ClassRoom> classes = new ArrayList<>();

    public static Teacher from(String username, String encodedPassword, String name) {
        return new Teacher(
                username,
                encodedPassword,
                name
        );
    }

    private Teacher(String username, String encodedPassword, String name) {
        super(username, encodedPassword, name, Role.ROLE_TEACHER);
    }


    // 생성 시 role 일관성 보장
    @PrePersist @PreUpdate
    private void ensureRole() {
        if (getRole() == null) {
            setRole(Role.ROLE_TEACHER);
        } else if (getRole() != Role.ROLE_TEACHER) {
            throw new IllegalStateException("Teacher.role must be ROLE_TEACHER");
        }
    }

    // 편의 메서드 (양방향 연관관계 동기화)
    public void addClassRoom(ClassRoom c) {
        this.classes.add(c);
        c.setTeacher(this);
    }
}
