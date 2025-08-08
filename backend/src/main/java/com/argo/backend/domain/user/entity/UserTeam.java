package com.argo.backend.domain.user.entity;

import com.argo.backend.domain.common.BaseTimeEntity;
import com.argo.backend.domain.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_teams", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "team_id"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTeam extends BaseTimeEntity {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userTeamId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime joinedAt;
    
    public static UserTeam create(User user, Team team) {
        return new UserTeam(user, team);
    }
    
    protected UserTeam(User user, Team team) {
        this.user = user;
        this.team = team;
        this.isActive = true;
        this.joinedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void activate() {
        this.isActive = true;
        this.joinedAt = LocalDateTime.now();
    }
}