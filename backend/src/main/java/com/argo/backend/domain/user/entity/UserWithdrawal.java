package com.argo.backend.domain.user.entity;

import com.argo.backend.domain.common.CreatedAtEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_withdrawals")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWithdrawal extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long withdrawalId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean passwordVerified = true;

    private UserWithdrawal(User user) {
        this.user = user;
    }

    public static UserWithdrawal from(User user) {
        return new UserWithdrawal(user);
    }
}