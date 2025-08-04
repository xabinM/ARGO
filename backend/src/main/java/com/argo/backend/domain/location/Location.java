package com.argo.backend.domain.location;

import com.argo.backend.domain.spot.Spot;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Coordinates coordinates;

    @OneToMany(mappedBy = "location", fetch = FetchType.LAZY)
    private List<Spot> spots = new ArrayList<>();

}
