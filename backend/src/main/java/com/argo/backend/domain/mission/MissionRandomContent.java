package com.argo.backend.domain.mission;

import com.argo.backend.domain.CreatedAtEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "mission_random_contents")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionRandomContent extends CreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", unique = true)
    private MissionSpot spot;

    @Lob
    private String contentText;
}