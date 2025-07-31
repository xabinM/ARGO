package com.argo.backend.domain.mission;

import com.argo.backend.domain.CreatedAtEntity;
import com.argo.backend.domain.ploblem.ProblemType;
import com.argo.backend.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_sessions")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MissionSession extends CreatedAtEntity {
    
    @Id
    @Column(length = 100)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private MissionSpot spot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionSessionStatus status = MissionSessionStatus.STARTED;

    @Enumerated(EnumType.STRING)
    private ProblemType resultType;

    @Lob
    private String resultData;

    private Boolean isSuccessful;

    private Integer rewardEarned;
}

