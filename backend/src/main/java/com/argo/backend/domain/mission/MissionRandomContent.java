package com.argo.backend.domain.mission;

import com.argo.backend.domain.CreatedAtEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_random_contents")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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