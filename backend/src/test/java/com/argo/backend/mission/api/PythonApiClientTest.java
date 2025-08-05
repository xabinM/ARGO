package com.argo.backend.mission.api;

import org.junit.jupiter.api.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.web.client.RestTemplate;

public class PythonApiClientTest {

    private static ClientAndServer mockServer;
    private RestTemplate restTemplate = new RestTemplate();

    @BeforeAll
    static void startServer() {
        mockServer = ClientAndServer.startClientAndServer(5000); // 파이썬 서버처럼 포트 맞춰줌
    }

    @AfterAll
    static void stopServer() {
        mockServer.stop();
    }

    @Test
    void testCallPythonApi() {
        // Mock 응답 정의
        mockServer
                .when(HttpRequest.request().withMethod("POST").withPath("/generate-problem"))
                .respond(HttpResponse.response().withStatusCode(200).withBody("{\"result\": \"ok\"}"));

        // 실제 RestTemplate 호출
        String url = "http://localhost:5000/generate-problem";
        String response = restTemplate.postForObject(url, null, String.class);

        Assertions.assertEquals("{\"result\": \"ok\"}", response);
    }
}
