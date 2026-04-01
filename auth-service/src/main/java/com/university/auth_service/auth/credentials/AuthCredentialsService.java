package com.university.auth_service.auth.credentials;

import com.university.auth_service.exception.ConflictException;
import com.university.auth_service.exception.NotFoundException;
import com.university.auth_service.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthCredentialsService {

    private final AuthCredentialsRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthCredentialsService(AuthCredentialsRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthCredentials registerCredentials(Long matrikelnummer, String email, String rawPassword) {
        if (repository.existsByMatrikelnummer(matrikelnummer)) {
            throw new ConflictException("Credentials already exist for matrikelnummer");
        }
        if (repository.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        AuthCredentials credentials = AuthCredentials.builder()
                .matrikelnummer(matrikelnummer)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();
        return repository.save(credentials);
    }

    public AuthCredentials authenticate(String email, String rawPassword) {
        AuthCredentials credentials = repository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, credentials.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return credentials;
    }

    public AuthCredentials getByMatrikelnummer(Long matrikelnummer) {
        return repository.findByMatrikelnummer(matrikelnummer)
                .orElseThrow(() -> new NotFoundException("Credentials not found"));
    }

    public AuthCredentials updateCredentials(Long matrikelnummer, String email, String rawPassword, Long newMatrikelnummer) {
        AuthCredentials existing = repository.findByMatrikelnummer(matrikelnummer)
                .orElseThrow(() -> new NotFoundException("Credentials not found"));

        Long effectiveMatrikelnummer = matrikelnummer;
        if (newMatrikelnummer != null && !newMatrikelnummer.equals(matrikelnummer)) {
            if (repository.existsByMatrikelnummer(newMatrikelnummer)) {
                throw new ConflictException("Matrikelnummer is already registered");
            }
            effectiveMatrikelnummer = newMatrikelnummer;
        }

        String effectiveEmail = existing.getEmail();
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(existing.getEmail())) {
            if (repository.existsByEmail(email)) {
                throw new ConflictException("Email is already registered");
            }
            effectiveEmail = email;
        }

        String effectivePasswordHash = existing.getPasswordHash();
        if (rawPassword != null && !rawPassword.isBlank()) {
            effectivePasswordHash = passwordEncoder.encode(rawPassword);
        }

        AuthCredentials updated = AuthCredentials.builder()
                .matrikelnummer(effectiveMatrikelnummer)
                .email(effectiveEmail)
                .passwordHash(effectivePasswordHash)
                .build();

        repository.deleteByMatrikelnummer(matrikelnummer);
        return repository.save(updated);
    }

    public Optional<AuthCredentials> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public void deleteCredentials(Long matrikelnummer) {
        if (!repository.existsByMatrikelnummer(matrikelnummer)) {
            throw new NotFoundException("Credentials not found for matrikelnummer: " + matrikelnummer);
        }
        repository.deleteByMatrikelnummer(matrikelnummer);
    }
}
