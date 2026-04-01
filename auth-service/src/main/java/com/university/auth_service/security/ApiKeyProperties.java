package com.university.auth_service.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Konfiguration für API-Key Authentication zwischen Services.
 * Properties aus application.yaml: apikey.key und apikey.name
 */
@Component
@ConfigurationProperties(prefix = "apikey")
public class ApiKeyProperties {
    
    private String key;
    private String name;
    private Map<String, String> allowed = new HashMap<>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getAllowed() {
        return allowed;
    }

    public void setAllowed(Map<String, String> allowed) {
        this.allowed = allowed;
    }
}
