package com.university.notenberechnung_service.service;

import com.university.notenberechnung_service.dto.GraphQLNoteResponse;
import com.university.notenberechnung_service.dto.NotenEingabe;
import com.university.notenberechnung_service.model.ModulConfig;
import com.university.notenberechnung_service.repository.ModulGewichtungsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service für die Berechnung von gewichteten Gesamtnoten.
 * 
 * <p>Verantwortlich für:
 * <ul>
 *   <li>Berechnung der gewichteten Gesamtnote basierend auf Modulnoten</li>
 *   <li>Abruf von Noten via GraphQL vom Noten-Modul-Service</li>
 *   <li>CRUD-Operationen für Module pro Prüfungsordnung (PO)</li>
 *   <li>Tenant-basierte Zugriffskontrolle auf Prüfungsordnungen</li>
 * </ul>
 * 
 * <p>Berechnungslogik:
 * <ul>
 *   <li>Gewichtete Durchschnittsberechnung: Σ(Note × Gewicht) / Σ(Gewicht)</li>
 *   <li>Bei mehreren Noten pro Modul wird der Durchschnitt gebildet</li>
 *   <li>Nur aktive Module werden in die Berechnung einbezogen</li>
 *   <li>Ergebnis wird auf eine Nachkommastelle gerundet</li>
 * </ul>
 * 
 * <p>Notenbereich: 1.0 (sehr gut) bis 5.0 (nicht bestanden)
 * 
 * @see GraphQLClientService
 * @see ModulGewichtungsRepository
 * @see com.university.notenberechnung_service.controller.NotenController
 */
@Service
@Slf4j
public class NotenService {

    /** Minimaler Notenwert (sehr gut) */
    private static final double MIN_NOTE = 1.0;
    
    /** Maximaler Notenwert (nicht bestanden) */
    private static final double MAX_NOTE = 5.0;

    private final ModulGewichtungsRepository repository;
    private final GraphQLClientService graphQLClientService;

    public NotenService(ModulGewichtungsRepository repository, 
                       GraphQLClientService graphQLClientService) {
        this.repository = repository;
        this.graphQLClientService = graphQLClientService;
    }

    // -------------------- Berechnung mit GraphQL Integration --------------------

    /**
     * Berechnet die gewichtete Gesamtnote basierend auf den übergebenen Modulnoten.
     * Nur aktive Module werden in die Berechnung einbezogen.
     * Diese Methode darf nur von Students aufgerufen werden.
     */
    public double berechneGesamtnote(String poId, NotenEingabe eingabe, Set<UUID> tenantIds) {
        log.info("Starte Notenberechnung für PO: {}", poId);
        log.debug("Anzahl Tenant-IDs: {}", tenantIds != null ? tenantIds.size() : 0);
        
        ensureAccessToPo(poId, tenantIds);
        if (eingabe == null || eingabe.getNotenListe() == null || eingabe.getNotenListe().isEmpty()) {
            log.warn("Notenberechnung abgebrochen: Notenliste ist leer für PO {}", poId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grades/notenListe ist leer");
        }

        log.debug("Verarbeite {} Noten für PO {}", eingabe.getNotenListe().size(), poId);
        Map<Integer, ModulConfig> store = repository.getModulesForPo(poId);
        log.debug("Gefundene Module für PO {}: {}", poId, store.size());

        // Gruppiere Noten pro Modul-ID (mehrfaches Modul -> Mittelwert)
        Map<Integer, List<Double>> notenProModul = new HashMap<>();
        for (NotenEingabe.EinzelNote n : eingabe.getNotenListe()) {
            if (n.getModulId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modulId fehlt in einer Note");
            }

            double note = n.getNote();
            if (Double.isNaN(note) || note < MIN_NOTE || note > MAX_NOTE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "ungültige note: " + note + " (erlaubt: " + MIN_NOTE + "–" + MAX_NOTE + ")");
            }

            notenProModul.computeIfAbsent(n.getModulId(), k -> new ArrayList<>()).add(note);
        }

        double summeProdukt = 0.0;
        double summeGewichte = 0.0;

        for (Map.Entry<Integer, List<Double>> entry : notenProModul.entrySet()) {
            Integer modulId = entry.getKey();
            ModulConfig cfg = store.get(modulId);
            
            if (cfg == null) {
                log.warn("Modul nicht gefunden: modulId={}, PO={}", modulId, poId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Modul nicht gefunden für modulId: " + modulId + " (PO=" + poId + "). " +
                        "Bitte überprüfen Sie, dass die Modul-ID im Berechnungs-Service konfiguriert ist.");
            }

            // Prüfe ob Modul aktiv ist
            if (!cfg.isActive()) {
                log.warn("Inaktives Modul verwendet: modulId={}, PO={}", modulId, poId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Modul ist inaktiv und kann nicht bewertet werden: " + modulId + " (PO=" + poId + ")");
            }

            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            log.debug("Modul {}: Durchschnitt={}, Gewichtung={}", modulId, avg, cfg.getGewichtung());
            summeProdukt += avg * cfg.getGewichtung();
            summeGewichte += cfg.getGewichtung();
        }

        if (summeGewichte == 0.0) {
            log.error("Berechnung fehlgeschlagen: Summe der Gewichte ist 0 für PO {}", poId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Summe der Gewichte ist 0");
        }

        double endNote = summeProdukt / summeGewichte;
        double gerundeteNote = Math.round(endNote * 10.0) / 10.0;
        log.info("Notenberechnung abgeschlossen für PO {}: Gesamtnote={}", poId, gerundeteNote);
        return gerundeteNote; // 1 Nachkommastelle
    }

    /**
     * Berechnet die gewichtete Gesamtnote basierend auf GraphQL Abfrage beim Noten-Modul-Service (ASYNC/REACTIVE VERSION)
     * Ruft notenvomstudent Query ab und verwendet die Module vom lokalen Repository
     * Kann von Students und höheren Rollen aufgerufen werden.
     * WICHTIG: Diese Methode ist 100% reactive - KEIN .block() Aufrufe!
     *
     * @param poId Prüfungsordnung (kann null sein, dann werden alle Module verwendet)
     * @param matrikelnummer Matrikelnummer des Studierenden
     * @param authorizationHeader JWT Authorization Header
     * @param tenantIds Tenant IDs des authentifizierten Benutzers
     * @return Mono<Double> mit der berechneten Gesamtnote
     */
    public reactor.core.publisher.Mono<Double> berechneGesamtnoteViaGraphQLAsync(String poId, String matrikelnummer, 
                                               String authorizationHeader,
                                               Set<UUID> tenantIds) {
        try {
            // Wenn poId null ist, keine Zugriffsprüfung durchführen (alle Module erlaubt)
            if (poId != null) {
                ensureAccessToPo(poId, tenantIds);
            }
            
            if (matrikelnummer == null || matrikelnummer.isBlank()) {
                return reactor.core.publisher.Mono.error(
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "matrikelnummer ist erforderlich"));
            }
        } catch (Exception e) {
            return reactor.core.publisher.Mono.error(e);
        }

        // Hole Noten vom Noten-Modul-Service via GraphQL (ASYNC - returns Mono)
        return graphQLClientService.getNotenVonStudent(matrikelnummer, authorizationHeader)
                .flatMap(notenVomService -> {
                    try {
                        if (notenVomService.isEmpty()) {
                            return reactor.core.publisher.Mono.error(
                                new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                "Keine Noten gefunden für Studierenden: " + matrikelnummer));
                        }

                        // Hole konfigurierte Module für diese Prüfungsordnung
                        // Wenn poId null ist, nutze alle Module von allen Prüfungsordnungen
                        Map<Integer, ModulConfig> store;
                        if (poId != null) {
                            store = repository.getModulesForPo(poId);
                        } else {
                            // Hole alle Module aus allen Prüfungsordnungen und merge sie
                            store = new HashMap<>();
                            store.putAll(repository.getModulesForPo("Luftverkehrsmanagement_-_Aviation_Management_dual_BA"));
                            store.putAll(repository.getModulesForPo("Accounting_and_Finance_MA"));
                            store.putAll(repository.getModulesForPo("Informatik_BA"));
                        }

                        // Gruppiere Noten pro Modul-ID
                        Map<Integer, List<Double>> notenProModul = new HashMap<>();
                        for (GraphQLNoteResponse.GraphQLNote note : notenVomService) {
                            if (note.getModulId() == null) {
                                log.warn("Note ohne modulId: {}", note.getId());
                                continue;
                            }

                            if (note.getNote() == null) {
                                log.debug("Note mit null-Wert für modulId: {} wird ignoriert", note.getModulId());
                                continue;
                            }

                            double noteWert = note.getNote();
                            if (Double.isNaN(noteWert) || noteWert < MIN_NOTE || noteWert > MAX_NOTE) {
                                log.warn("Ungültige Note {} für modulId: {}", noteWert, note.getModulId());
                                continue;
                            }

                            notenProModul.computeIfAbsent(note.getModulId(), k -> new ArrayList<>()).add(noteWert);
                        }

                        if (notenProModul.isEmpty()) {
                            return reactor.core.publisher.Mono.error(
                                new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                "Keine gültigen Noten für Studierenden: " + matrikelnummer));
                        }

                        double summeProdukt = 0.0;
                        double summeGewichte = 0.0;
                        List<String> warningMessages = new ArrayList<>();

                        for (Map.Entry<Integer, List<Double>> entry : notenProModul.entrySet()) {
                            Integer modulId = entry.getKey();
                            ModulConfig cfg = store.get(modulId);
                            
                            if (cfg == null) {
                                String warningMsg = "Modul-ID " + modulId + " nicht im Berechnungs-Service konfiguriert (PO=" + poId + 
                                                    "). Diese Note wird nicht berücksichtigt.";
                                log.warn(warningMsg);
                                warningMessages.add(warningMsg);
                                continue;
                            }

                            // Prüfe ob Modul aktiv ist
                            if (!cfg.isActive()) {
                                String warningMsg = "Modul " + modulId + " ist inaktiv und wird nicht berücksichtigt.";
                                log.warn(warningMsg);
                                warningMessages.add(warningMsg);
                                continue;
                            }

                            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                            summeProdukt += avg * cfg.getGewichtung();
                            summeGewichte += cfg.getGewichtung();
                        }

                        if (summeGewichte == 0.0) {
                            return reactor.core.publisher.Mono.error(
                                new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                                "Keine aktiven Module für die Berechnung vorhanden (PO=" + poId + ")"));
                        }

                        double endNote = summeProdukt / summeGewichte;
                        
                        if (!warningMessages.isEmpty()) {
                            log.info("Berechnung für {} mit Warnings abgeschlossen: {}", matrikelnummer, warningMessages);
                        }

                        double result = Math.round(endNote * 10.0) / 10.0; // 1 Nachkommastelle
                        return reactor.core.publisher.Mono.just(result);
                    } catch (Exception e) {
                        return reactor.core.publisher.Mono.error(e);
                    }
                });
    }

    /**
     * Legt ein neues Modul an.
     * Das Modul wird für die angegebene Prüfungsordnung erstellt.
     */
    public ModulConfig legeModulAn(String poId, ModulConfig modul, Set<UUID> tenantIds, boolean isAdmin) {
        ensureAccessToPo(poId, tenantIds, isAdmin);
        if (modul == null || modul.getModulId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "modulId fehlt");
        }

        Integer modulId = modul.getModulId();
        
        // Prüfe ob Modul bereits existiert
        if (repository.findByModulId(poId, modulId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "Modul mit ID " + modulId + " existiert bereits in PO " + poId);
        }

        // Generiere interne ID
        modul.setId(UUID.randomUUID().toString());

        // Tenant aus bestehender Konfiguration der PO übernehmen
        Map<Integer, ModulConfig> existingModules = repository.getModulesForPo(poId);
        String tenantIdForPo = existingModules.values().stream()
            .map(ModulConfig::getTenantId)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        modul.setTenantId(tenantIdForPo);
        
        if (modul.getCreatedAt() == null) {
            modul.setCreatedAt(LocalDateTime.now());
        }

        repository.saveModule(poId, modul);
        return modul;
    }

    /**
     * Bearbeitet ein bestehendes Modul.
     */
    public ModulConfig bearbeiteModul(String poId, Integer modulId, ModulConfig patch, Set<UUID> tenantIds, boolean isAdmin) {
        ensureAccessToPo(poId, tenantIds, isAdmin);
        
        ModulConfig existing = repository.findByModulId(poId, modulId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Modul nicht gefunden: " + modulId));

        if (patch.getModulName() != null && !patch.getModulName().isBlank()) {
            existing.setModulName(patch.getModulName());
        }
        if (patch.getGewichtung() > 0) {
            existing.setGewichtung(patch.getGewichtung());
        }

        repository.saveModule(poId, existing);
        return existing;
    }

    /**
     * Setzt den aktiv/inaktiv Status eines Moduls.
     */
    public ModulConfig setzeModulAktiv(String poId, Integer modulId, boolean active, Set<UUID> tenantIds, boolean isAdmin) {
        ensureAccessToPo(poId, tenantIds, isAdmin);
        
        return repository.setModuleActive(poId, modulId, active)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Modul nicht gefunden: " + modulId));
    }

    /**
     * Gibt alle Module für eine Prüfungsordnung zurück.
     */
    public List<ModulConfig> alleModule(String poId, Set<UUID> tenantIds, boolean isAdmin) {
        ensureAccessToPo(poId, tenantIds, isAdmin);
        return repository.allModules(poId);
    }

    /**
     * Gibt nur aktive Module für eine Prüfungsordnung zurück.
     */
    public List<ModulConfig> aktiveModule(String poId, Set<UUID> tenantIds, boolean isAdmin) {
        ensureAccessToPo(poId, tenantIds, isAdmin);
        return repository.activeModules(poId);
    }

    /**
     * Löscht ein Modul.
     */
    public void loescheModul(String poId, Integer modulId, Set<UUID> tenantIds, boolean isAdmin) {
        ensureAccessToPo(poId, tenantIds, isAdmin);
        
        if (repository.findByModulId(poId, modulId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Modul nicht gefunden: " + modulId);
        }
        
        repository.deleteModule(poId, modulId);
    }

    /**
     * Findet ein Modul anhand der Modul-ID.
     */
    public ModulConfig findeModul(String poId, Integer modulId, Set<UUID> tenantIds, boolean isAdmin) {
        ensureAccessToPo(poId, tenantIds, isAdmin);
        
        return repository.findByModulId(poId, modulId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Modul nicht gefunden: " + modulId));
    }

    // -------------------- Hilfsmethoden --------------------

    /**
     * Prüft, ob der aufrufende Benutzer (laut JWT) Zugriff auf die
     * angegebene Prüfungsordnung (PO) hat. Grundlage ist die Tenant-ID,
     * die in den Modulen der PO hinterlegt ist.
     * Version für Students (keine Admin-Prüfung).
     */
    private void ensureAccessToPo(String poId, Set<UUID> tenantIds) {
        if (tenantIds == null || tenantIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Kein Tenant im JWT hinterlegt");
        }

        Map<Integer, ModulConfig> modules = repository.getModulesForPo(poId);
        String tenantIdForPo = modules.values().stream()
                .map(ModulConfig::getTenantId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Für die Prüfungsordnung " + poId + " ist kein Tenant konfiguriert"));

        UUID poTenant;
        try {
            poTenant = UUID.fromString(tenantIdForPo);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ungültige Tenant-ID für Prüfungsordnung " + poId);
        }

        if (!tenantIds.contains(poTenant)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Kein Zugriff auf Prüfungsordnung " + poId + " für diese Tenant-IDs");
        }
    }

    /**
     * Prüft, ob der aufrufende Benutzer (laut JWT) Zugriff auf die
     * angegebene Prüfungsordnung (PO) hat. 
     * Version für Admin-Operationen (kann Admin-Flag berücksichtigen).
     * Admins haben Zugriff auf alle Prüfungsordnungen.
     */
    private void ensureAccessToPo(String poId, Set<UUID> tenantIds, boolean isAdmin) {
        if (isAdmin) {
            // Admin darf auf alle Prüfungsordnungen zugreifen
            return;
        }
        ensureAccessToPo(poId, tenantIds);
    }
}

