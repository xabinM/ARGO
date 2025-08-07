package com.argo.backend.mission.api;

import com.argo.backend.mission.dto.selfieDetermine.MultipartInputStreamFileResource;
import com.argo.backend.mission.dto.selfieDetermine.SelfieResultResponse;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateResponse;
import com.argo.backend.mission.dto.problemGenerate.ProblemGenerateRequestToAI;
import com.argo.backend.mission.exception.problem.ProblemCountMismatchException;
import com.argo.backend.mission.exception.problem.ProblemGenerationFailedException;
import com.argo.backend.mission.exception.problem.PythonServerNoResponseException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class PythonApiClient {

    private final RestTemplate restTemplate;
    private static final String PROBLEM_GENERATE_API_URL = "http://localhost:5000/api/generate";
    private static final String SELFIE_POSE_API_URL = "http://localhost:5000/api/predict-pose";

    public PythonApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProblemGenerateResponse requestProblem(String spotName, int problemCnt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ProblemGenerateRequestToAI request = new ProblemGenerateRequestToAI(spotName, problemCnt);

        HttpEntity<ProblemGenerateRequestToAI> entity = new HttpEntity<>(request, headers);

        ResponseEntity<ProblemGenerateResponse> response = restTemplate.postForEntity(
                PROBLEM_GENERATE_API_URL,
                entity,
                ProblemGenerateResponse.class
        );

        ProblemGenerateResponse body = response.getBody();
        if (body == null) {
            throw new PythonServerNoResponseException();
        }
        if (body.getProblems() == null) {
            throw new ProblemGenerationFailedException();
        }
        if (body.getProblems().size() != request.getProblemCnt()) {
            throw new ProblemCountMismatchException();
        }

        return body;
    }

    public SelfieResultResponse requestDeterMineSelfie(MultipartFile imageFile) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new MultipartInputStreamFileResource(imageFile.getInputStream(), imageFile.getOriginalFilename()));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<SelfieResultResponse> response = restTemplate.postForEntity(
                SELFIE_POSE_API_URL,
                requestEntity,
                SelfieResultResponse.class
        );

        return response.getBody();
    }
}
