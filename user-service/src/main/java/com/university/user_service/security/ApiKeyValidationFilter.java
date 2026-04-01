package com.university.user_service.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Filter zur Validierung von API-Keys für Service-to-Service Kommunikation.
 * Prüft ob X-Service-API-Key Header vorhanden und gültig ist.
 */
@Component
public class ApiKeyValidationFilter implements Filter {

    private final ApiKeyValidator apiKeyValidator;

    public ApiKeyValidationFilter(ApiKeyValidator apiKeyValidator) {
        this.apiKeyValidator = apiKeyValidator;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();
        // Nur /internal/* Endpoints brauchen API-Key Authentifizierung
        boolean isInternalRequest = requestPath != null && requestPath.contains("/internal/");

        if (isInternalRequest) {
            String apiKey = httpRequest.getHeader("X-Service-API-Key");
            String serviceName = httpRequest.getHeader("X-Service-Name");

            if (apiKey == null || apiKey.isEmpty() || serviceName == null || serviceName.isEmpty()) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"Missing API Key or Service Name header\"}");
                return;
            }

            if (!apiKeyValidator.isValid(apiKey, serviceName)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"Invalid API Key for service: " + serviceName + "\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}
