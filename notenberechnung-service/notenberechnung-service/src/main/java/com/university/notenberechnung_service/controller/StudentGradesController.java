package com.university.notenberechnung_service.controller;

import com.university.notenberechnung_service.security.JwtTenantService;
import com.university.notenberechnung_service.service.NotenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * REST Controller für Student-spezifische Notenabfragen.
 * 
 * <p>Endpunkte:
 * <ul>
 *   <li>GET /api/grades/student - Gesamtnote des authentifizierten Studenten berechnen</li>
 * </ul>
 * 
 * <p>Die Matrikelnummer wird automatisch aus dem JWT-Token extrahiert.
 * Verwendet reactive Programming (Mono) für asynchrone Verarbeitung.
 * 
 * @see NotenService
 * @see JwtTenantService
 */
@RestController
@RequestMapping("/api/grades")
@Slf4j
public class StudentGradesController {

    private final NotenService notenService;
    private final JwtTenantService jwtTenantService;

    public StudentGradesController(NotenService notenService, JwtTenantService jwtTenantService) {
        this.notenService = notenService;
        this.jwtTenantService = jwtTenantService;
    }

    /**
     * Berechnet die gewichtete Gesamtnote für den authentifizierten Student (ASYNC/REACTIVE).
     * Liest die Matrikelnummer aus dem JWT Token aus (aus den Claims).
     * Ruft Noten vom Noten-Modul-Service ab und berechnet den Durchschnitt basierend auf allen Modulen.
     * JWT/Credentials werden an den Noten-Modul-Service weitergegeben.
     *
     * GET /api/grades/student
     * Header: Authorization: Bearer YOUR_JWT_TOKEN
     */
    @GetMapping("/student")
    public Mono<Map<String, Object>> calculateForStudent(
            @RequestHeader("Authorization") String authorization) {
        
        log.info("Berechne Noten für authentifizierten Student (ASYNC)");
        
        try {
            var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
            // Matrikelnummer wird aus dem JWT Token extrahiert
            String matrikelnummer = jwtTenantService.extractMatrikelnummerFromAuthorizationHeader(authorization);
            
            log.debug("Student-Anfrage: matrikelnummer={}, tenantIds={}", matrikelnummer, tenantIds);
            
            // Verwendet alle verfügbaren Module (über alle POs hinweg) - ASYNC VERSION
            return notenService.berechneGesamtnoteViaGraphQLAsync(
                    null, matrikelnummer, authorization, tenantIds)
                .map(gesamt -> {
                    log.info("Berechnung erfolgreich für matrikelnummer={}, gesamtNote={}", matrikelnummer, gesamt);
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("matrikelnummer", matrikelnummer);
                    result.put("gesamtNote", gesamt);
                    result.put("status", "Berechnung erfolgreich");
                    return result;
                })
                .doOnError(error -> log.error("Fehler bei Berechnung für Student: {}", error.getMessage(), error));
        } catch (Exception e) {
            log.error("Fehler in StudentGradesController: {}", e.getMessage(), e);
            return Mono.error(e);
        }
    }
}
