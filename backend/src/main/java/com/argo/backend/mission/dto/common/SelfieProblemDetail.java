package com.argo.backend.mission.dto.common;

import com.argo.backend.domain.ploblem.entity.SelfieProblem;
import lombok.Getter;

@Getter
public class SelfieProblemDetail extends ProblemDetail {

    private final String guideline;
    private final String pose;
    private final String poseHint;

    public static SelfieProblemDetail from(SelfieProblem selfieProblem) {
        return new SelfieProblemDetail(
                selfieProblem.getId(),
                "SELFIE",
                selfieProblem.getGuideline(),
                selfieProblem.getPose().toString(),
                selfieProblem.getPoseHint()
        );
    }

    public SelfieProblemDetail(Long id, String dtype,
                               String guideline, String pose,
                               String poseHint) {
        super(id, dtype);
        this.guideline = guideline;
        this.pose = pose;
        this.poseHint = poseHint;
    }
}
