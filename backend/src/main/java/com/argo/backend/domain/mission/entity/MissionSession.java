package com.argo.backend.domain.mission.entity;

import com.argo.backend.domain.common.CreatedAtEntity;
import com.argo.backend.domain.mission.enums.MissionSessionStatus;
import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_sessions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @JoinColumn(name = "problem_id")    // 미션세션이 같은 문제를 공유할 수 있어야함.
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

    public boolean isStatusStarted() {
        return this.status == MissionSessionStatus.STARTED;
    }

    public void alterSuccessfulTrue() {
        this.isSuccessful = true;
    }

    public void alterSuccessfulFalse() {
        this.isSuccessful = false;
    }

    public void alterMissionStatus() {
        this.status = MissionSessionStatus.COMPLETED;
    }
}

