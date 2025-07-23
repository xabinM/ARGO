package com.argo.backend.domain.location;

import com.argo.backend.domain.classroom.ClassRoom;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "class_locations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "location_id"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ClassLocation {
    @EmbeddedId
    private ClassLocationId id;

    @MapsId("classId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassRoom classRoom;

    @MapsId("locationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Embeddable
    @Getter @Setter @NoArgsConstructor
    public static class ClassLocationId implements   Serializable {
        private Long classId;
        private Long locationId;
    }
}