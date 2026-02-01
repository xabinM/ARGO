package com.argo.backend.mission.service;

import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.ploblem.enums.ProblemType;
import com.argo.backend.domain.ploblem.entity.QuizProblem;
import com.argo.backend.domain.ploblem.entity.SelfieProblem;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.domain.team.entity.Team;
import com.argo.backend.domain.team.repository.TeamRepository;
import com.argo.backend.domain.user.entity.UserTeam;
import com.argo.backend.domain.user.repository.UserTeamRepository;
import com.argo.backend.mission.api.PythonApiClient;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterRequest;
import com.argo.backend.mission.dto.common.ProblemDetail;
import com.argo.backend.mission.dto.common.QuizProblemDetail;
import com.argo.backend.mission.dto.common.SelfieProblemDetail;
import com.argo.backend.mission.dto.selfieDetermine.SelfieRequestDto;
import com.argo.backend.mission.dto.selfieDetermine.SelfieResultDto;
import com.argo.backend.mission.exception.SpotNotFoundException;
import com.argo.backend.domain.ploblem.repository.ProblemRepository;
import com.argo.backend.domain.spot.repository.SpotRepository;
import com.argo.backend.mission.exception.TeamNotFoundException;
import com.argo.backend.mission.exception.problem.PythonServerNoResponseException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final PythonApiClient pythonApiClient;
    private final SpotRepository spotRepository;
    private final UserTeamRepository userTeamRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public void registerQuizProblem(Long spotId, ProblemRegisterRequest request) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(SpotNotFoundException::new);

        QuizProblem quiz = QuizProblem.from(
                spot,
                request.getGrade(),
                request.getQuestion(),
                request.getChoices(),
                request.getCorrectIndex(),
                request.getExplanation()
        );
        problemRepository.save(quiz);
    }

    @Transactional
    public List<QuizProblem> saveGeneratedProblems(List<QuizProblem> quizProblems) {
        return problemRepository.saveAll(quizProblems);
    }

    public Map<String, Object> testQuizGeneration(String spotName, int grade, int problemCnt) {
        try {
            Map<String, Object> quizResult;
            try {
                quizResult = pythonApiClient.requestProblemAsMapWithApache(spotName, grade, problemCnt);
            } catch (PythonApiClient.PythonApiException e) {
                quizResult = pythonApiClient.requestProblemAsMap(spotName, grade, problemCnt);
            }
            return quizResult;
        } catch (PythonApiClient.PythonApiException e) {
            throw new PythonServerNoResponseException();
        }
    }

    public List<ProblemDetail> getProblemsBySpotId(Long spotId) {
        List<Problem> problems = problemRepository.findAllBySpotId(spotId);
        return ProblemDetail.from(problems);
    }

    public List<ProblemDetail> findProblemsBySpotIdAndType(Long spotId, ProblemType type) {

        List<Problem> allProblems = problemRepository.findAllBySpotId(spotId);

        return switch (type) {
            case QUIZ -> allProblems.stream()
                    .filter(p -> p instanceof QuizProblem)
                    .map(p -> QuizProblemDetail.from((QuizProblem) p))
                    .collect(Collectors.toList());

            case SELFIE -> allProblems.stream()
                    .filter(p -> p instanceof SelfieProblem)
                    .map(p -> SelfieProblemDetail.from((SelfieProblem) p))
                    .collect(Collectors.toList());
        };
    }

    public SelfieResultDto determineSelfie(SelfieRequestDto request) throws IOException {
        Team team = teamRepository.findById(request.getTeamId()).orElseThrow(TeamNotFoundException::new);

        List<UserTeam> userTeams = userTeamRepository.findAllByTeam(team);

        Integer teamMemberCnt = userTeams.size();

        return pythonApiClient.requestDeterMineSelfie(teamMemberCnt, request.getMultipartFile(), request.getPose());
    }
}
