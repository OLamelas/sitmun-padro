package com.sitmun.padro.config;

import com.sitmun.padro.security.ApiRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for validating API key via X-API-Key header.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(PadroProperties.class)
public class SecurityConfig {

    @Bean
    public ApiRequestFilter apiRequestFilter() {
        return new ApiRequestFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiRequestFilter apiRequestFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(apiRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

