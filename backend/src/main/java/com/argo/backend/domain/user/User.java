package com.argo.backend.domain.user;

import com.argo.backend.domain.BaseTimeEntity;
import com.argo.backend.domain.classroom.ClassApplication;
import com.argo.backend.domain.team.Team;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

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
}
