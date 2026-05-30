package com.redpatitas.redPatitas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redpatitas.redPatitas.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import com.redpatitas.redPatitas.security.JsonAuthenticationEntryPoint;
import com.redpatitas.redPatitas.dto.response.ApiErrorResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final SecurityProperties securityProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http
            // CSRF disabled because this is a stateless REST API using JWT authentication.
            // No session or cookies are used, so CSRF protection is not required.
            .csrf(AbstractHttpConfigurer::disable)
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(this::securityHeaders)
            .exceptionHandling(e -> e
                .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    String trace = firstNonBlank(
                            org.slf4j.MDC.get(TraceIdFilter.TRACE_ID_MDC),
                            request.getHeader(TraceIdFilter.TRACE_ID_HEADER));
                    var body = new ApiErrorResponse(
                            "ACCESS_DENIED",
                            "Permisos insuficientes para este recurso",
                            null,
                            trace);
                    try {
                        objectMapper.writeValue(response.getOutputStream(), body);
                    } catch (IOException ioEx) {
                        // ignore
                    }
                }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/register").permitAll()
                //.requestMatchers(HttpMethod.GET, "/api/reports", "/api/reports/resolved").permitAll()
                //.requestMatchers(HttpMethod.POST, "/api/reports/form").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/v1/users/internal/**").permitAll()
                .requestMatchers("/api/admin/**").authenticated()
                .anyRequest().permitAll())
            .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void securityHeaders(HeadersConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity> headers) {
        headers.frameOptions(frame -> frame.deny());
        headers.contentTypeOptions(Customizer.withDefaults());
        headers.referrerPolicy(referrer -> referrer.policy(
                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
        if (securityProperties.isHstsEnabled()) {
            headers.httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000).includeSubDomains(true));
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        List<String> origins = corsProperties.allowedOriginList();
        if (origins == null || origins.isEmpty()) {
            // En desarrollo permitir el puerto más usado por frontends (Vite/React) y cualquier patrón si es necesario
            config.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://localhost:5173", "*"));
        } else if (origins.contains("*")) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(origins);
        }
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Trace-Id", "X-User-Id", "X-User-Roles"));
        config.setExposedHeaders(List.of("X-Trace-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
