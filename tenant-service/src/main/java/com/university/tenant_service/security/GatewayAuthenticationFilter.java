package com.university.tenant_service.security;

import com.university.tenant_service.common.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final String GATEWAY_USER_ID_HEADER = "X-User-Id";
    private static final String GATEWAY_EMAIL_HEADER = "X-User-Email";
    private static final String GATEWAY_ROLES_HEADER = "X-User-Roles";
    private static final String GATEWAY_TENANT_IDS_HEADER = "X-User-Tenant-Ids";
    private static final String GATEWAY_STUDIENGAENGE_HEADER = "X-User-Studiengaenge";
    private static final String GATEWAY_MATRIKELNUMMER_HEADER = "X-User-Matrikelnummer";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userIdHeader = request.getHeader(GATEWAY_USER_ID_HEADER);
            String emailHeader = request.getHeader(GATEWAY_EMAIL_HEADER);
            String rolesHeader = request.getHeader(GATEWAY_ROLES_HEADER);
            String tenantIdsHeader = request.getHeader(GATEWAY_TENANT_IDS_HEADER);
            String studiengaengeHeader = request.getHeader(GATEWAY_STUDIENGAENGE_HEADER);
            String matrikelnummerHeader = request.getHeader(GATEWAY_MATRIKELNUMMER_HEADER);

            if (userIdHeader != null || emailHeader != null || rolesHeader != null) {
                UUID userId = null;
                if (userIdHeader != null && !userIdHeader.isEmpty()) {
                    try {
                        userId = UUID.fromString(userIdHeader);
                    } catch (IllegalArgumentException e) {
                        log.debug("Invalid user id header: {}", userIdHeader);
                    }
                }

                Set<Role> roles = Set.of();
                if (rolesHeader != null && !rolesHeader.isEmpty()) {
                    roles = Arrays.stream(rolesHeader.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(r -> {
                                try {
                                    return Role.valueOf(r);
                                } catch (IllegalArgumentException e) {
                                    return null;
                                }
                            })
                            .filter(r -> r != null)
                            .collect(Collectors.toSet());
                }

                Set<UUID> tenantIds = Set.of();
                if (tenantIdsHeader != null && !tenantIdsHeader.isEmpty()) {
                    tenantIds = Arrays.stream(tenantIdsHeader.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(UUID::fromString)
                            .collect(Collectors.toSet());
                }

                Set<String> studiengaenge = Set.of();
                if (studiengaengeHeader != null && !studiengaengeHeader.isEmpty()) {
                    studiengaenge = Arrays.stream(studiengaengeHeader.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toSet());
                }

                Long matrikelnummer = null;
                if (matrikelnummerHeader != null && !matrikelnummerHeader.isEmpty()) {
                    try {
                        matrikelnummer = Long.parseLong(matrikelnummerHeader);
                    } catch (NumberFormatException e) {
                        log.debug("Invalid matrikelnummer header: {}", matrikelnummerHeader);
                    }
                }

                SecurityUtils.setAuthentication(userId, roles, emailHeader, tenantIds, studiengaenge, matrikelnummer);
            }

            filterChain.doFilter(request, response);
        } finally {
            SecurityUtils.clear();
        }
    }
}
