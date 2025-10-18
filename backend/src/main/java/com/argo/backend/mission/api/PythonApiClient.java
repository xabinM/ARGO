package com.argo.backend.mission.api;

import com.argo.backend.domain.ploblem.enums.PhotoPose;
import com.argo.backend.mission.dto.selfieDetermine.SelfieResultDto;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestToAI;
import com.argo.backend.mission.exception.problem.ProblemCountMismatchException;
import com.argo.backend.mission.exception.problem.ProblemGenerationFailedException;
import com.argo.backend.mission.exception.problem.PythonServerNoResponseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PythonApiClient {

    private final RestTemplate restTemplate;

    @Value("${python.api.base-url:http://localhost:8000}")
    private String pythonApiBaseUrl;

    public PythonApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> requestProblemAsMapWithApache(String spotName, int grade, int problemCnt) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(pythonApiBaseUrl + "/generate-problem");

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestData = Map.of(
                    "spotName", spotName,
                    "grade", grade,
                    "problemCnt", problemCnt
            );

            String jsonString = objectMapper.writeValueAsString(requestData);

            httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
            httpPost.setHeader("Accept", "application/json");

            StringEntity entity = new StringEntity(jsonString, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    throw new PythonApiException("FastAPI 응답 실패: " + statusCode + " - " + responseBody);
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> problemsData = (List<Map<String, Object>>) result.get("problems");

                if (problemsData == null) {
                    throw new ProblemGenerationFailedException();
                }
                if (problemsData.size() != problemCnt) {
                    throw new ProblemCountMismatchException();
                }

                return result;
            }

        } catch (Exception e) {
            throw new PythonApiException("Apache 퀴즈 생성 실패: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> requestProblemAsMap(String spotName, int grade, int problemCnt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        ProblemGenerateRequestToAI request = new ProblemGenerateRequestToAI(spotName, grade, problemCnt);
        HttpEntity<ProblemGenerateRequestToAI> entity = new HttpEntity<>(request, headers);

        try {
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

            return responseBody;

        } catch (Exception e) {
            throw new PythonApiException("RestTemplate API 호출 실패: " + e.getMessage(), e);
        }
    }

    public SelfieResultDto requestDeterMineSelfie(Integer teamMemberCnt, MultipartFile multipartFile, PhotoPose pose) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(pythonApiBaseUrl + "/pose/full");

            if (multipartFile == null || multipartFile.isEmpty()) {
                throw new PythonApiException("업로드할 파일이 없습니다");
            }

            byte[] fileBytes = multipartFile.getBytes();

            String contentType = multipartFile.getContentType();
            if (contentType == null) {
                contentType = "image/jpeg";
            }

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            ByteArrayBody fileBody = new ByteArrayBody(fileBytes,
                    org.apache.http.entity.ContentType.create(contentType),
                    multipartFile.getOriginalFilename());
            builder.addPart("file", fileBody);

            StringBody poseBody = new StringBody(pose.name().toLowerCase(),
                    org.apache.http.entity.ContentType.TEXT_PLAIN);
            builder.addPart("pose_select", poseBody);

            StringBody cntBody = new StringBody(teamMemberCnt.toString(),
                    org.apache.http.entity.ContentType.TEXT_PLAIN);
            builder.addPart("people_count", cntBody);

            org.apache.http.HttpEntity multipartEntity = builder.build();
            httpPost.setEntity(multipartEntity);

            httpPost.setHeader("Accept", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode == 200) {
                    ObjectMapper mapper = new ObjectMapper();
                    return mapper.readValue(responseBody, SelfieResultDto.class);
                } else {
                    throw new PythonApiException("HTTP " + statusCode + ": " + responseBody);
                }
            }

        } catch (IOException e) {
            throw new PythonApiException("파일 처리 실패: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new PythonApiException("Apache HttpClient 실패: " + e.getMessage(), e);
        }
    }

    public boolean testConnection() {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/pose/test",
                    null,
                    Map.class
            );

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getHealthStatus() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    pythonApiBaseUrl + "/health",
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("status", "error");
                errorMap.put("message", "Health check failed");
                return errorMap;
            }

        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", e.getMessage());
            return errorMap;
        }
    }

    public static class PythonApiException extends RuntimeException {
        public PythonApiException(String message) {
            super(message);
        }

        public PythonApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}