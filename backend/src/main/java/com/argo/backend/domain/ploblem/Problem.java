package com.argo.backend.domain.ploblem;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "problems")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProblemType type;

    // 각 하위 클래스에서 자신이 선언한 타입을 반환
    protected abstract ProblemType declaredType();

    // 타입 정합성 보장: null이면 자동 세팅, 값이 있으면 일치 검증
    @PrePersist @PreUpdate
    private void ensureType() {
        ProblemType declared = declaredType();
        if (this.type == null) {
            this.type = declared;
        } else if (this.type != declared) {
            throw new IllegalStateException(
                    "Problem.type(" + this.type + ") must be " + declared + " for " + getClass().getSimpleName()
            );
        }
    }
}
