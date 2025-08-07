package com.argo.backend.mission.dto.common;

import com.argo.backend.domain.ploblem.entity.QuizProblem;
import lombok.Getter;

import java.util.List;

@Getter
public class QuizProblemResponseDto extends ProblemResponseDto {

    private final String question;
    private final List<String> choices;
    private final Integer correctIndex;
    private final String explanation;

    public static QuizProblemResponseDto from(QuizProblem quizProblem) {
        return new QuizProblemResponseDto(
                quizProblem.getId(),
                "QUIZ",
                quizProblem.getQuestion(),
                quizProblem.getChoices(),
                quizProblem.getCorrectIndex(),
                quizProblem.getExplanation()
        );
    }

    private QuizProblemResponseDto(Long id, String dtype, String question, List<String> choices,
                                   Integer correctIndex, String explanation) {
        super(id, dtype);
        this.question = question;
        this.choices = choices;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }
}
