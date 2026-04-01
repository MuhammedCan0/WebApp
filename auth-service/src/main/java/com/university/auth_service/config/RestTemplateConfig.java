package com.university.auth_service.config;

import com.university.auth_service.security.ApiKeyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    private final ApiKeyInterceptor apiKeyInterceptor;

    public RestTemplateConfig(ApiKeyInterceptor apiKeyInterceptor) {
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Registriere den API-Key Interceptor für alle Requests
        restTemplate.setInterceptors(Collections.singletonList(apiKeyInterceptor));
        
        return restTemplate;
    }
}
