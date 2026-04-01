package com.university.gateway_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter für das Gateway, der JWT-Claims extrahiert und als HTTP-Header weiterleitet.
 * 
 * Funktionsweise:
 * 1. JWT aus Authorization Header extrahieren und validieren
 * 2. Claims (userId, email, roles) aus dem JWT extrahieren
 * 3. Claims als HTTP-Header (X-User-Id, X-User-Email, X-User-Roles) an Backend-Services weiterleiten
 * 
 * Vorteile:
 * - Backend-Services müssen JWT nicht erneut validieren (bereits vom Gateway validiert)
 * - Zentrale JWT-Validierung im Gateway
 * - Backend-Services können Claims aus Headers lesen (schneller als JWT-Parsing)
 */
@Component
public class JwtClaimsExtractionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtClaimsExtractionFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Key signingKey;

    public JwtClaimsExtractionFilter(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Öffentliche Endpoints ohne JWT-Validierung:
        // /api/auth/*: Login, Register
        // /actuator: Health-Check und Monitoring
        // /graphiql: GraphQL-Playground (nur für Entwicklung)
        // 
        // /graphql: WIRD validiert - Gateway extrahiert JWT-Claims und leitet als X-User-* Headers weiter
        return path.startsWith("/api/auth/") || 
               path.startsWith("/actuator") ||
               path.startsWith("/graphiql");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.debug("Missing or invalid Authorization header for path: {}", request.getRequestURI());
            sendErrorResponse(response, "Unauthorized", "Missing or invalid Authorization header");
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            // JWT parsen und validieren
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Claims als Header für ALLE authentifizierten Endpunkte weiterleiten
            HttpServletRequest wrappedRequest = new ClaimsHeaderRequestWrapper(request, claims);
            log.debug("JWT Claims extracted for {}: userId={}, email={}", 
                request.getRequestURI(), claims.getSubject(), claims.get("email"));
            filterChain.doFilter(wrappedRequest, response);

        } catch (ExpiredJwtException ex) {
            log.debug("JWT token expired: {}", ex.getMessage());
            sendErrorResponse(response, "Unauthorized", "JWT token has expired");
        } catch (JwtException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            sendErrorResponse(response, "Unauthorized", "Invalid JWT token");
        } catch (Exception ex) {
            log.error("Failed to process JWT: {}", ex.getMessage());
            sendErrorResponse(response, "Unauthorized", "Failed to process authentication");
        }
    }

    /**
     * Schreibt eine standardisierte JSON Error Response (wie AuthenticationEntryPoint im Service)
     */
    private void sendErrorResponse(HttpServletResponse response, String error, String message) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Request Wrapper, der die JWT-Claims als Header hinzufügt.
     * 
     * Mapping:
     * - sub (JWT Subject) -> X-User-Id
     * - email -> X-User-Email
     * - role -> X-User-Roles (komma-separiert)
     * - tenant_ids -> X-User-Tenant-Ids (komma-separiert)
     * - studiengaenge -> X-User-Studiengaenge (komma-separiert)
     * - matrikelnummer -> X-User-Matrikelnummer
     */
    private static class ClaimsHeaderRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        
        private final Claims claims;

        public ClaimsHeaderRequestWrapper(HttpServletRequest request, Claims claims) {
            super(request);
            this.claims = claims;
        }

        @Override
        public String getHeader(String name) {
            // Claims als Header bereitstellen
            switch (name) {
                case "X-User-Id":
                    // userId ist im JWT als "sub" (Subject) gespeichert
                    return claims.getSubject();
                case "X-User-Email":
                    return claims.get("email", String.class);
                case "X-User-Roles":
                    // Rollen sind im JWT als "role" (nicht "roles") gespeichert
                    Object rolesObj = claims.get("role");
                    if (rolesObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> rolesList = (List<String>) rolesObj;
                        return String.join(",", rolesList);
                    } else if (rolesObj instanceof String) {
                        return (String) rolesObj;
                    }
                    return null;
                case "X-User-Tenant-Ids":
                    Object tenantIdsObj = claims.get("tenant_ids");
                    if (tenantIdsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> tenantIdsList = (List<String>) tenantIdsObj;
                        return String.join(",", tenantIdsList);
                    } else if (tenantIdsObj instanceof String) {
                        return (String) tenantIdsObj;
                    }
                    return null;
                case "X-User-Studiengaenge":
                    Object studiengaengeObj = claims.get("studiengaenge");
                    if (studiengaengeObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> studiengaengeList = (List<String>) studiengaengeObj;
                        return String.join(",", studiengaengeList);
                    } else if (studiengaengeObj instanceof String) {
                        return (String) studiengaengeObj;
                    }
                    return null;
                case "X-User-Matrikelnummer":
                    Object matrikelnummerObj = claims.get("matrikelnummer");
                    if (matrikelnummerObj != null) {
                        return matrikelnummerObj.toString();
                    }
                    return null;
                default:
                    return super.getHeader(name);
            }
        }

        @Override
        public java.util.Enumeration<String> getHeaderNames() {
            java.util.Set<String> names = new java.util.HashSet<>();
            java.util.Enumeration<String> originalNames = super.getHeaderNames();
            while (originalNames.hasMoreElements()) {
                names.add(originalNames.nextElement());
            }
            // Füge Custom Header hinzu
            names.add("X-User-Id");
            names.add("X-User-Email");
            names.add("X-User-Roles");
            names.add("X-User-Tenant-Ids");
            names.add("X-User-Studiengaenge");
            names.add("X-User-Matrikelnummer");
            return java.util.Collections.enumeration(names);
        }

        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            if ("X-User-Id".equals(name) || "X-User-Email".equals(name) || "X-User-Roles".equals(name) ||
                "X-User-Tenant-Ids".equals(name) || "X-User-Studiengaenge".equals(name) || "X-User-Matrikelnummer".equals(name)) {
                String value = getHeader(name);
                if (value != null) {
                    return java.util.Collections.enumeration(java.util.Collections.singletonList(value));
                }
            }
            return super.getHeaders(name);
        }
    }
}
