package com.argo.backend.domain.user;

import com.argo.backend.auth.dto.signup.SignupRequest;
import com.argo.backend.auth.exception.WrongPasswordException;
import com.argo.backend.domain.BaseTimeEntity;
import com.argo.backend.domain.team.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    public static User from(String username, String encodedPassword, String name, Role role) {
        return new User(
                username,
                encodedPassword,
                name,
                role,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    private User(String username, String password, String name, Role role,
                 BigDecimal latitude, BigDecimal longitude) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.latitude = latitude;
        this.longitude = longitude;
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
}
