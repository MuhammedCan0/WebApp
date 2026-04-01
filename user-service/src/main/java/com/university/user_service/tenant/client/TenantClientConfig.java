package com.university.user_service.tenant.client;

import com.university.user_service.security.ApiKeyInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

@Configuration
public class TenantClientConfig {

    private final ApiKeyInterceptor apiKeyInterceptor;

    public TenantClientConfig(ApiKeyInterceptor apiKeyInterceptor) {
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Bean
    public RestTemplate tenantRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds in milliseconds
        factory.setReadTimeout(10000);   // 10 seconds in milliseconds
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Registriere den API-Key Interceptor
        restTemplate.setInterceptors(Collections.singletonList(apiKeyInterceptor));
        
        return restTemplate;
    }
}
