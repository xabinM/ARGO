package com.argo.backend.domain.ploblem;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("SELFIE")
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "id")
public class SelfieProblem extends Problem{

    @Lob
    @Column(nullable = false)
    private String guideline;                // 가이드(예: "배경에 학교 로고 포함")

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PhotoPose pose;

    @Column(nullable = false)
    private String poseHint;                 // 포즈 힌트(옵션)

}
