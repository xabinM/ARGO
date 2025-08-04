package com.argo.backend.domain.ploblem;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("QUIZ")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@PrimaryKeyJoinColumn(name = "id")
public class QuizProblem extends Problem {

    @Lob
    @Column(nullable = false)
    private String question;

    @ElementCollection
    @CollectionTable(name = "quiz_problem_choices",
            joinColumns = @JoinColumn(name = "quiz_problem_id"))
    @Column(name = "choice", nullable = false)
    private List<String> choices = new ArrayList<>();

    @Column(nullable = false)
    private Integer correctIndex;

    @Lob
    private String explanation;

    public static QuizProblem from(String question, List<String> choices, Integer correctIndex, String explanation) {
        return new QuizProblem(question, choices, correctIndex, explanation);
    }

    private QuizProblem(String question, List<String> choices, Integer correctIndex, String explanation) {
        this.question = question;
        this.choices = choices;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }
}
