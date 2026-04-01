package de.frauas.notenmodulservice.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kombinierter GraphQL-Interceptor für Authentication.
 * 
 * Unterstützt zwei Authentifizierungsmethoden:
 * 1. Claims aus HTTP-Headern (X-User-Id, X-User-Email, X-User-Roles, etc.) - vom Gateway weitergeleitet
 * 2. Direkte JWT-Authentifizierung - wenn der Service direkt angesprochen wird
 * 
 * Priorisierung:
 * - Wenn X-User-Id Header vorhanden ist (Gateway-Quelle) → Claims aus Headers verwenden
 * - Wenn kein X-User-Id Header, aber Authorization Bearer Token vorhanden → JWT direkt dekodieren
 * 
 * Sicherheit:
 * - Bei Gateway-Authentifizierung wird das JWT nicht erneut validiert (bereits vom Gateway validiert)
 * - Bei direkter Authentifizierung wird das JWT-Payload dekodiert (Signaturprüfung im Gateway)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtGraphQlInterceptor implements WebGraphQlInterceptor {

    private static final String GATEWAY_USER_ID_HEADER = "X-User-Id";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String userIdHeader = request.getHeaders().getFirst(GATEWAY_USER_ID_HEADER);
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        JwtUser user = null;

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("JWT-Authentifizierung erkannt");
            String token = authHeader.substring(BEARER_PREFIX.length());
            user = decodeToken(token);
        } else if (userIdHeader != null && !userIdHeader.isEmpty()) {
            log.warn("Gateway-Header ohne Authorization gefunden - Anfrage wird nicht authentifiziert");
        } else {
            log.debug("Keine Authentifizierungs-Credentials gefunden, JwtUser wird nicht gesetzt");
        }

        if (user != null) {
            final JwtUser finalUser = user;
            request.configureExecutionInput((executionInput, builder) ->
                    builder.graphQLContext(ctx -> ctx.put("jwtUser", finalUser)).build()
            );
        }

        return chain.next(request);
    }

    /**
     * Validiert das JWT-Token und erstellt einen JwtUser.
     */
    private JwtUser decodeToken(String token) {
        try {
            Claims claims = jwtService.parseAndValidate(token);
            return buildUserFromClaims(claims);
        } catch (Exception ex) {
            log.warn("Fehler beim Dekodieren des JWT", ex);
            return null;
        }
    }

    private JwtUser buildUserFromClaims(Claims claims) {
        JwtUser user = new JwtUser();
        user.setSubject(claims.getSubject());
        user.setEmail(claims.get("email", String.class));

        Object rolesObj = claims.get("role");
        List<String> roles = extractStringList(rolesObj);
        user.setRoles(roles);

        Object tenantIdsObj = claims.get("tenant_ids");
        List<UUID> tenantIds = extractStringList(tenantIdsObj).stream()
                .map(UUID::fromString)
                .toList();
        user.setTenantIds(tenantIds);

        Object matrikelnummerObj = claims.get("matrikelnummer");
        if (matrikelnummerObj != null) {
            try {
                user.setMatrikelnummer(Integer.parseInt(matrikelnummerObj.toString()));
            } catch (NumberFormatException ex) {
                log.warn("Ungültige Matrikelnummer im JWT: {}", matrikelnummerObj);
            }
        }

        Object studiengaengeObj = claims.get("studiengaenge");
        user.setStudiengaenge(extractStringList(studiengaengeObj));
        return user;
    }

    private List<String> extractStringList(Object value) {
        if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            return str.isEmpty() ? Collections.emptyList() : List.of(str);
        }
        return Collections.emptyList();
    }
}