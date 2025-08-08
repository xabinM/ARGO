package com.argo.backend.domain.classroom.entity;

import com.argo.backend.domain.common.BaseTimeEntity;
import com.argo.backend.domain.classroom.enums.ApplicationStatus;
import com.argo.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "class_applications")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassApplication extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassRoom classRoom;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    private LocalDateTime processedAt;

    public static ClassApplication from(User user, ClassRoom classRoom) {
        return new ClassApplication(
                user,
                classRoom
        );
    }

    protected ClassApplication(User user, ClassRoom classRoom) {
        this.user = user;
        this.classRoom = classRoom;
    }
}
