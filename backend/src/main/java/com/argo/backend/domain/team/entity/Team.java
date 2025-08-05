package com.argo.backend.domain.team.entity;

import com.argo.backend.domain.common.CreatedAtEntity;
import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.mission.entity.MissionSession;
import com.argo.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends CreatedAtEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    @Column(nullable = false, length = 100)
    private String teamName;

    @Column(nullable = false)
    private Integer maxMembers;

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    private List<MissionSession> missions = new ArrayList<>();

    public static Team from(ClassRoom classRoom, String teamName, Integer maxMembers) {
        return new Team(classRoom, teamName, maxMembers);
    }

    protected Team(ClassRoom classRoom, String teamName, Integer maxMembers) {
        this.classRoom = classRoom;
        this.teamName = teamName;
        this.maxMembers = maxMembers;
    }
}