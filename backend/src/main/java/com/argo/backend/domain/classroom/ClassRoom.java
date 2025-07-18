package com.argo.backend.domain.classroom;
import com.argo.backend.domain.CreatedAtEntity;
import com.argo.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassRoom extends CreatedAtEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Column(nullable = false)
    private String className;

    private String description;

    @Column(nullable = false)
    private LocalDate activityDate;

    private Integer maxStudents = 30;

    @Column(nullable = false, unique = true, length = 20)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.ACTIVE;
}
