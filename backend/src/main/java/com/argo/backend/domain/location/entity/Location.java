package com.argo.backend.domain.location.entity;

import com.argo.backend.domain.common.Coordinates;
import com.argo.backend.domain.spot.entity.Spot;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // DataLoader용 정적 팩토리 메서드
    public static Location create(String name, Coordinates coordinates) {
        Location location = new Location();
        location.name = name;
        location.coordinates = coordinates;
        return location;
    }
}
