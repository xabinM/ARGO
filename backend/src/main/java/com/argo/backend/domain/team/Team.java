package com.argo.backend.domain.team;

import com.argo.backend.domain.CreatedAtEntity;
import com.argo.backend.domain.classroom.ClassRoom;
import com.argo.backend.domain.mission.MissionSession;
import com.argo.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Team extends CreatedAtEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    @Column(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassRoom classRoom;

    @Column(nullable = false, length = 100)
    private String teamName;

    @Column(nullable = false)
    private Integer maxMembers;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<MissionSession> missions = new ArrayList<>();
}