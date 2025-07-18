package com.argo.backend.domain.mission;

import com.argo.backend.domain.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mission_sessions")
@Getter
@Setter
@NoArgsConstructor
// team과 missionspot의 중간 테이블
public class MissionSession {
    
    @Id
    @Column(length = 100)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private MissionSpot spot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private LocalDateTime startedAt;

    @Enumerated(EnumType.STRING)
    private MissionSessionStatus status;

    @Enumerated(EnumType.STRING)
    private ResultType resultType;

    @Lob
    private String resultData;

    private Boolean isSuccessful;

    private Integer rewardEarned;
}

