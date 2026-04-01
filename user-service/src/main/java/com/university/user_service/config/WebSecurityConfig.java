package com.university.user_service.config;

import com.university.user_service.security.CombinedAuthenticationFilter;
import com.university.user_service.security.JwtService;
import com.university.user_service.tenant.client.TenantClient;
import com.university.user_service.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtService jwtService;
    private final UserService userService;
    private final TenantClient tenantClient;
    private final CorsConfigurationSource corsConfigurationSource;

    public WebSecurityConfig(JwtService jwtService, UserService userService, TenantClient tenantClient, CorsConfigurationSource corsConfigurationSource) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.tenantClient = tenantClient;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Kombinierter Authentication Filter, der sowohl Gateway-Claims (X-User-Id Headers)
     * als auch direkte JWT-Authentifizierung unterstützt.
     */
    @Bean
    public CombinedAuthenticationFilter combinedAuthenticationFilter() {
        return new CombinedAuthenticationFilter(jwtService, userService, tenantClient);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Access denied\"}");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, 
                                          CombinedAuthenticationFilter combinedAuthenticationFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                // Security Headers (ohne HSTS für Entwicklung)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())  // Verhindert Clickjacking (iframe-Einbettung)
                        .xssProtection(xss -> xss.disable())  // XSS-Protection ist veraltet, CSP ist der moderne Ersatz
                        .contentTypeOptions(Customizer.withDefaults())  // Verhindert MIME-Sniffing
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))  // Nur eigene Ressourcen erlaubt
                        
                )
                .authorizeHttpRequests(authz -> authz
                        // Pre-flight requests (OPTIONS) und HEAD requests müssen immer erlaubt sein
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        //.requestMatchers(HttpMethod.HEAD, "/**").permitAll()
                        // Public endpoints
                        .requestMatchers("/api/v1/users/internal/**").permitAll()
                        // Actuator & health checks
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        // User endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()  // Alle authentifizierten Benutzer
                        // Hierarchischer Endpunkt: erlaubt unauthentifizierte Anfragen, damit Controller 404 zurückgeben kann
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/fachbereiche/*/studiengaenge/*").hasAnyRole("ADMIN", "PRUEFUNGSAMT")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").hasAnyRole("ADMIN", "PRUEFUNGSAMT")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("ADMIN", "PRUEFUNGSAMT")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/activate").hasAnyRole("ADMIN", "PRUEFUNGSAMT")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/deactivate").hasAnyRole("ADMIN", "PRUEFUNGSAMT")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasAnyRole("ADMIN", "PRUEFUNGSAMT","STUDENT","LEHRENDER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("ADMIN", "PRUEFUNGSAMT","STUDENT","LEHRENDER")
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                // CombinedAuthenticationFilter für JWT und Gateway-Claims Authentifizierung
                .addFilterBefore(combinedAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
