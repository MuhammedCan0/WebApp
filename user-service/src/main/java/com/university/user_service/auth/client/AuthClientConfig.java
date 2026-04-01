package com.university.user_service.auth.client;

import com.university.user_service.security.ApiKeyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.net.http.HttpClient;
import java.util.Collections;

@Configuration
public class AuthClientConfig {

    private final ApiKeyInterceptor apiKeyInterceptor;

    public AuthClientConfig(ApiKeyInterceptor apiKeyInterceptor) {
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Bean(name = "authRestTemplate")
    public RestTemplate authRestTemplate() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(10));
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Registriere den API-Key Interceptor
        restTemplate.setInterceptors(Collections.singletonList(apiKeyInterceptor));
        
        return restTemplate;
    }
}
