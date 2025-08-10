package com.argo.backend.mission.dto.common;

import com.argo.backend.domain.ploblem.entity.QuizProblem;
import lombok.Getter;

import java.util.List;

@Getter
public class QuizProblemDetail extends ProblemDetail {

    private final String question;
    private final List<String> choices;
    private final Integer correctIndex;
    private final String explanation;

    public static QuizProblemDetail from(QuizProblem quizProblem) {
        return new QuizProblemDetail(
                quizProblem.getId(),
                "QUIZ",
                quizProblem.getQuestion(),
                quizProblem.getChoices(),
                quizProblem.getCorrectIndex(),
                quizProblem.getExplanation()
        );
    }

    private QuizProblemDetail(Long id, String dtype, String question, List<String> choices,
                              Integer correctIndex, String explanation) {
        super(id, dtype);
        this.question = question;
        this.choices = choices;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }
}
