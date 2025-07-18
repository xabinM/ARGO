package com.argo.backend.domain.location;

import com.argo.backend.domain.classroom.ClassRoom;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "class_locations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "location_id"}))
@Getter
@Setter
@NoArgsConstructor
// location과 class의 중간 테이블
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
    public static class ClassLocationId implements Serializable {
        private Long classId;
        private Long locationId;
    }
}