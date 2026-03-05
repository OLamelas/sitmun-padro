package com.sitmun.padro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Set;

public class ApiRequestFilter extends OncePerRequestFilter {

    @Value("${api.shared-secret}")
    private String sharedSecret;

    @Value("#{'${api.allowed-methods:GET}'.toUpperCase().split(',')}")
    private Set<String> allowedMethods;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        return pathMatcher.match("/actuator/health", request.getServletPath());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        if (!allowedMethods.contains(request.getMethod().toUpperCase())) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        final String apiSecretHeader = request.getHeader("X-API-Key");

        if (apiSecretHeader == null || !MessageDigest.isEqual(sharedSecret.getBytes(), apiSecretHeader.getBytes())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or missing API secret\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
