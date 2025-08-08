package com.argo.backend.domain.classroom.entity;

import com.argo.backend.domain.common.CreatedAtEntity;
import com.argo.backend.domain.classroom.enums.ClassStatus;
import com.argo.backend.domain.location.entity.Location;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.user.entity.Teacher;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassRoom extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false)
    private String className;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Column(nullable = false)
    private Integer maxStudents;

    @Column(nullable = false)
    private Integer grade;

    @Column(nullable = false, unique = true, length = 20)
    private String inviteCode;

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    private List<ClassApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    private List<Team> teams = new ArrayList<>();

    protected ClassRoom(Teacher teacher, String className, String description, LocalDate activityDate,String inviteCode, Integer maxStudents, Location location, Integer grade) {
        this.teacher = teacher;
        this.className = className;
        this.description = description;
        this.activityDate = activityDate;
        this.inviteCode = inviteCode;
        this.maxStudents = maxStudents;
        this.location = location;
        this.grade = grade;

    }

    public static ClassRoom from(Teacher teacher, String className, String description, LocalDate activityDate,String inviteCode, Integer maxStudents, Location location, Integer grade) {
        return new ClassRoom(
                teacher,
                className,
                description,
                activityDate,
                inviteCode,
                maxStudents,
                location,
                grade
        );
    }

}
