package com.university.notenberechnung_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service zur Extraktion und Validierung von JWT-Claims.
 *
 * <p>Verantwortlich für:
 * <ul>
 *   <li>Extraktion der Tenant-IDs aus dem JWT-Token</li>
 *   <li>Prüfung der Admin-Rolle im JWT-Token</li>
 *   <li>Extraktion der Matrikelnummer aus dem JWT-Token</li>
 * </ul>
 *
 * <p>Das JWT wird mit dem konfigurierten Secret (HMAC-SHA) signiert und verifiziert.
 * Admin-Tokens ohne Tenant-Einschränkung erhalten Zugriff auf alle Prüfungsordnungen.
 *
 * @see JwtProperties
 */
@Slf4j
@Service
public class JwtTenantService {

    private final JwtProperties jwtProperties;

    public JwtTenantService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public Set<UUID> extractTenantIdsFromAuthorizationHeader(String authorizationHeader) {
        return extractTenantIds(extractTokenFromHeader(authorizationHeader));
    }

    public boolean isAdminFromAuthorizationHeader(String authorizationHeader) {
        return isAdmin(extractTokenFromHeader(authorizationHeader));
    }

    public String extractMatrikelnummerFromAuthorizationHeader(String authorizationHeader) {
        return extractMatrikelnummer(extractTokenFromHeader(authorizationHeader));
    }

    /**
     * Extrahiert und validiert den Bearer-Token aus dem Authorization-Header.
     * Wirft eine {@link ResponseStatusException} bei fehlendem oder leerem Token.
     */
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization Header 'Bearer <token>' wird benötigt");
        }
        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token ist leer");
        }
        return token;
    }

    public Set<UUID> extractTenantIds(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            boolean isAdmin = hasAdminRole(claims);

            Object tenantObj = claims.get("tenant_ids");
            if (tenantObj == null) {
                if (isAdmin) {
                    // Admin ohne Tenant-Einschränkung: leeres Set bedeutet "alle erlaubt"
                    log.debug("Admin-Token ohne tenant_ids verwendet – globaler Zugriff erlaubt");
                    return Collections.emptySet();
                }
                log.warn("Kein tenant_ids Claim im JWT vorhanden");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Tenant im Token hinterlegt");
            }

            if (tenantObj instanceof Collection<?>) {
                Collection<?> col = (Collection<?>) tenantObj;
                return col.stream()
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());
            }

            // Einzelner Wert
            String single = tenantObj.toString().trim();
            if (single.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Tenant im Token hinterlegt");
            }
            return Collections.singleton(UUID.fromString(single));

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Fehler beim Lesen der tenant_ids aus dem JWT: {}", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token ist ungültig oder abgelaufen");
        }
    }

    public boolean isAdmin(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return hasAdminRole(claims);
        } catch (Exception ex) {
            log.error("Fehler beim Prüfen der Admin-Rolle im JWT: {}", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token ist ungültig oder abgelaufen");
        }
    }

    public String extractMatrikelnummer(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object matrikelnummerObj = claims.get("matrikelnummer");
            if (matrikelnummerObj == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Matrikelnummer nicht im JWT vorhanden");
            }

            return String.valueOf(matrikelnummerObj);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Fehler beim Lesen der Matrikelnummer aus dem JWT: {}", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT Token ist ungültig oder abgelaufen");
        }
    }

    private boolean hasAdminRole(Claims claims) {
        Object roleObj = claims.get("role");
        if (roleObj == null) {
            return false;
        }

        if (roleObj instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) roleObj;
            return col.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(String.valueOf(r)));
        }

        return String.valueOf(roleObj).contains("ADMIN");
    }
}
