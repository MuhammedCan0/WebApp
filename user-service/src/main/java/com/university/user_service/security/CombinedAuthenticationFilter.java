package com.university.user_service.security;

import com.university.user_service.common.Role;
import com.university.user_service.common.Status;
import com.university.user_service.tenant.client.TenantClient;
import com.university.user_service.tenant.client.TenantDto;
import com.university.user_service.user.UserService;
import com.university.user_service.user.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kombinierter Authentication Filter für den Auth-Service.
 * 
 * Unterstützt zwei Authentifizierungsmethoden:
 * 1. Claims aus HTTP-Headern (X-User-Id, X-User-Email, X-User-Roles) - vom Gateway weitergeleitet
 * 2. Direkte JWT-Authentifizierung - wenn der Service direkt angesprochen wird
 * 
 * Priorisierung:
 * - Wenn X-User-Id Header vorhanden ist (Gateway-Quelle) → Claims aus Headers verwenden
 * - Wenn kein X-User-Id Header, aber Authorization Bearer Token vorhanden → JWT direkt validieren
 * 
 * Sicherheit:
 * - Bei Gateway-Authentifizierung wird das JWT nicht erneut validiert (bereits vom Gateway validiert)
 * - Bei direkter Authentifizierung wird das JWT vollständig validiert
 */
@Slf4j
public class CombinedAuthenticationFilter extends OncePerRequestFilter {

    private static final String GATEWAY_USER_ID_HEADER = "X-User-Id";
    private static final String GATEWAY_EMAIL_HEADER = "X-User-Email";
    private static final String GATEWAY_ROLES_HEADER = "X-User-Roles";
    private static final String GATEWAY_TENANT_IDS_HEADER = "X-User-Tenant-Ids";
    private static final String GATEWAY_STUDIENGAENGE_HEADER = "X-User-Studiengaenge";
    private static final String GATEWAY_MATRIKELNUMMER_HEADER = "X-User-Matrikelnummer";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserService userService;
    private final TenantClient tenantClient;

    public CombinedAuthenticationFilter(JwtService jwtService, UserService userService, TenantClient tenantClient) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.tenantClient = tenantClient;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Öffentliche Endpunkte nicht filtern
        return path.startsWith("/actuator") || 
               path.equals("/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userIdHeader = request.getHeader(GATEWAY_USER_ID_HEADER);
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

            // Priorisierung: Gateway-Claims (X-User-Id Header) vor JWT
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                log.debug("Gateway authentication detected (X-User-Id header present)");
                if (!authenticateFromGatewayHeaders(request, response)) {
                    return; // Authentication failed, response already sent
                }
            } else if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
                log.debug("Direct JWT authentication detected");
                if (!authenticateFromJwt(request, response)) {
                    return; // Authentication failed, response already sent
                }
            } else {
                log.debug("No authentication credentials found for path: {}", request.getRequestURI());
                // Lasse die Request durch - Spring Security entscheidet ob der Endpunkt öffentlich ist
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Authentication filter error: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication");
        } finally {
            SecurityUtils.clear();
        }
    }

    /**
     * Authentifizierung via Gateway-Headers (X-User-Id, X-User-Email, X-User-Roles, etc.).
     * Das JWT wurde bereits vom Gateway validiert, daher keine erneute Validierung.
     * 
     * @return true wenn erfolgreich, false wenn fehlgeschlagen (Response bereits gesendet)
     */
    private boolean authenticateFromGatewayHeaders(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userIdHeader = request.getHeader(GATEWAY_USER_ID_HEADER);
        String emailHeader = request.getHeader(GATEWAY_EMAIL_HEADER);
        String rolesHeader = request.getHeader(GATEWAY_ROLES_HEADER);
        String tenantIdsHeader = request.getHeader(GATEWAY_TENANT_IDS_HEADER);
        String studiengaengeHeader = request.getHeader(GATEWAY_STUDIENGAENGE_HEADER);
        String matrikelnummerHeader = request.getHeader(GATEWAY_MATRIKELNUMMER_HEADER);

        log.debug("Gateway claims - userId: {}, email: {}, roles: {}, tenants: {}, studiengaenge: {}", 
            userIdHeader, emailHeader, rolesHeader, tenantIdsHeader, studiengaengeHeader);

        if (emailHeader == null || emailHeader.isEmpty()) {
            log.warn("Gateway authentication failed: missing email header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid gateway claims");
            return false;
        }

        try {
            UUID userId = UUID.fromString(userIdHeader);

            // Rollen parsen
            Set<Role> roles = Set.of();
            if (rolesHeader != null && !rolesHeader.isEmpty()) {
                roles = Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Role::valueOf)
                        .collect(Collectors.toSet());
            }

            // Tenant IDs parsen
            Set<UUID> tenantIds = Set.of();
            if (tenantIdsHeader != null && !tenantIdsHeader.isEmpty()) {
                tenantIds = Arrays.stream(tenantIdsHeader.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());
            }

            // Studiengaenge parsen
            Set<String> studiengaenge = Set.of();
            if (studiengaengeHeader != null && !studiengaengeHeader.isEmpty()) {
                studiengaenge = Arrays.stream(studiengaengeHeader.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());
            }

            // Matrikelnummer parsen
            Long matrikelnummer = null;
            if (matrikelnummerHeader != null && !matrikelnummerHeader.isEmpty()) {
                try {
                    matrikelnummer = Long.parseLong(matrikelnummerHeader);
                } catch (NumberFormatException e) {
                    log.warn("Invalid matrikelnummer header: {}", matrikelnummerHeader);
                }
            }

            // User und Tenant validieren
            if (!validateUserAndTenants(userId, emailHeader, response)) {
                return false;
            }

            log.info("Gateway authentication successful for user: {}, roles: {}, tenants: {}", emailHeader, roles, tenantIds);
            setSecurityContext(userId, roles, emailHeader, tenantIds, studiengaenge, matrikelnummer);
            return true;

        } catch (IllegalArgumentException e) {
            log.warn("Gateway authentication failed: invalid header format - {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid gateway claims");
            return false;
        }
    }

    /**
     * Authentifizierung via direktem JWT Token.
     * Vollständige JWT-Validierung wird durchgeführt.
     * 
     * @return true wenn erfolgreich, false wenn fehlgeschlagen (Response bereits gesendet)
     */
    private boolean authenticateFromJwt(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        log.debug("JWT token present, validating...");

        if (!jwtService.validateToken(token)) {
            log.debug("JWT validation failed");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return false;
        }

        try {
            UUID userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);
            Set<Role> roles = jwtService.extractRoles(token);
            Set<UUID> tenantIds = jwtService.extractTenantIds(token);
            Set<String> studiengaenge = jwtService.extractStudiengaenge(token);
            Long matrikelnummer = jwtService.extractMatrikelnummer(token);

            // User und Tenant validieren
            if (!validateUserAndTenants(userId, email, response)) {
                return false;
            }

            if (roles == null || roles.isEmpty()) {
                log.warn("No roles found in JWT for user: {}", email);
                roles = Set.of();
            }

            log.info("JWT authentication successful for user: {}, roles: {}, tenants: {}", email, roles, tenantIds);
            setSecurityContext(userId, roles, email, tenantIds, studiengaenge, matrikelnummer);
            return true;

        } catch (Exception e) {
            log.error("Error processing JWT claims: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error processing token");
            return false;
        }
    }

    /**
     * Validiert ob der User existiert, aktiv ist und mindestens einen aktiven Tenant hat.
     * 
     * @return true wenn valide, false wenn nicht (Response bereits gesendet)
     */
    private boolean validateUserAndTenants(UUID userId, String email, HttpServletResponse response) throws IOException {
        User user = userService.findById(userId).orElse(null);

        if (user == null) {
            log.warn("Authentication rejected: user not found for id {}", userId);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return false;
        }

        if (user.getStatus() != Status.ACTIVE) {
            log.warn("Authentication rejected: user {} is {}", email, user.getStatus());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User account is not active");
            return false;
        }

        // Tenant-Validierung (falls User Tenants hat)
        if (user.getTenantIds() != null && !user.getTenantIds().isEmpty()) {
            boolean hasActiveTenant = false;
            for (UUID tenantId : user.getTenantIds()) {
                TenantDto tenant = tenantClient.findById(tenantId).orElse(null);
                if (tenant == null) {
                    log.warn("Authentication: tenant {} for user {} missing; ignoring", tenantId, email);
                    continue;
                }
                if (tenant.getStatus() == Status.ACTIVE) {
                    hasActiveTenant = true;
                } else {
                    log.warn("Authentication: tenant {} for user {} is {} and ignored", 
                            tenant.getIdentifier(), email, tenant.getStatus());
                }
            }

            if (!hasActiveTenant) {
                log.warn("Authentication rejected: no active tenant for user {}", email);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No active tenant");
                return false;
            }
        }

        return true;
    }

    /**
     * Setzt den SecurityContext für den authentifizierten User.
     */
    private void setSecurityContext(UUID userId, Set<Role> roles, String email, Set<UUID> tenantIds, Set<String> studiengaenge, Long matrikelnummer) {
        // SecurityUtils setzen (für Controller-Zugriff)
        SecurityUtils.setAuthentication(userId, roles, email, tenantIds, studiengaenge, matrikelnummer);

        // Spring Security Authentication setzen (für Authorization)
        var authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("SecurityContext set for user: {} with authorities: {}", email, authorities);
    }
}
