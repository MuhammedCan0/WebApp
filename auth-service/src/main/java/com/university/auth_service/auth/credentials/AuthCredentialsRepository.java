package com.university.auth_service.auth.credentials;

import org.springframework.stereotype.Repository;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AuthCredentialsRepository {

    private final Map<Long, AuthCredentials> byMatrikelnummer = new ConcurrentHashMap<>();
    private final Map<String, Long> matrikelnummerByEmail = new ConcurrentHashMap<>();

    public Optional<AuthCredentials> findByMatrikelnummer(Long matrikelnummer) {
        if (matrikelnummer == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byMatrikelnummer.get(matrikelnummer));
    }

    public Optional<AuthCredentials> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return Optional.empty();
        }
        Long matrikelnummer = matrikelnummerByEmail.get(normalizedEmail);
        if (matrikelnummer == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byMatrikelnummer.get(matrikelnummer));
    }

    public boolean existsByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return normalizedEmail != null && matrikelnummerByEmail.containsKey(normalizedEmail);
    }

    public boolean existsByMatrikelnummer(Long matrikelnummer) {
        return matrikelnummer != null && byMatrikelnummer.containsKey(matrikelnummer);
    }

    public AuthCredentials save(AuthCredentials credentials) {
        if (credentials == null) {
            return null;
        }
        String normalizedEmail = normalizeEmail(credentials.getEmail());
        if (normalizedEmail != null) {
            matrikelnummerByEmail.put(normalizedEmail, credentials.getMatrikelnummer());
        }
        byMatrikelnummer.put(credentials.getMatrikelnummer(), credentials);
        return credentials;
    }

    public void deleteByMatrikelnummer(Long matrikelnummer) {
        if (matrikelnummer == null) {
            return;
        }
        AuthCredentials removed = byMatrikelnummer.remove(matrikelnummer);
        if (removed == null) {
            return;
        }
        String normalizedEmail = normalizeEmail(removed.getEmail());
        if (normalizedEmail != null) {
            matrikelnummerByEmail.remove(normalizedEmail);
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
