package com.university.user_service.auth.client;

import com.university.user_service.exception.BadRequestException;
import com.university.user_service.exception.ConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AuthServiceClient(@Qualifier("authRestTemplate") RestTemplate restTemplate,
                             @Value("${auth.service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public void registerCredentials(Long matrikelnummer, String email, String password) {
        String url = baseUrl + "/api/v1/auth/register-credentials";
        RegisterCredentialsRequest request = new RegisterCredentialsRequest(matrikelnummer, email, password);
        try {
            restTemplate.exchange(url, HttpMethod.POST, buildEntity(request), Void.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ConflictException("User already exists", e);
            }
            throw new BadRequestException("Failed to register credentials", e);
        }
    }

    public Optional<String> getEmailByMatrikelnummer(Long matrikelnummer) {
        if (matrikelnummer == null) {
            return Optional.empty();
        }
        String url = baseUrl + "/api/v1/auth/internal/credentials/" + matrikelnummer;
        try {
            ResponseEntity<AuthCredentialsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    buildEntity(null),
                    AuthCredentialsResponse.class
            );
            return Optional.ofNullable(response.getBody()).map(AuthCredentialsResponse::getEmail);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to fetch credentials", e);
        }
    }

    public AuthCredentialsResponse updateCredentials(Long matrikelnummer, String email, String password, Long newMatrikelnummer) {
        String url = baseUrl + "/api/v1/auth/internal/credentials/" + matrikelnummer;
        UpdateCredentialsRequest request = new UpdateCredentialsRequest(email, password, newMatrikelnummer);
        try {
            ResponseEntity<AuthCredentialsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    buildEntity(request),
                    AuthCredentialsResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to update credentials", e);
        }
    }

    public void deleteCredentials(Long matrikelnummer) {
        if (matrikelnummer == null) {
            return;
        }
        String url = baseUrl + "/api/v1/auth/internal/credentials/" + matrikelnummer;
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, buildEntity(null), Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            // Credentials existieren nicht - das ist ok bei Löschung
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to delete credentials", e);
        }
    }

    private HttpEntity<?> buildEntity(Object body) {
        return new HttpEntity<>(body, buildHeaders());
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        // API-Key Headers werden automatisch vom ApiKeyInterceptor gesetzt!
        return headers;
    }

    private record RegisterCredentialsRequest(Long matrikelnummer, String email, String password) {
    }

    private record UpdateCredentialsRequest(String email, String password, Long newMatrikelnummer) {
    }
}
