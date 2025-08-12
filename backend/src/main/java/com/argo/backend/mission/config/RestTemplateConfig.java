package com.argo.backend.mission.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter; // 🔥 추가
import org.springframework.http.converter.ResourceHttpMessageConverter; // 🔥 추가
import org.springframework.http.converter.ByteArrayHttpMessageConverter; // 🔥 추가
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
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

        // 🔥 UTF-8 인코딩 문제 해결을 위한 메시지 컨버터 설정
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();

        // 🔥 핵심 추가: 파일 업로드를 위한 컨버터들 (순서 중요!)
        messageConverters.add(new ByteArrayHttpMessageConverter()); // 바이트 배열 처리
        messageConverters.add(new ResourceHttpMessageConverter()); // 파일 리소스 처리
        messageConverters.add(new FormHttpMessageConverter()); // Form 데이터 처리 (multipart 포함)

        // 1. String 컨버터 - UTF-8 강제 설정
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false); // Accept-Charset 헤더 생성 방지
        messageConverters.add(stringConverter);

        // 2. JSON 컨버터 - UTF-8 + 한글 이스케이프 방지
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false); // 🔥 한글 이스케이프 방지
        
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);
        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8); // 🔥 UTF-8 강제 설정
        messageConverters.add(jsonConverter);

        return builder
                .setConnectTimeout(Duration.ofSeconds(10))  // 연결 타임아웃: 10초
                .setReadTimeout(Duration.ofSeconds(60))     // 🔥 읽기 타임아웃 증가: 60초 (포즈 분석용)
                .messageConverters(messageConverters)       // 🔥 커스텀 메시지 컨버터 적용
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
            log.info("📤 요청 헤더: {}", request.getHeaders());
            
            // 🔥 multipart 요청의 경우 바디 크기만 로깅
            if (request.getHeaders().getContentType() != null && 
                request.getHeaders().getContentType().toString().contains("multipart")) {
                log.info("📤 Multipart 요청 바디 크기: {} bytes", body.length);
            } else if (body.length > 0 && body.length < 1000) {
                log.debug("📤 요청 바디: {}", new String(body, StandardCharsets.UTF_8));
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