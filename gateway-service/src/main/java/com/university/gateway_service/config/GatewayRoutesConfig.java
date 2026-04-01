package com.university.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.rewritePath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

/**
 * Gateway-Routen-Konfiguration für Spring Cloud Gateway MVC.
 * 
 * Routing-Übersicht:
 * - Gateway (8080) /api/grades/** → Notenberechnung-Service (8083)
 * - Gateway (8080) /api/{poId}/grades/** → Notenberechnung-Service (8083)
 * - Gateway (8080) /api/{poId}/modules/** → Notenberechnung-Service (8083)
 * - Gateway (8080) /graphql → Noten-Modulverwaltung-Service (8082) /graphql
 * - Gateway (8080) /api/tenants/** → Tenant-Service (8084) /api/v1/tenants/**
 * - Gateway (8080) /api/auth/** → Auth-Service (8085) /api/v1/auth/**
 * - Gateway (8080) /api/** → User-Service (8081) /api/v1/**
 * 
 * Vorteil: Alle aktuellen und zukünftigen Endpunkte werden automatisch geroutet,
 * ohne dass jede Route einzeln konfiguriert werden muss.
 * 
 * WICHTIG: Die @Order Annotation stellt sicher, dass spezifischere Routen
 * vor allgemeineren Routen geprüft werden.
 */
@Configuration
public class GatewayRoutesConfig {

            private static final String USER_SERVICE_URI =
                System.getenv().getOrDefault("USER_SERVICE_URL", "http://localhost:8081");
            private static final String NOTEN_MODUL_SERVICE_URI =
                System.getenv().getOrDefault("NOTEN_MODUL_SERVICE_URL", "http://localhost:8082");
            private static final String NOTENBERECHNUNG_SERVICE_URI =
                System.getenv().getOrDefault("NOTENBERECHNUNG_SERVICE_URL", "http://localhost:8083");
            private static final String TENANT_SERVICE_URI =
                System.getenv().getOrDefault("TENANT_SERVICE_URL", "http://localhost:8084");
            private static final String AUTH_SERVICE_URI =
                System.getenv().getOrDefault("AUTH_SERVICE_URL", "http://localhost:8085");

    /**
     * Route für Noten-Modulverwaltung-Service (GraphQL):
     * /graphql → Noten-Modul-Service:8082/graphql
     */
    @Bean
    @Order(1)
    public RouterFunction<ServerResponse> notenModulServiceRoute() {
        return route("noten-modul-service")
                .route(request -> request.path().equals("/graphql") || 
                                  request.path().startsWith("/graphiql"),
                       http(NOTEN_MODUL_SERVICE_URI))
                .build();
    }

    /**
     * Route für Notenberechnung-Service (REST):
     * /api/grades/** → Notenberechnung-Service:8083
     * /api/{poId}/grades/** → Notenberechnung-Service:8083
     * /api/{poId}/modules/** → Notenberechnung-Service:8083
     */
    @Bean
    @Order(2)
    public RouterFunction<ServerResponse> notenberechnungServiceRoute() {
        return route("notenberechnung-service")
                .route(request -> {
                    String path = request.path();
                    // /api/grades/** für StudentGradesController
                    if (path.startsWith("/api/grades")) {
                        return true;
                    }
                    // /api/{poId}/grades/** und /api/{poId}/modules/** für NotenController
                    if (path.matches("/api/[^/]+/grades.*") || path.matches("/api/[^/]+/modules.*")) {
                        return true;
                    }
                    return false;
                }, http(NOTENBERECHNUNG_SERVICE_URI))
                .build();
    }

    /**
         * Route für Tenant-Service (REST):
         * /api/tenants/** → Tenant-Service:8084/api/v1/tenants/**
         */
        @Bean
        @Order(3)
        public RouterFunction<ServerResponse> tenantServiceRoute() {
         return route("tenant-service")
                  .route(request -> request.path().startsWith("/api/tenants"),
                  http(TENANT_SERVICE_URI))
                  .filter(rewritePath("/api/tenants(?<segment>/?.*)", "/api/v1/tenants${segment}"))
              .build();
        }

        /**
     * Generische Route für alle User-Tenant-Auth-Service Endpunkte:
     * /api/** → /api/v1/**
     * 
     * Matched alle Pfade die mit /api/ beginnen und fügt automatisch /v1/ ein.
     * Funktioniert für alle HTTP-Methoden (GET, POST, PUT, PATCH, DELETE).
     */
    @Bean
    @Order(4)
    public RouterFunction<ServerResponse> authServiceRoute() {
        return route("auth-service")
                .route(request -> request.path().startsWith("/api/auth"),
                       http(AUTH_SERVICE_URI))
                .filter(rewritePath("/api/auth(?<segment>/?.*)", "/api/v1/auth${segment}"))
                .build();
    }

    /**
     * Generische Route für alle User-Service Endpunkte:
     * /api/** → /api/v1/**
     * 
     * Matched alle Pfade die mit /api/ beginnen und fügt automatisch /v1/ ein.
     * Funktioniert für alle HTTP-Methoden (GET, POST, PUT, PATCH, DELETE).
     */
    @Bean
    @Order(5)
    public RouterFunction<ServerResponse> userServiceRoute() {
        return route("user-service")
                .route(request -> request.path().startsWith("/api/"), 
                       http(USER_SERVICE_URI))
                .filter(rewritePath("/api/(?<segment>.*)", "/api/v1/${segment}"))
                .build();
    }
}
