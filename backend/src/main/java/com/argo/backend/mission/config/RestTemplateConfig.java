package com.argo.backend.mission.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


@Configuration
@Slf4j
public class RestTemplateConfig {


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new LoggingInterceptor());

        return builder
                .setConnectTimeout(Duration.ofSeconds(10))  // 연결 타임아웃: 10초
                .setReadTimeout(Duration.ofSeconds(30))     // 읽기 타임아웃: 30초
                .additionalInterceptors(interceptors)       // 로깅 인터셉터 추가
                .build();
    }

    /**
     * HTTP 요청/응답 로깅 인터셉터
     */
    private static class LoggingInterceptor implements ClientHttpRequestInterceptor {
        
        @Override
        public org.springframework.http.client.ClientHttpResponse intercept(
                org.springframework.http.HttpRequest request,
                byte[] body,
                org.springframework.http.client.ClientHttpRequestExecution execution) throws java.io.IOException {
            
            long startTime = System.currentTimeMillis();
            
            // 요청 로깅
            log.info("🌐 HTTP 요청: {} {}", request.getMethod(), request.getURI());
            if (body.length > 0 && body.length < 1000) {  // 요청 바디가 1KB 미만일 때만 로깅
                log.debug("📤 요청 바디: {}", new String(body));
            }
            
            // 실제 요청 실행
            org.springframework.http.client.ClientHttpResponse response = execution.execute(request, body);
            
            long endTime = System.currentTimeMillis();
            
            // 응답 로깅
            log.info("📡 HTTP 응답: {} (소요시간: {}ms)", 
                    response.getStatusCode(), (endTime - startTime));
            
            return response;
        }
    }
}