package com.university.notenberechnung_service.controller;

import com.university.notenberechnung_service.dto.NotenEingabe;
import com.university.notenberechnung_service.model.ModulConfig;
import com.university.notenberechnung_service.security.JwtTenantService;
import com.university.notenberechnung_service.service.NotenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller für Notenberechnung und Modul-Verwaltung nach Prüfungsordnung (PO).
 * 
 * <p>Endpunkte:
 * <ul>
 *   <li>POST /api/{poId}/grades/calculate - Gewichtete Gesamtnote berechnen</li>
 *   <li>GET /api/{poId}/grades/student - Noten eines Studenten berechnen (via GraphQL)</li>
 *   <li>POST /api/{poId}/modules - Modul anlegen</li>
 *   <li>PUT /api/{poId}/modules/{modulId} - Modul aktualisieren</li>
 *   <li>PATCH /api/{poId}/modules/{modulId}/active - Modul aktivieren/deaktivieren</li>
 *   <li>GET /api/{poId}/modules - Alle Module einer PO abrufen</li>
 *   <li>DELETE /api/{poId}/modules/{modulId} - Modul löschen</li>
 * </ul>
 * 
 * <p>Die Prüfungsordnung (poId) bestimmt, welche Module und Gewichtungen verwendet werden.
 * 
 * @see NotenService
 */
@RestController
@RequestMapping("/api/{poId}")
@Slf4j
public class NotenController {

    private final NotenService notenService;
    private final JwtTenantService jwtTenantService;

    public NotenController(NotenService notenService, JwtTenantService jwtTenantService) {
        this.notenService = notenService;
        this.jwtTenantService = jwtTenantService;
    }

    // -------------------- Notenberechnung --------------------

    /**
     * Berechnet die gewichtete Gesamtnote basierend auf den übergebenen Modulnoten.
     * Erwartet eine Liste mit modulId (Integer) und note.
     * Nur Module aus der angegebenen Prüfungsordnung werden berücksichtigt.
     * Nur Students dürfen diese Methode aufrufen.
     */
    @PostMapping("/grades/calculate")
    public Map<String, Object> calculate(@PathVariable String poId,
                                         @RequestBody NotenEingabe eingabe,
                                         @RequestHeader("Authorization") String authorization) {
        var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
        double gesamt = notenService.berechneGesamtnote(poId, eingabe, tenantIds);
        return Map.of(
                "poId", poId,
                "gesamtNote", gesamt,
                "anzahlModule", eingabe.getNotenListe().size()
        );
    }

    /**
     * Berechnet die gewichtete Gesamtnote für einen Studierenden basierend auf GraphQL Query.
     * Ruft Noten vom Noten-Modul-Service ab und verwendet die Module aus dieser Prüfungsordnung.
     * JWT/Credentials werden an den Noten-Modul-Service weitergegeben.
     * Kann von Studenten und höheren Rollen aufgerufen werden.
     *
     * Query Parameter:
     * - matrikelnummer: Matrikelnummer des Studierenden
     *
     * GET /api/PO-2025-01/grades/student?matrikelnummer=12345678
     */
    @GetMapping("/grades/student")
    public reactor.core.publisher.Mono<Map<String, Object>> calculateForStudent(
            @PathVariable String poId,
            @RequestParam String matrikelnummer,
            @RequestHeader("Authorization") String authorization) {
        
        log.info("Berechne Noten für Student {} in PO {} (ASYNC)", matrikelnummer, poId);
        
        var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
        
        return notenService.berechneGesamtnoteViaGraphQLAsync(
                poId, matrikelnummer, authorization, tenantIds)
            .map(gesamt -> Map.of(
                    "poId", poId,
                    "matrikelnummer", matrikelnummer,
                    "gesamtNote", gesamt,
                    "status", "Berechnung erfolgreich"
            ));
    }

    // -------------------- Modul CRUD --------------------

    /**
     * Erstellt ein neues Modul für die angegebene Prüfungsordnung.
     * Die tenantId wird automatisch aus der Seed-Konfiguration gesetzt und ignoriert Client-Eingaben.
     * Body: { "modulId": 13, "modulName": "Neues Modul", "gewichtung": 2.0, "active": true }
     */
    @PostMapping("/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModulConfig createModule(@PathVariable String poId,
                                    @RequestBody ModulConfig modul,
                                    @RequestHeader("Authorization") String authorization) {
        var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
        boolean isAdmin = jwtTenantService.isAdminFromAuthorizationHeader(authorization);
        return notenService.legeModulAn(poId, modul, tenantIds, isAdmin);
    }

    /**
     * Aktualisiert ein bestehendes Modul (Name und/oder Gewichtung).
     * Die tenantId bleibt unverändert (immutable).
     * Body: { "modulName": "Neuer Name", "gewichtung": 3.0 }
     */
    @PutMapping("/modules/{modulId}")
    public ModulConfig updateModule(@PathVariable String poId,
                                    @PathVariable Integer modulId,
                                    @RequestBody ModulConfig modul,
                                    @RequestHeader("Authorization") String authorization) {
        var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
        boolean isAdmin = jwtTenantService.isAdminFromAuthorizationHeader(authorization);
        return notenService.bearbeiteModul(poId, modulId, modul, tenantIds, isAdmin);
    }

    /**
     * Setzt den aktiv/inaktiv Status eines Moduls.
     * Body: { "active": true } oder { "active": false }
     */
    @PatchMapping("/modules/{modulId}/active")
    public ModulConfig setModuleActive(@PathVariable String poId,
                                       @PathVariable Integer modulId,
                                       @RequestBody Map<String, Boolean> body,
                                       @RequestHeader("Authorization") String authorization) {
        Boolean active = body.get("active");
        if (active == null) {
            throw new IllegalArgumentException("'active' muss angegeben werden");
        }
        var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
        boolean isAdmin = jwtTenantService.isAdminFromAuthorizationHeader(authorization);
        return notenService.setzeModulAktiv(poId, modulId, active, tenantIds, isAdmin);
    }

    /**
     * Gibt alle Module für eine Prüfungsordnung zurück (inkl. inaktiver).
     * Die Modules sind mit ihren Tenant IDs verbunden.
     */
    @GetMapping("/modules")
    public List<ModulConfig> listModules(@PathVariable String poId,
                                         @RequestHeader("Authorization") String authorization) {
        var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
        boolean isAdmin = jwtTenantService.isAdminFromAuthorizationHeader(authorization);
        return notenService.alleModule(poId, tenantIds, isAdmin);
    }

    /**
     * Gibt nur aktive Module für eine Prüfungsordnung zurück.
     * Nützlich für Dropdown/Selection-Interfaces.
     */
    @GetMapping("/modules/active")
    public List<ModulConfig> listActiveModules(@PathVariable String poId,
                                               @RequestHeader("Authorization") String authorization) {
        var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
        boolean isAdmin = jwtTenantService.isAdminFromAuthorizationHeader(authorization);
        return notenService.aktiveModule(poId, tenantIds, isAdmin);
    }

    /**
     * Gibt ein einzelnes Modul mit allen Details (inklusive tenantId) zurück.
     */
    @GetMapping("/modules/{modulId}")
    public ModulConfig getModule(@PathVariable String poId,
                                 @PathVariable Integer modulId,
                                 @RequestHeader("Authorization") String authorization) {
        var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
        boolean isAdmin = jwtTenantService.isAdminFromAuthorizationHeader(authorization);
        return notenService.findeModul(poId, modulId, tenantIds, isAdmin);
    }

    /**
     * Löscht ein Modul aus einer Prüfungsordnung.
     */
    @DeleteMapping("/modules/{modulId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable String poId,
                             @PathVariable Integer modulId,
                             @RequestHeader("Authorization") String authorization) {
        var tenantIds = jwtTenantService.extractTenantIdsFromAuthorizationHeader(authorization);
        boolean isAdmin = jwtTenantService.isAdminFromAuthorizationHeader(authorization);
        notenService.loescheModul(poId, modulId, tenantIds, isAdmin);
    }
}
