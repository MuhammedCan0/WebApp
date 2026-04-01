package com.university.auth_service.auth;

import com.university.auth_service.auth.credentials.AuthCredentials;
import com.university.auth_service.auth.credentials.AuthCredentialsService;
import com.university.auth_service.auth.dto.AuthCredentialsResponse;
import com.university.auth_service.auth.dto.LoginRequest;
import com.university.auth_service.auth.dto.LoginResponse;
import com.university.auth_service.auth.dto.RegisterCredentialsRequest;
import com.university.auth_service.auth.dto.UpdateCredentialsRequest;
import com.university.auth_service.security.JwtProperties;
import com.university.auth_service.user.client.UserAuthDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final AuthCredentialsService credentialsService;

    public AuthController(AuthService authService,
                          JwtProperties jwtProperties,
                          AuthCredentialsService credentialsService) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
        this.credentialsService = credentialsService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@jakarta.validation.Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        AuthResult result = authService.authenticate(request.getEmail(), request.getPassword());
        UserAuthDto user = result.user();

        LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .email(result.email())
                .token(result.token())
                .roles(user.getRoles())
                .expiresIn(jwtProperties.getExpirationMs())
                .build();

        log.info("User {} logged in successfully", request.getEmail());
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/register-credentials")
    public ResponseEntity<AuthCredentialsResponse> registerCredentials(
            @jakarta.validation.Valid @RequestBody RegisterCredentialsRequest request) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter
        AuthCredentials credentials = credentialsService.registerCredentials(
                request.getMatrikelnummer(),
                request.getEmail(),
                request.getPassword()
        );

        AuthCredentialsResponse response = AuthCredentialsResponse.builder()
                .matrikelnummer(credentials.getMatrikelnummer())
                .email(credentials.getEmail())
                .build();

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/internal/credentials/{matrikelnummer}")
    public ResponseEntity<AuthCredentialsResponse> getCredentialsByMatrikelnummer(
            @PathVariable Long matrikelnummer) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter
        AuthCredentials credentials = credentialsService.getByMatrikelnummer(matrikelnummer);

        AuthCredentialsResponse response = AuthCredentialsResponse.builder()
                .matrikelnummer(credentials.getMatrikelnummer())
                .email(credentials.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/internal/credentials/{matrikelnummer}")
    public ResponseEntity<AuthCredentialsResponse> updateCredentials(
            @PathVariable Long matrikelnummer,
            @jakarta.validation.Valid @RequestBody UpdateCredentialsRequest request) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter

        if ((request.getEmail() == null || request.getEmail().isBlank())
                && (request.getPassword() == null || request.getPassword().isBlank())
                && request.getNewMatrikelnummer() == null) {
            throw new com.university.auth_service.exception.BadRequestException("No credential changes provided");
        }

        AuthCredentials updated = credentialsService.updateCredentials(
                matrikelnummer,
                request.getEmail(),
                request.getPassword(),
                request.getNewMatrikelnummer()
        );

        AuthCredentialsResponse response = AuthCredentialsResponse.builder()
                .matrikelnummer(updated.getMatrikelnummer())
                .email(updated.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/internal/credentials/{matrikelnummer}")
    public ResponseEntity<Void> deleteCredentials(@PathVariable Long matrikelnummer) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter
        credentialsService.deleteCredentials(matrikelnummer);
        log.info("Credentials deleted for matrikelnummer: {}", matrikelnummer);
        return ResponseEntity.noContent().build();
    }
}
