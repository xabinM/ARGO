package com.argo.backend.domain.ploblem;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class QuizProblem extends Problem {

    @Lob
    @Column(nullable = false)
    private String question;                 // 지문

    @ElementCollection
    @CollectionTable(name = "quiz_problem_choices",
            joinColumns = @JoinColumn(name = "quiz_problem_id"))
    @Column(name = "choice", nullable = false)
    private List<String> choices = new ArrayList<>(); // 보기(순서)

    @Column(nullable = false)
    private Integer correctIndex;            // 정답 인덱스(0-based)

    @Lob
    private String explanation;              // 해설(옵션)

    @Override
    protected ProblemType declaredType() {
        return ProblemType.QUIZ;
    }
}
