package com.university.user_service.security;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Interceptor für RestTemplate - fügt API-Key Header zu alle outgoing Service-to-Service Requests hinzu.
 * Dies sichert die Kommunikation zwischen Microservices.
 */
@Component
public class ApiKeyInterceptor implements ClientHttpRequestInterceptor {

    private final ApiKeyProperties apiKeyProperties;

    public ApiKeyInterceptor(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        
        // Füge API-Key als custom Header hinzu
        request.getHeaders().set("X-Service-API-Key", apiKeyProperties.getKey());
        
        // Füge Service-Name hinzu für Logging/Debugging
        request.getHeaders().set("X-Service-Name", apiKeyProperties.getName());
        
        // Fahre mit der Request fort
        return execution.execute(request, body);
    }
}
