package com.argo.backend.domain.team;

import com.argo.backend.domain.CreatedAtEntity;
import com.argo.backend.domain.classroom.ClassRoom;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends CreatedAtEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassRoom classRoom;

    @Column(nullable = false, length = 100)
    private String teamName;

    private Integer maxMembers;
}