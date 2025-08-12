package com.argo.backend.auth.security.config;

import com.argo.backend.auth.security.CustomAccessDeniedHandler;
import com.argo.backend.auth.security.JwtAuthenticationEntryPoint;
import com.argo.backend.auth.security.jwt.JwtAuthenticationFilter;
import com.argo.backend.auth.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
//@EnableMethodSecurity 테스트용 임시 주석 처리
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint entryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // H2 콘솔을 위한 frameOptions 설정 추가
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        // 🔥 테스트용: 모든 요청 허용
                        .anyRequest().permitAll()
                        
                        // 원래 설정 (테스트 후 복구용)
                        /*
                        .requestMatchers(
                                "/api/users/signup",
                                "/api/users/login",
                                "/api/users/reissue",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/problem/**",
                                "/h2-console/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/problem/generate").permitAll()
                        .anyRequest().authenticated()
                        */
                );
                // JWT 필터도 테스트용으로 주석 처리 (이미 주석처리됨)
                //.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, entryPoint), UsernamePasswordAuthenticationFilter.class)

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}