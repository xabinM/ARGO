package com.argo.backend.domain.classroom;
import com.argo.backend.domain.CreatedAtEntity;
import com.argo.backend.domain.location.ClassLocation;
import com.argo.backend.domain.location.Location;
import com.argo.backend.domain.team.Team;
import com.argo.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    private List<ClassApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "classRoom", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<ClassLocation> classLocations = new ArrayList<>();

}
