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

// Apache HttpClient imports (import 충돌 해결)
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

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
     * 퀴즈 생성 요청 - Map 방식
     */
    public Map<String, Object> requestProblemAsMap(String spotName, int grade, int problemCnt) {
        log.info("📝 퀴즈 생성 요청: spotName={}, grade={}, problemCnt={}", spotName, grade, problemCnt);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

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
     * 🔥 Apache HttpClient를 사용한 포즈 분석 (Python requests와 동일한 방식)
     */
    public SelfieResultDto requestDeterMineSelfie(SelfieRequestDto request) throws IOException {
        log.info("🎯 Apache HttpClient로 포즈 분석 시작: pose={}", request.getPose());
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(pythonApiBaseUrl + "/pose/full");
            
            MultipartFile multipartFile = request.getMultipartFile();
            if (multipartFile == null || multipartFile.isEmpty()) {
                throw new PythonApiException("업로드할 파일이 없습니다");
            }

            byte[] fileBytes = multipartFile.getBytes();
            
            // Content-Type null 체크 및 기본값 설정
            String contentType = multipartFile.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg"; // 기본값
            }
            
            log.info("📁 파일 정보:");
            log.info("  - 파일명: {}", multipartFile.getOriginalFilename());
            log.info("  - Content-Type: {}", contentType);
            log.info("  - 파일 크기: {} bytes", fileBytes.length);
            
            // 멀티파트 엔티티 생성 (Python requests와 동일한 방식)
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            
            // 파일 파트 추가
            ByteArrayBody fileBody = new ByteArrayBody(fileBytes, 
                org.apache.http.entity.ContentType.create(contentType),
                multipartFile.getOriginalFilename());
            builder.addPart("file", fileBody);
            
            // 텍스트 파트 추가
            StringBody poseBody = new StringBody(request.getPose().name().toLowerCase(), 
                org.apache.http.entity.ContentType.TEXT_PLAIN);
            builder.addPart("pose_select", poseBody);
            
            // 🔥 풀네임 사용으로 import 충돌 해결
            org.apache.http.HttpEntity multipartEntity = builder.build();
            httpPost.setEntity(multipartEntity);
            
            // 헤더 설정
            httpPost.setHeader("Accept", "application/json");
            
            log.info("📤 Apache HttpClient 요청 전송");
            log.info("  - URL: {}/pose/full", pythonApiBaseUrl);
            log.info("  - 포즈: {}", request.getPose().name().toLowerCase());
            
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                log.info("📥 Apache HttpClient 응답:");
                log.info("  - 상태 코드: {}", statusCode);
                log.info("  - 응답 길이: {} bytes", responseBody.length());
                
                if (statusCode == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    SelfieResultDto result = mapper.readValue(responseBody, SelfieResultDto.class);
                    
                    log.info("✅ 포즈 분석 성공: {}", result.getResult());
                    return result;
                } else {
                    log.error("❌ HTTP 에러: {}", responseBody);
                    throw new PythonApiException("HTTP " + statusCode + ": " + responseBody);
                }
            }
            
        } catch (IOException e) {
            log.error("❌ 파일 처리 실패: {}", e.getMessage());
            throw new PythonApiException("파일 처리 실패: " + e.getMessage(), e);
            
        } catch (Exception e) {
            log.error("❌ Apache HttpClient 실패: {}", e.getMessage());
            throw new PythonApiException("Apache HttpClient 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 🔄 Fallback: Spring RestTemplate 방식 (백업용)
     */
    public SelfieResultDto requestDeterMineSelfieWithRestTemplate(SelfieRequestDto request) throws IOException {
        log.info("🎯 RestTemplate로 포즈 분석 시작: pose={}", request.getPose());

        try {
            MultipartFile multipartFile = request.getMultipartFile();
            if (multipartFile == null || multipartFile.isEmpty()) {
                throw new PythonApiException("업로드할 파일이 없습니다");
            }

            byte[] fileBytes = multipartFile.getBytes();

            // 멀티파트 바디 생성
            MultiValueMap<String, org.springframework.http.HttpEntity<?>> multipartBody = new LinkedMultiValueMap<>();

            // 파일 파트
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.parseMediaType(multipartFile.getContentType()));
            fileHeaders.setContentDispositionFormData("file", multipartFile.getOriginalFilename());
            org.springframework.http.HttpEntity<byte[]> fileEntity = new org.springframework.http.HttpEntity<>(fileBytes, fileHeaders);
            multipartBody.add("file", fileEntity);

            // 텍스트 파트
            HttpHeaders textHeaders = new HttpHeaders();
            textHeaders.setContentDispositionFormData("pose_select", null);
            org.springframework.http.HttpEntity<String> textEntity = new org.springframework.http.HttpEntity<>(request.getPose().name().toLowerCase(), textHeaders);
            multipartBody.add("pose_select", textEntity);

            // 요청 헤더
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            org.springframework.http.HttpEntity<MultiValueMap<String, org.springframework.http.HttpEntity<?>>> requestEntity =
                    new org.springframework.http.HttpEntity<>(multipartBody, headers);

            log.info("📤 RestTemplate 요청 전송");

            ResponseEntity<SelfieResultDto> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/pose/full",
                    requestEntity,
                    SelfieResultDto.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new PythonApiException("FastAPI 응답 실패: " + response.getStatusCode());
            }

            SelfieResultDto result = response.getBody();
            if (result == null) {
                throw new PythonApiException("응답 바디가 null입니다");
            }

            log.info("✅ RestTemplate 포즈 분석 성공: {}", result.getResult());
            return result;

        } catch (Exception e) {
            log.error("❌ RestTemplate 포즈 분석 실패: {}", e.getMessage());
            throw new PythonApiException("RestTemplate 포즈 분석 실패: " + e.getMessage(), e);
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
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            byte[] fileBytes = request.getMultipartFile().getBytes();
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return request.getMultipartFile().getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("pose_select", request.getPose().name().toLowerCase());

            org.springframework.http.HttpEntity<MultiValueMap<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(body, headers);

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