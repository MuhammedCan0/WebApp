package com.university.user_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service für JWT (JSON Web Token) Operationen.
 * 
 * <p>Verantwortlich für:
 * <ul>
 *   <li>Token-Generierung mit User-Claims (userId, email, roles, tenants, studiengaenge)</li>
 *   <li>Token-Validierung und Signaturprüfung</li>
 *   <li>Extraktion von Claims aus validen Tokens</li>
 * </ul>
 * 
 * <p>Sicherheitshinweise:
 * <ul>
 *   <li>Das JWT-Secret muss in Produktion über Umgebungsvariable (JWT_SECRET) gesetzt werden</li>
 *   <li>Tokens haben eine konfigurierbare Ablaufzeit (jwt.expiration-ms)</li>
 *   <li>Verwendeter Algorithmus: HS256 (HMAC-SHA256)</li>
 * </ul>
 * 
 * @see JwtProperties
 * @see com.university.user_service.security.CombinedAuthenticationFilter
 */
@Slf4j
@Service
public class JwtService {

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(UUID userId, String email, java.util.Set<com.university.user_service.common.Role> roles, java.util.Set<String> studiengaenge, java.util.Set<UUID> tenantIds, Long matrikelnummer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", roles);
        claims.put("studiengaenge", studiengaenge);  // Changed from studiengang to studiengaenge
        claims.put("tenant_ids", tenantIds);  // Changed from tenant_id to tenant_ids
        claims.put("email", email);
        claims.put("matrikelnummer", matrikelnummer);

        return createToken(claims, userId.toString());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public UUID extractUserId(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return (String) claims.get("email");
    }

    public java.util.Set<com.university.user_service.common.Role> extractRoles(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object roleObj = claims.get("role");
        
        if (roleObj == null) {
            log.warn("No roles found in JWT token");
            return java.util.Collections.emptySet();
        }
        
        try {
            // JWT serializes Set as List, so we need to convert
            if (roleObj instanceof java.util.Collection<?>) {
                java.util.Collection<?> roleCollection = (java.util.Collection<?>) roleObj;
                return roleCollection.stream()
                        .map(r -> {
                            if (r instanceof String) {
                                return com.university.user_service.common.Role.valueOf((String) r);
                            } else if (r instanceof com.university.user_service.common.Role) {
                                return (com.university.user_service.common.Role) r;
                            }
                            return null;
                        })
                        .filter(r -> r != null)
                        .collect(java.util.stream.Collectors.toSet());
            } else if (roleObj instanceof String) {
                return java.util.Set.of(com.university.user_service.common.Role.valueOf((String) roleObj));
            }
            
            log.warn("Unable to parse roles from JWT: {}", roleObj);
            return java.util.Collections.emptySet();
        } catch (Exception e) {
            log.error("Error extracting roles from JWT: {}", e.getMessage(), e);
            return java.util.Collections.emptySet();
        }
    }

    public java.util.Set<UUID> extractTenantIds(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object tenantIdsObj = claims.get("tenant_ids");
        
        if (tenantIdsObj == null) {
            return java.util.Collections.emptySet();
        }
        
        try {
            if (tenantIdsObj instanceof java.util.Collection<?>) {
                java.util.Collection<?> tenantCollection = (java.util.Collection<?>) tenantIdsObj;
                return tenantCollection.stream()
                        .map(t -> {
                            if (t instanceof String) {
                                return UUID.fromString((String) t);
                            } else if (t instanceof UUID) {
                                return (UUID) t;
                            }
                            return null;
                        })
                        .filter(t -> t != null)
                        .collect(java.util.stream.Collectors.toSet());
            } else if (tenantIdsObj instanceof String) {
                return java.util.Set.of(UUID.fromString((String) tenantIdsObj));
            }
            return java.util.Collections.emptySet();
        } catch (Exception e) {
            log.warn("Error extracting tenant IDs from JWT: {}", e.getMessage());
            return java.util.Collections.emptySet();
        }
    }

    public java.util.Set<String> extractStudiengaenge(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object studiengaengeObj = claims.get("studiengaenge");
        
        if (studiengaengeObj == null) {
            return java.util.Collections.emptySet();
        }
        
        try {
            if (studiengaengeObj instanceof java.util.Collection<?>) {
                java.util.Collection<?> sgCollection = (java.util.Collection<?>) studiengaengeObj;
                return sgCollection.stream()
                        .filter(s -> s instanceof String)
                        .map(s -> (String) s)
                        .collect(java.util.stream.Collectors.toSet());
            } else if (studiengaengeObj instanceof String) {
                return java.util.Set.of((String) studiengaengeObj);
            }
            return java.util.Collections.emptySet();
        } catch (Exception e) {
            log.warn("Error extracting Studiengaenge from JWT: {}", e.getMessage());
            return java.util.Collections.emptySet();
        }
    }

    public Long extractMatrikelnummer(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object matrikelnummer = claims.get("matrikelnummer");
        if (matrikelnummer instanceof Number) {
            return ((Number) matrikelnummer).longValue();
        }
        return null;
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
