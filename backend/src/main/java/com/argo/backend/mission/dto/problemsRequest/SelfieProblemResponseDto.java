package com.argo.backend.mission.dto.problemsRequest;

import com.argo.backend.domain.ploblem.entity.SelfieProblem;
import lombok.Getter;

@Getter
public class SelfieProblemResponseDto extends ProblemResponseDto {

    private final String guideline;
    private final String pose;
    private final String poseHint;

    public static SelfieProblemResponseDto from(SelfieProblem selfieProblem) {
        return new SelfieProblemResponseDto(
                selfieProblem.getId(),
                "SELFIE",
                selfieProblem.getGuideline(),
                selfieProblem.getPose().toString(),
                selfieProblem.getPoseHint()
        );
    }

    public SelfieProblemResponseDto(Long id, String dtype,
                                    String guideline, String pose,
                                    String poseHint) {
        super(id, dtype);
        this.guideline = guideline;
        this.pose = pose;
        this.poseHint = poseHint;
    }
}
