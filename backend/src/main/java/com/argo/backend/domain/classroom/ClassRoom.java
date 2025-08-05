package com.argo.backend.domain.classroom;

import com.argo.backend.domain.CreatedAtEntity;
import com.argo.backend.domain.location.Location;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.Teacher;
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
@AllArgsConstructor
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

    @Enumerated(EnumType.STRING)
    private ClassStatus status = ClassStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    private List<ClassApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    private List<Team> teams = new ArrayList<>();
}
