package com.argo.backend.domain.user.entity;

import com.argo.backend.domain.common.BaseTimeEntity;
import com.argo.backend.domain.classroom.entity.ClassApplication;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.user.enums.Role;
import com.argo.backend.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTeam> userTeams = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<ClassApplication> applications = new ArrayList<>();

    public static User from(String username, String encodedPassword, String name, Role role) {
        return new User(
                username,
                encodedPassword,
                name,
                role
        );
    }

    protected User(String username, String password, String name, Role role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public boolean isPasswordMatching(PasswordEncoder encoder, String rawPassword) {
        return encoder.matches(rawPassword, this.password);
    }

    public void updateStatusByWithdraw() {
        this.status = UserStatus.INACTIVE;
    }

    public boolean checkStatus() {
        return this.status == UserStatus.ACTIVE;
    }

    // 특정 클래스에서의 활성 팀 조회 헬퍼 메서드 (성능 최적화를 위해 Repository 위임 권장)
    // 이 클래스에 이 팀이 있는지
    public Team getActiveTeamByClass(Long classId) {
        // 성능상 이슈가 있을 수 있으므로 가능하면 UserTeamRepository.findActiveByUserIdAndClassId 사용 권장
        return userTeams.stream()
                .filter(ut -> ut.getIsActive())
                .filter(ut -> ut.getTeam().getClassRoom().getClassId().equals(classId))
                .map(UserTeam::getTeam)
                .findFirst()
                .orElse(null);
    }
    
    // 특정 클래스에서 팀에 배정되어 있는지 확인 헬퍼 메서드
    public boolean isInTeamForClass(Long classId) {
        return userTeams.stream()
                .anyMatch(ut -> ut.getIsActive() && 
                               ut.getTeam().getClassRoom().getClassId().equals(classId));
    }
}
