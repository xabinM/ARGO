package com.argo.backend.mission.api;

import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateResponse;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestToAI;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PythonApiClient {

    private final RestTemplate restTemplate;
    private static final String PYTHON_API_URL = "http://localhost:5000/api/generate";

    public PythonApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProblemGenerateResponse requestProblem(String spotName, int problemCnt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ProblemGenerateRequestToAI request = new ProblemGenerateRequestToAI(spotName, problemCnt);

        HttpEntity<ProblemGenerateRequestToAI> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProblemGenerateResponse> response = restTemplate.postForEntity(
                PYTHON_API_URL,
                entity,
                ProblemGenerateResponse.class
        );

        ProblemGenerateResponse body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Python 서버에서 응답이 없습니다.");
        }
        if (body.getProblems() == null) {
            throw new IllegalStateException("문제 생성 실패");
        }
        if (body.getProblems().size() != request.getProblemCnt()) {
            throw new IllegalStateException("요청한 문제 개수와 맞지 않습니다.");
        }

            return body;
    }

}
