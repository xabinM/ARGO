package com.argo.backend.domain.ploblem.entity;

import com.argo.backend.domain.spot.entity.Spot;
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

    @Column(nullable = false)
    private Integer grade;

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

    public static QuizProblem from(Spot spot, Integer grade, String question,
                                   List<String> choices,
                                   Integer correctIndex,
                                   String explanation) {
        return new QuizProblem(spot, grade, question, choices, correctIndex, explanation);
    }
    
    // DataLoader용 - ID를 직접 설정할 수 있는 팩토리 메서드
    public static QuizProblem fromWithId(Long id, Spot spot, Integer grade, String question,
                                        List<String> choices,
                                        Integer correctIndex,
                                        String explanation) {
        QuizProblem problem = new QuizProblem(spot, grade, question, choices, correctIndex, explanation);
        problem.setId(id);  // 부모 클래스의 ID 설정
        return problem;
    }

    private QuizProblem(Spot spot, Integer grade, String question,
                        List<String> choices,
                        Integer correctIndex,
                        String explanation) {
        super(spot);
        this.grade = grade;
        this.question = question;
        this.choices = choices;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }
}
