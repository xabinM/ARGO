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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
     * 퀴즈 생성 요청 - Map 방식으로 수정
     */
    public Map<String, Object> requestProblemAsMap(String spotName, int grade, int problemCnt) {
        log.info("퀴즈 생성 요청: spotName={}, grade={}, problemCnt={}", spotName, grade, problemCnt);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        // FastAPI 요청 형식
        ProblemGenerateRequestToAI request = new ProblemGenerateRequestToAI(spotName, grade, problemCnt);
        HttpEntity<ProblemGenerateRequestToAI> entity = new HttpEntity<>(request, headers);

        try {
            // 🔥 핵심 변경: Map으로 받기
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/generate-problem",
                    entity,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            
            if (responseBody == null) {
                throw new PythonServerNoResponseException();
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> problemsData = (List<Map<String, Object>>) responseBody.get("problems");
            
            if (problemsData == null) {
                throw new ProblemGenerationFailedException();
            }
            if (problemsData.size() != problemCnt) {
                throw new ProblemCountMismatchException();
            }

            log.info("퀴즈 생성 성공: {}개 문제", problemsData.size());
            return responseBody;
            
        } catch (Exception e) {
            log.error("Python API 호출 실패: {}", e.getMessage());
            throw new PythonApiException("Python API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 메서드 - 호환성 유지용 (deprecated)
     * @deprecated Map 방식 사용 권장
     */
    @Deprecated
    public ProblemGenerateDto requestProblem(String spotName, int grade, int problemCnt) {
        // 일단 빈 DTO 반환 (실제로는 requestProblemAsMap 사용)
        throw new UnsupportedOperationException("requestProblemAsMap 메서드를 사용하세요");
    }

    /**
     * 셀피 포즈 분석 요청 (기존 메서드 시그니처 유지)
     */
    /**
     * 셀피 포즈 분석 요청 (최신 수정)
     */
    public SelfieResultDto requestDeterMineSelfie(SelfieRequestDto request) throws IOException {
        log.info("포즈 분석 요청: pose={}", request.getPose());

        // 1) 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 2) 파일 바이트 추출
        byte[] imageBytes = request.getMultipartFile().getBytes();
        if (imageBytes == null || imageBytes.length == 0) {
            throw new PythonApiException("이미지 바이트가 비어 있습니다.");
        }

        // 3) 파일 리소스 (filename + contentLength 오버라이드)
        ByteArrayResource fileResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                String name = request.getMultipartFile().getOriginalFilename();
                return (name == null || name.isBlank()) ? "upload.jpg" : name;
            }

            @Override
            public long contentLength() {
                return imageBytes.length;
            }
        };

        // 4) 멀티파트 바디 구성 (필드명 FastAPI와 일치)
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource); // ✅ "file"
        body.add("pose_select", request.getPose().name().toLowerCase()); // ✅ "pose_select"

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // 5) 호출
            ResponseEntity<SelfieResultDto> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/pose/predict",
                    requestEntity,
                    SelfieResultDto.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new PythonApiException("포즈 분석 실패: FastAPI 응답이 비정상입니다. status=" + response.getStatusCode());
            }

            SelfieResultDto result = response.getBody();
            log.info("포즈 분석 완료: success={}, result={}", result.isSuccess(), result.getResult());
            return result;

        } catch (Exception e) {
            log.error("포즈 분석 실패: {}", e.getMessage());
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