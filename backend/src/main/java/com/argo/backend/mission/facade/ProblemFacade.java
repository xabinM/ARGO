package com.argo.backend.mission.facade;

import com.argo.backend.domain.ploblem.entity.QuizProblem;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.spot.repository.SpotRepository;
import com.argo.backend.mission.api.PythonApiClient;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateDto;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestFromCli;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateTransDto;
import com.argo.backend.mission.exception.SpotNotFoundException;
import com.argo.backend.mission.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProblemFacade {

    private final ProblemService problemService;
    private final PythonApiClient pythonApiClient;
    private final SpotRepository spotRepository;


    public ProblemGenerateTransDto generateProblem(ProblemGenerateRequestFromCli request) {

        Spot spot = spotRepository.findById(request.getSpotId())
                .orElseThrow(SpotNotFoundException::new);

        Map<String, Object> pythonResponse;
        try {
            pythonResponse = pythonApiClient.requestProblemAsMapWithApache(
                    spot.getName(),
                    request.getGrade(),
                    request.getProblemCnt()
            );
        } catch (Exception e) {
            pythonResponse = pythonApiClient.requestProblemAsMap(
                    spot.getName(),
                    request.getGrade(),
                    request.getProblemCnt()
            );
        }

        List<QuizProblem> quizProblems = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> problemsData = (List<Map<String, Object>>) pythonResponse.get("problems");

        for (Map<String, Object> problemData : problemsData) {
            QuizProblem quizProblem = QuizProblem.from(
                    spot,
                    (Integer) problemData.get("grade"),
                    (String) problemData.get("question"),
                    (List<String>) problemData.get("choices"),
                    (Integer) problemData.get("correctIndex"),
                    (String) problemData.get("explanation")
            );
            quizProblems.add(quizProblem);
        }

        List<QuizProblem> savedProblems = problemService.saveGeneratedProblems(quizProblems);

        ProblemGenerateDto dto = new ProblemGenerateDto(savedProblems);
        return new ProblemGenerateTransDto(request.getGrade(), spot.getName(), dto);
    }
}
