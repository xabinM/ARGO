package com.argo.backend.domain.spot;

import com.argo.backend.domain.location.Coordinates;
import com.argo.backend.domain.location.Location;
import com.argo.backend.domain.mission.MissionSession;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Embedded
    private Coordinates coordinates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(mappedBy = "spot", fetch = FetchType.LAZY)
    private List<MissionSession> missions = new ArrayList<>();
}
