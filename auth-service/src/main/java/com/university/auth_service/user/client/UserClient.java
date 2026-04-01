package com.university.auth_service.user.client;

import com.university.auth_service.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class UserClient {

    private static final String INTERNAL_CALL_HEADER = "X-Internal-Call";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${user.service.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Optional<UserAuthDto> findByMatrikelnummer(Long matrikelnummer) {
        if (matrikelnummer == null) {
            return Optional.empty();
        }
        String url = baseUrl + "/api/v1/users/internal/by-matrikelnummer/" + matrikelnummer;
        try {
            ResponseEntity<UserAuthDto> response = restTemplate.exchange(url, HttpMethod.GET, buildEntity(null), UserAuthDto.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            throw new BadRequestException("Failed to fetch user by matrikelnummer", e);
        }
    }

    private HttpEntity<?> buildEntity(Object body) {
        return new HttpEntity<>(body, buildHeaders());
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(INTERNAL_CALL_HEADER, "true");
        return headers;
    }
}
