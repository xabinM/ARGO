package com.argo.backend.mission.service;

import com.argo.backend.domain.ploblem.entity.Problem;
import com.argo.backend.domain.ploblem.enums.ProblemType;
import com.argo.backend.domain.ploblem.entity.QuizProblem;
import com.argo.backend.domain.ploblem.entity.SelfieProblem;
import com.argo.backend.domain.spot.entity.Spot;
import com.argo.backend.mission.api.PythonApiClient;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateTransDto;
import com.argo.backend.mission.dto.problemRegister.ProblemRegisterRequest;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestFromCli;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateDto;
import com.argo.backend.mission.dto.common.ProblemDetail;
import com.argo.backend.mission.dto.common.QuizProblemDetail;
import com.argo.backend.mission.dto.common.SelfieProblemDetail;
import com.argo.backend.mission.dto.selfieDetermine.SelfieRequestDto;
import com.argo.backend.mission.dto.selfieDetermine.SelfieResultDto;
import com.argo.backend.mission.exception.SpotNotFoundException;
import com.argo.backend.domain.ploblem.repository.ProblemRepository;
import com.argo.backend.domain.ploblem.repository.QuizProblemRepository;
import com.argo.backend.domain.ploblem.repository.SelfieProblemRepository;
import com.argo.backend.domain.spot.repository.SpotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
                request.getGrade(),
                request.getQuestion(),
                request.getChoices(),
                request.getCorrectIndex(),
                request.getExplanation()
        );
        problemRepository.save(quiz);
    }

    /**
     * 🔥 Apache HttpClient로 퀴즈 생성 (메인 메서드)
     */
    public ProblemGenerateTransDto generateProblem(ProblemGenerateRequestFromCli request) {
        Spot spot = spotRepository.findById(request.getSpotId())
                .orElseThrow(SpotNotFoundException::new);

        log.info("🎯 퀴즈 생성 요청: spotId={}, spotName={}, grade={}, count={}", 
                request.getSpotId(), spot.getName(), request.getGrade(), request.getProblemCnt());

        // 🔥 Apache HttpClient 방식으로 변경 (422 에러 해결)
        Map<String, Object> pythonResponse;
        try {
            pythonResponse = pythonApiClient.requestProblemAsMapWithApache(
                    spot.getName(),
                    request.getGrade(),
                    request.getProblemCnt()
            );
        } catch (Exception e) {
            log.warn("🔄 Apache 방식 실패, RestTemplate으로 재시도: {}", e.getMessage());
            // 백업: RestTemplate 방식
            pythonResponse = pythonApiClient.requestProblemAsMap(
                    spot.getName(),
                    request.getGrade(),
                    request.getProblemCnt()
            );
        }

        // Map을 QuizProblem으로 변환
        List<QuizProblem> quizProblems = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> problemsData = (List<Map<String, Object>>) pythonResponse.get("problems");

        for (Map<String, Object> problemData : problemsData) {
            QuizProblem quizProblem = QuizProblem.from(
                    spot,  // 핵심: spot 정보 추가
                    (Integer) problemData.get("grade"),
                    (String) problemData.get("question"),
                    (List<String>) problemData.get("choices"),
                    (Integer) problemData.get("correctIndex"),
                    (String) problemData.get("explanation")
            );
            quizProblems.add(quizProblem);
        }

        log.info("✅ 퀴즈 생성 완료: {}개 문제 변환됨", quizProblems.size());

        ProblemGenerateDto dto = new ProblemGenerateDto(quizProblems);
        return new ProblemGenerateTransDto(request.getGrade(), spot.getName(), dto);
    }

    /**
     * 🧪 Apache HttpClient로 퀴즈 테스트 메서드
     */
    public Map<String, Object> testQuizGeneration(String spotName, int grade, int problemCnt) {
        try {
            log.info("🧪 Apache 퀴즈 생성 테스트: spotName={}, grade={}, count={}", spotName, grade, problemCnt);
            
            // 🔥 Apache HttpClient 방식 먼저 시도
            Map<String, Object> quizResult;
            try {
                quizResult = pythonApiClient.requestProblemAsMapWithApache(spotName, grade, problemCnt);
                log.info("✅ Apache 방식 성공");
            } catch (Exception e) {
                log.warn("🔄 Apache 실패, RestTemplate 재시도: {}", e.getMessage());
                quizResult = pythonApiClient.requestProblemAsMap(spotName, grade, problemCnt);
                log.info("✅ RestTemplate 방식 성공");
            }
            
            // problems 필드 확인
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> problems = (List<Map<String, Object>>) quizResult.get("problems");
            
            log.info("📝 생성된 문제 수: {}", problems.size());
            
            for (int i = 0; i < problems.size(); i++) {
                Map<String, Object> problem = problems.get(i);
                log.info("문제 {}: {}", i+1, problem.get("question"));
                log.info("정답: {}번", problem.get("correctIndex"));
            }
            
            return quizResult;
            
        } catch (Exception e) {
            log.error("❌ 퀴즈 생성 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 🧪 Apache HttpClient 직접 테스트 (DB 없이)
     */
    public Map<String, Object> testQuizGenerationDirect(String spotName, int grade, int problemCnt) {
        try {
            log.info("🧪 Apache 직접 테스트: spotName={}, grade={}, count={}", spotName, grade, problemCnt);
            
            // Apache HttpClient 직접 호출 (DB 우회)
            Map<String, Object> result = pythonApiClient.requestProblemAsMapWithApache(
                spotName, grade, problemCnt
            );
            
            log.info("✅ 직접 테스트 성공");
            return result;
            
        } catch (Exception e) {
            log.error("❌ 직접 테스트 실패: {}", e.getMessage());
            throw e;
        }
    }

    public List<ProblemDetail> getProblemsBySpotId(Long spotId) {
        List<Problem> problems = problemRepository.findAllBySpotId(spotId);
        return ProblemDetail.from(problems);
    }

    // 🔥 핵심 수정: Repository 문제 해결을 위한 대안 방법
    public List<ProblemDetail> findProblemsBySpotIdAndType(Long spotId, ProblemType type) {
        
        // 🔥 기존에 잘 작동하는 ProblemRepository 사용
        List<Problem> allProblems = problemRepository.findAllBySpotId(spotId);
        
        return switch (type) {
            case QUIZ -> allProblems.stream()
                    .filter(p -> p instanceof QuizProblem)  // QuizProblem만 필터링
                    .map(p -> QuizProblemDetail.from((QuizProblem) p))
                    .collect(Collectors.toList());

            case SELFIE -> allProblems.stream()
                    .filter(p -> p instanceof SelfieProblem)  // SelfieProblem만 필터링
                    .map(p -> SelfieProblemDetail.from((SelfieProblem) p))
                    .collect(Collectors.toList());
        };
    }

    public SelfieResultDto determineSelfie(SelfieRequestDto request) throws IOException {
        log.info("🎯 포즈 분석 시작");
        return pythonApiClient.requestDeterMineSelfie(request);
    }
}