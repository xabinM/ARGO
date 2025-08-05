package com.argo.backend.mission.service;

import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.ploblem.enums.ProblemType;
import com.argo.backend.domain.ploblem.entity.QuizProblem;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.mission.api.PythonApiClient;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterRequest;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestFromCli;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateResponse;
import com.argo.backend.mission.dto.common.ProblemResponseDto;
import com.argo.backend.mission.dto.common.QuizProblemResponseDto;
import com.argo.backend.mission.dto.common.SelfieProblemResponseDto;
import com.argo.backend.mission.exception.SpotNotFoundException;
import com.argo.backend.domain.ploblem.repository.ProblemRepository;
import com.argo.backend.domain.ploblem.repository.QuizProblemRepository;
import com.argo.backend.domain.ploblem.repository.SelfieProblemRepository;
import com.argo.backend.domain.spot.repository.SpotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private static final String PYTHON_API_URL = "http://localhost:5000/api/generate";

    private final ProblemRepository problemRepository;
    private final PythonApiClient pythonApiClient;
    private final SpotRepository spotRepository;
    private final QuizProblemRepository quizProblemRepository;
    private final SelfieProblemRepository selfieProblemRepository;

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

    public List<ProblemResponseDto> findProblemsBySpotIdAndType(Long spotId, ProblemType type) {
        return switch (type) {
            case QUIZ -> quizProblemRepository.findBySpotId(spotId).stream()
                    .map(QuizProblemResponseDto::from)
                    .collect(Collectors.toList());

            case SELFIE -> selfieProblemRepository.findBySpotId(spotId).stream()
                    .map(SelfieProblemResponseDto::from)
                    .collect(Collectors.toList());
        };
    }
}
