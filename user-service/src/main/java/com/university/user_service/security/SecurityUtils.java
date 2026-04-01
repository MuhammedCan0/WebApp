package com.university.user_service.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class SecurityUtils {

    private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();
    private static final ThreadLocal<java.util.Set<com.university.user_service.common.Role>> currentRoles = new ThreadLocal<>();
    private static final ThreadLocal<String> currentEmail = new ThreadLocal<>();
    private static final ThreadLocal<java.util.Set<UUID>> currentTenantIds = new ThreadLocal<>();
    private static final ThreadLocal<java.util.Set<String>> currentStudiengaenge = new ThreadLocal<>();
    private static final ThreadLocal<Long> currentMatrikelnummer = new ThreadLocal<>();

    public static String getCurrentEmail() {
        return currentEmail.get();
    }

    public static UUID getCurrentUserId() {
        return currentUserId.get();
    }

    public static java.util.Set<com.university.user_service.common.Role> getCurrentRoles() {
        return currentRoles.get();
    }

    public static java.util.Set<UUID> getCurrentTenantIds() {
        return currentTenantIds.get();
    }

    public static java.util.Set<String> getCurrentStudiengaenge() {
        return currentStudiengaenge.get();
    }

    public static Long getCurrentMatrikelnummer() {
        return currentMatrikelnummer.get();
    }

    public static boolean hasRole(String role) {
        var roles = currentRoles.get();
        if (roles == null) {
            return false;
        }
        return roles.stream()
                .anyMatch(r -> r.name().equals(role));
    }

    public static void setAuthentication(UUID userId, java.util.Set<com.university.user_service.common.Role> roles, String email,
                                         java.util.Set<UUID> tenantIds, java.util.Set<String> studiengaenge, Long matrikelnummer) {
        currentUserId.set(userId);
        currentRoles.set(roles);
        currentEmail.set(email);
        currentTenantIds.set(tenantIds);
        currentStudiengaenge.set(studiengaenge);
        currentMatrikelnummer.set(matrikelnummer);
    }

    public static void clear() {
        currentUserId.remove();
        currentRoles.remove();
        currentEmail.remove();
        currentTenantIds.remove();
        currentStudiengaenge.remove();
        currentMatrikelnummer.remove();
    }
}
