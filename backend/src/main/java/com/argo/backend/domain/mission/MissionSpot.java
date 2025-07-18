package com.argo.backend.domain.mission;

import com.argo.backend.domain.location.Location;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_spots")
@Getter
@Setter
@NoArgsConstructor
public class MissionSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    private String landmark;

    private Integer radius = 30;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

