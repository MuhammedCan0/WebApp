package com.university.auth_service.security;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Validiert API-Keys für Service-to-Service Kommunikation.
 * Verwaltet bekannte Services und ihre API-Keys.
 */
@Component
public class ApiKeyValidator {
    
    private final ApiKeyProperties apiKeyProperties;

    public ApiKeyValidator(ApiKeyProperties apiKeyProperties) {
        this.apiKeyProperties = apiKeyProperties;
    }

    /**
     * Prüft ob der API-Key gültig für den Service ist.
     * @param apiKey Der API-Key aus dem Request
     * @param serviceName Der Service-Name aus dem Request Header
     * @return true wenn gültig, false sonst
     */
    public boolean isValid(String apiKey, String serviceName) {
        if (apiKey == null || apiKey.isEmpty() || serviceName == null || serviceName.isEmpty()) {
            return false;
        }
        
        Map<String, String> allowed = apiKeyProperties.getAllowed();
        if (allowed == null) {
            return false;
        }

        String expectedKey = allowed.get(serviceName);
        if (expectedKey == null) {
            // Service nicht bekannt
            return false;
        }
        
        return expectedKey.equals(apiKey);
    }

    /**
     * Registriert einen neuen Service.
     * @param serviceName Name des Service
     * @param apiKey API-Key des Service
     */
    public void registerService(String serviceName, String apiKey) {
        Map<String, String> allowed = apiKeyProperties.getAllowed();
        if (allowed != null) {
            allowed.put(serviceName, apiKey);
        }
    }
}
