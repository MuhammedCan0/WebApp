package com.university.user_service.tenant.client;

import com.university.user_service.exception.BadRequestException;
import com.university.user_service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
public class TenantClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TenantClient(RestTemplate tenantRestTemplate,
                        @Value("${tenant.service.base-url}") String baseUrl) {
        this.restTemplate = tenantRestTemplate;
        this.baseUrl = baseUrl;
    }

    public Optional<TenantDto> findById(UUID id) {
        String url = baseUrl + "/api/v1/tenants/" + id;
        try {
            ResponseEntity<TenantDto> response = restTemplate.exchange(url, HttpMethod.GET, buildEntity(null), TenantDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public Optional<TenantDto> findByIdentifier(String identifier) {
        String url = baseUrl + "/api/v1/tenants/by-identifier/" + identifier;
        try {
            ResponseEntity<TenantDto> response = restTemplate.exchange(url, HttpMethod.GET, buildEntity(null), TenantDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public UUID resolveTenantIdForStudiengang(String studiengang) {
        String url = baseUrl + "/api/v1/tenants/resolve?studiengang=" + studiengang;
        try {
            ResponseEntity<UUID> response = restTemplate.exchange(url, HttpMethod.GET, buildEntity(null), UUID.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to resolve tenant for studiengang: " + studiengang, e);
        }
    }

    public boolean isStudiengangAllowedForTenant(UUID tenantId, String studiengang) {
        String url = baseUrl + "/api/v1/tenants/" + tenantId + "/studiengaenge/" + studiengang + "/allowed";
        try {
            ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.GET, buildEntity(null), Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to validate studiengang for tenant", e);
        }
    }

    public String getTenantIdentifier(UUID tenantId) {
        String url = baseUrl + "/api/v1/tenants/" + tenantId + "/identifier";
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, buildEntity(null), String.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Tenant not found");
        }
    }

    private HttpEntity<?> buildEntity(Object body) {
        return new HttpEntity<>(body, buildHeaders());
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        // API-Key Headers werden automatisch vom ApiKeyInterceptor gesetzt!
        // Keine User-Context-Headers für Service-to-Service Kommunikation
        return headers;
    }
}
