package com.argo.backend.mission.service;

import com.argo.backend.domain.ploblem.Problem;
import com.argo.backend.domain.ploblem.QuizProblem;
import com.argo.backend.domain.spot.Spot;
import com.argo.backend.mission.api.PythonApiClient;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterRequest;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestFromCli;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateResponse;
import com.argo.backend.mission.dto.problemsRequest.ProblemResponseDto;
import com.argo.backend.mission.exception.SpotNotFoundException;
import com.argo.backend.mission.repository.ProblemRepository;
import com.argo.backend.mission.repository.SpotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private static final String PYTHON_API_URL = "http://localhost:5000/api/generate";

    private final ProblemRepository problemRepository;
    private final PythonApiClient pythonApiClient;
    private final SpotRepository spotRepository;

    @Transactional
    public void registerQuizProblem(Long spotId, ProblemRegisterRequest request) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(SpotNotFoundException::new);

        QuizProblem quiz = QuizProblem.from(
                spot,
                request.getQuestion(),
                request.getChoices(),
                request.getCorrectIndex(),
                request.getExplanation()
        );
        problemRepository.save(quiz);
    }

    public ProblemGenerateResponse generateProblem(ProblemGenerateRequestFromCli request) {
        Spot spot = spotRepository.findById(request.getSpotId())
                .orElseThrow(SpotNotFoundException::new);

        return pythonApiClient.requestProblem(spot.getName(), request.getProblemCnt());
    }

    public List<ProblemResponseDto> getProblemsBySpotId(Long spotId) {

        List<Problem> problems = problemRepository.findAllBySpotId(spotId);


        return ProblemResponseDto.from(problems);
    }
}
