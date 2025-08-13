package com.argo.backend.mission.api;

import com.argo.backend.mission.dto.selfieDetermine.SelfieRequestDto;
import com.argo.backend.mission.dto.selfieDetermine.SelfieResultDto;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestToAI;
import com.argo.backend.mission.exception.problem.ProblemCountMismatchException;
import com.argo.backend.mission.exception.problem.ProblemGenerationFailedException;
import com.argo.backend.mission.exception.problem.PythonServerNoResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
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
        log.info("📝 퀴즈 생성 요청: spotName={}, grade={}, problemCnt={}", spotName, grade, problemCnt);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        // FastAPI 요청 형식
        ProblemGenerateRequestToAI request = new ProblemGenerateRequestToAI(spotName, grade, problemCnt);
        HttpEntity<ProblemGenerateRequestToAI> entity = new HttpEntity<>(request, headers);

        try {
            log.info("📡 FastAPI 퀴즈 생성 호출: {}/generate-problem", pythonApiBaseUrl);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/generate-problem",
                    entity,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            
            if (responseBody == null) {
                log.error("❌ FastAPI 응답 바디가 null입니다");
                throw new PythonServerNoResponseException();
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> problemsData = (List<Map<String, Object>>) responseBody.get("problems");
            
            if (problemsData == null) {
                log.error("❌ 퀴즈 데이터가 null입니다");
                throw new ProblemGenerationFailedException();
            }
            if (problemsData.size() != problemCnt) {
                log.error("❌ 요청한 문제 개수({})와 응답 개수({})가 다릅니다", problemCnt, problemsData.size());
                throw new ProblemCountMismatchException();
            }

            log.info("✅ 퀴즈 생성 성공: {}개 문제", problemsData.size());
            return responseBody;
            
        } catch (Exception e) {
            log.error("❌ Python API 호출 실패: {}", e.getMessage());
            throw new PythonApiException("Python API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 🔥 핵심 수정: 셀피 포즈 분석 요청 (기존 구조 유지하며 수정)
     */
    public SelfieResultDto requestDeterMineSelfie(SelfieRequestDto request) throws IOException {
        log.info("🎯 포즈 분석 요청 시작: pose={}", request.getPose());

        try {
            MultipartFile multipartFile = request.getMultipartFile();
            if (multipartFile == null || multipartFile.isEmpty()) {
                throw new PythonApiException("업로드할 파일이 없습니다");
            }

            byte[] fileBytes = multipartFile.getBytes();
            log.info("📁 파일 정보:");
            log.info("  - 파일명: {}", multipartFile.getOriginalFilename());
            log.info("  - Content-Type: {}", multipartFile.getContentType());
            log.info("  - 파일 크기: {} bytes", fileBytes.length);

            // === 🔥 핵심 수정: MultipartBodyBuilder 사용 ===
            MultiValueMap<String, HttpEntity<?>> multipartBody = new LinkedMultiValueMap<>();

            // 1. 파일 파트 생성 (Python requests와 동일한 방식)
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(multipartFile.getContentType()));
            fileHeaders.setContentDispositionFormData("file", multipartFile.getOriginalFilename());
            HttpEntity<byte[]> fileEntity = new HttpEntity<>(fileBytes, fileHeaders);
            multipartBody.add("file", fileEntity);

            // 2. 텍스트 파트 생성
            HttpHeaders textHeaders = new HttpHeaders();
            textHeaders.setContentDispositionFormData("pose_select", null);
            HttpEntity<String> textEntity = new HttpEntity<>(request.getPose().name().toLowerCase(), textHeaders);
            multipartBody.add("pose_select", textEntity);

            // === 헤더 설정 ===
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity =
                    new HttpEntity<>(multipartBody, headers);

            // === 요청 로깅 ===
            log.info("📤 수정된 멀티파트 요청:");
            log.info("  - URL: {}/pose/full", pythonApiBaseUrl);
            log.info("  - 파트 수: {}", multipartBody.size());

            // === FastAPI 호출 ===
            log.info("📡 FastAPI 포즈 분석 호출...");

            ResponseEntity<SelfieResultDto> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/pose/full",
                    requestEntity,
                    SelfieResultDto.class
            );

            // === 응답 검증 ===
            log.info("📥 응답:");
            log.info("  - 상태 코드: {}", response.getStatusCode());
            log.info("  - 성공: {}", response.getStatusCode().is2xxSuccessful());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new PythonApiException("FastAPI 응답 실패: " + response.getStatusCode());
            }

            SelfieResultDto result = response.getBody();
            if (result == null) {
                throw new PythonApiException("응답 바디가 null입니다");
            }

            log.info("✅ 포즈 분석 성공: {}", result.getResult());
            return result;

        } catch (Exception e) {
            log.error("❌ 포즈 분석 실패: {}", e.getMessage());
            throw new PythonApiException("포즈 분석 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 연결 테스트 메서드
     */
    public boolean testConnection() {
        try {
            log.info("🔍 FastAPI 연결 테스트 시작");
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                pythonApiBaseUrl + "/pose/test",
                null,
                Map.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("✅ 연결 테스트 결과: {}", success ? "성공" : "실패");
            
            if (success && response.getBody() != null) {
                log.info("📥 응답: {}", response.getBody());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("❌ 연결 테스트 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * GET 방식 연결 테스트
     */
    public boolean testConnectionGet() {
        try {
            log.info("🔍 FastAPI 연결 테스트 시작 (GET)");
            
            ResponseEntity<Map> response = restTemplate.getForEntity(
                pythonApiBaseUrl + "/pose/test",
                Map.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("✅ GET 연결 테스트 결과: {}", success ? "성공" : "실패");
            
            if (success && response.getBody() != null) {
                log.info("📥 서버 응답: {}", response.getBody());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("❌ GET 연결 테스트 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 디버깅용 메서드
     */
    public Map<String, Object> debugPoseRequest(SelfieRequestDto request) throws IOException {
        try {
            log.info("🔍 디버깅 요청 시작");
            
            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // 파일 처리
            byte[] fileBytes = request.getMultipartFile().getBytes();
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return request.getMultipartFile().getOriginalFilename();
                }
            };

            // 멀티파트 바디
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("pose_select", request.getPose().name().toLowerCase());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 디버깅 엔드포인트 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/pose/debug",
                    requestEntity,
                    Map.class
            );

            log.info("📥 디버깅 응답: {}", response.getBody());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("❌ 디버깅 실패: {}", e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("success", false);
            errorMap.put("error", e.getMessage());
            return errorMap;
        }
    }
    
    /**
     * 헬스체크
     */
    public Map<String, Object> getHealthStatus() {
        try {
            log.info("🔍 FastAPI 헬스체크 시작");
            
            ResponseEntity<Map> response = restTemplate.getForEntity(
                pythonApiBaseUrl + "/health",
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("✅ 헬스체크 성공");
                return response.getBody();
            } else {
                log.error("❌ 헬스체크 실패: {}", response.getStatusCode());
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("status", "error");
                errorMap.put("message", "Health check failed");
                return errorMap;
            }
            
        } catch (Exception e) {
            log.error("❌ 헬스체크 예외: {}", e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", e.getMessage());
            return errorMap;
        }
    }

    /**
     * Python API 예외 클래스
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