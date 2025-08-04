package com.argo.backend.domain.ploblem;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "problems")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
@Getter @Setter @AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
