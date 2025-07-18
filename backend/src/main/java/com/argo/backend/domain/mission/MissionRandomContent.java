package com.argo.backend.domain.mission;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mission_random_contents")
@Getter
@Setter
@NoArgsConstructor
public class MissionRandomContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", unique = true)
    private MissionSpot spot;

    @Lob
    private String contentText;

    @CreationTimestamp
    private LocalDateTime createdAt;
}