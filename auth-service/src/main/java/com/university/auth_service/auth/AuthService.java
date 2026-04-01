package com.university.auth_service.auth;

import com.university.auth_service.common.Role;
import com.university.auth_service.common.Status;
import com.university.auth_service.auth.credentials.AuthCredentials;
import com.university.auth_service.auth.credentials.AuthCredentialsService;
import com.university.auth_service.exception.UnauthorizedException;
import com.university.auth_service.security.JwtService;
import com.university.auth_service.user.client.UserAuthDto;
import com.university.auth_service.user.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final UserClient userClient;
    private final JwtService jwtService;
    private final AuthCredentialsService credentialsService;

    public AuthService(UserClient userClient,
                       JwtService jwtService,
                       AuthCredentialsService credentialsService) {
        this.userClient = userClient;
        this.jwtService = jwtService;
        this.credentialsService = credentialsService;
    }

    public AuthResult authenticate(String email, String password) {
        AuthCredentials credentials = credentialsService.authenticate(email, password);
        UserAuthDto user = userClient.findByMatrikelnummer(credentials.getMatrikelnummer())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getStatus() != Status.ACTIVE) {
            log.warn("Login attempt for non-active user: {} (Status: {})", email, user.getStatus());
            throw new UnauthorizedException("User account is not active");
        }

        // Trust tenants from User-Service (already validated)
        Set<UUID> tenantIds = user.getTenantIds() != null ? user.getTenantIds() : Collections.emptySet();
        if (user.getRoles() != null && !user.getRoles().contains(Role.ADMIN) && tenantIds.isEmpty()) {
            throw new UnauthorizedException("User has no active tenants");
        }

        String token = jwtService.generateToken(
                user.getId(),
            credentials.getEmail(),
                user.getRoles(),
                user.getStudiengaenge(),
                tenantIds,
                user.getMatrikelnummer()
        );

        log.info("User {} authenticated successfully", email);
        return new AuthResult(token, credentials.getEmail(), user);
    }


}
