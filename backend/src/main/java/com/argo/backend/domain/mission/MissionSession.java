package com.argo.backend.domain.mission;

import com.argo.backend.domain.CreatedAtEntity;
import com.argo.backend.domain.ploblem.Problem;
import com.argo.backend.domain.spot.Spot;
import com.argo.backend.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_sessions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MissionSession extends CreatedAtEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private Spot spot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MissionSessionStatus status = MissionSessionStatus.STARTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    private Boolean isSuccessful;

    public static MissionSession from(Spot spot, Team team, Problem problem) {
        return new MissionSession(spot, team, problem);
    }

    private MissionSession(Spot spot, Team team, Problem problem) {
        this.spot = spot;
        this.team = team;
        this.problem = problem;
    }
}

