package com.argo.backend.mission.dto.problemRegister;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProblemRegisterRequest {

    @NotBlank
    private Integer grade;

    @NotBlank
    private String question;

    @NotEmpty
    private List<String> choices;

    @NotNull
    private Integer correctIndex;

    private String explanation;
}
