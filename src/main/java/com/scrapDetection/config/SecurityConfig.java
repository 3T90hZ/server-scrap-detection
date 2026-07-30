package com.scrapDetection.config;

import com.scrapDetection.security.device.DeviceAuthenticationFilter;
import com.scrapDetection.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DeviceAuthenticationFilter deviceAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/scrap-yards/request",     // ← Make sure exact match
                                "/api/scrap-yards/**",
                                "/api/materials/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/detections", "/api/detections/frame")
                        .hasAnyRole("STAFF", "YARD_OWNER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/detections", "/api/detections/frame")
                        .hasRole("DEVICE")

                        .requestMatchers("/api/detections/**").hasRole("DEVICE")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)  // ← Use the field
                .addFilterBefore(deviceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}