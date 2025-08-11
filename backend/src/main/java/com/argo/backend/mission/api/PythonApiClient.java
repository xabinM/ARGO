package com.argo.backend.mission.api;

import com.argo.backend.mission.dto.selfieDetermine.MultipartInputStreamFileResource;
import com.argo.backend.mission.dto.selfieDetermine.SelfieRequestDto;
import com.argo.backend.mission.dto.selfieDetermine.SelfieResultDto;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateDto;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestToAI;
import com.argo.backend.mission.exception.problem.ProblemCountMismatchException;
import com.argo.backend.mission.exception.problem.ProblemGenerationFailedException;
import com.argo.backend.mission.exception.problem.PythonServerNoResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@Component
public class PythonApiClient {

    private final RestTemplate restTemplate;
    
    @Value("${python.api.base-url:http://localhost:8000}")
    private String pythonApiBaseUrl;

    public PythonApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 퀴즈 생성 요청
     */
    public ProblemGenerateDto requestProblem(String spotName, int grade, int problemCnt) {
        log.info(" 퀴즈 생성 요청: spotName={}, problemCnt={}", spotName, problemCnt);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // FastAPI 요청 형식
        ProblemGenerateRequestToAI request = new ProblemGenerateRequestToAI(spotName, grade, problemCnt); // grade 추가
        HttpEntity<ProblemGenerateRequestToAI> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<ProblemGenerateDto> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/generate-problem",
                    entity,
                    ProblemGenerateDto.class
            );

            ProblemGenerateDto body = response.getBody();
            if (body == null) {
                throw new PythonServerNoResponseException();
            }
            if (body.getProblems() == null) {
                throw new ProblemGenerationFailedException();
            }
            if (body.getProblems().size() != request.getProblemCnt()) {
                throw new ProblemCountMismatchException();
            }

            log.info("퀴즈 생성 성공: {}개 문제", body.getProblems().size());
            return body;
            
        } catch (Exception e) {
            log.error("Python API 호출 실패: {}", e.getMessage());
            throw new PythonApiException("Python API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 셀피 포즈 분석 요청 (기존 메서드 시그니처 유지)
     */
    public SelfieResultDto requestDeterMineSelfie(SelfieRequestDto request) throws IOException {
        log.info("포즈 분석 요청: pose={}", request.getPose());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(
                request.getMultipartFile().getInputStream(),
                request.getMultipartFile().getOriginalFilename()));
        body.add("pose_select", request.getPose().name().toLowerCase());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<SelfieResultDto> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/pose/predict",
                    requestEntity,
                    SelfieResultDto.class
            );

            log.info(" 포즈 분석 완료");
            return response.getBody();
            
        } catch (Exception e) {
            log.error(" 포즈 분석 실패: {}", e.getMessage());
            throw new PythonApiException("포즈 분석 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Python API 예외 클래스 (GlobalExceptionHandler용)
     */
    public static class PythonApiException extends RuntimeException {
        public PythonApiException(String message) {
            super(message);
        }
        
        public PythonApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}