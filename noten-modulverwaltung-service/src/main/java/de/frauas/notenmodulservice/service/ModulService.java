package de.frauas.notenmodulservice.service;

import de.frauas.notenmodulservice.dto.ModulErstellenInput;
import de.frauas.notenmodulservice.exception.ConflictException;
import de.frauas.notenmodulservice.exception.NotFoundException;
import de.frauas.notenmodulservice.exception.ValidationException;
import de.frauas.notenmodulservice.model.Modul;
import de.frauas.notenmodulservice.repository.ModulRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service-Klasse für die Verwaltung von Modulen.
 * 
 * <p>Module sind mandantenspezifisch und definieren die Lehrveranstaltungen
 * eines Studiengangs mit ECTS-Punkten und Gewichtung für die Durchschnittsberechnung.</p>
 * 
 * <p>Nur das Prüfungsamt darf Module erstellen, aktualisieren und löschen.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ModulService {

    private final ModulRepository modulRepository;

    /**
     * Gibt alle Module im System zurück
     */
    public List<Modul> alleModule(List<UUID> tenantIds) {
        log.debug("Lade alle Module");
        return modulRepository.findByTenantIdIn(tenantIds);
    }

    /**
     * Gibt ein Modul anhand der Modul-ID zurück
     */
    public Modul modulById(Integer modulId, List<UUID> tenantIds) {
        log.debug("Lade Modul mit ID: {}", modulId);
        List<Modul> module = modulRepository.findByModulIdAndTenantIdIn(modulId, tenantIds);
        if (module.isEmpty()) {
            throw new NotFoundException("Modul", modulId);
        }
        if (module.size() > 1) {
            throw new ValidationException("Mehrere Module mit ID " + modulId + " gefunden. Tenant-ID ist erforderlich");
        }
        return module.get(0);
    }

    /**
     * Gibt alle Module eines Studiengangs zurück
     */
    public List<Modul> moduleNachStudiengang(String studiengang, List<UUID> tenantIds) {
        log.debug("Lade Module für Studiengang: {}", studiengang);
        return modulRepository.findByStudiengangAndTenantIdIn(studiengang, tenantIds);
    }

    /**
     * Erstellt ein neues Modul (Nur Prüfungsamt)
     */
    public Modul modulErstellen(ModulErstellenInput input) {
        log.debug("Erstelle neues Modul mit ID: {}", input.getModulId());

        // Prüfen, ob Modul bereits existiert
        if (modulRepository.existsByModulIdAndTenantId(input.getModulId(), input.getTenantId())) {
            throw new ConflictException("Modul mit ID " + input.getModulId() + " existiert bereits");
        }

        Modul modul = Modul.builder()
                .modulId(input.getModulId())
                .tenantId(input.getTenantId())
                .modulName(input.getModulName())
                .ects(input.getEcts())
                .gewichtung(input.getGewichtung())
                .studiengang(input.getStudiengang())
                .semester(input.getSemester())
                .beschreibung(input.getBeschreibung())
                .build();

        Modul gespeichertesModul = modulRepository.save(modul);
        log.info("Modul erfolgreich erstellt mit ID: {}", gespeichertesModul.getModulId());
        return gespeichertesModul;
    }

    /**
     * Aktualisiert ein bestehendes Modul (Nur Prüfungsamt)
     */
    public Modul modulAktualisieren(Integer modulId, ModulErstellenInput input) {
        log.debug("Aktualisiere Modul mit ID: {}", modulId);

        Modul modul = modulRepository.findByModulIdAndTenantId(modulId, input.getTenantId())
                .orElseThrow(() -> new NotFoundException("Modul", modulId));

        // Modul aktualisieren
        modul.setModulId(input.getModulId());
        modul.setTenantId(input.getTenantId());
        modul.setModulName(input.getModulName());
        modul.setEcts(input.getEcts());
        modul.setGewichtung(input.getGewichtung());
        modul.setStudiengang(input.getStudiengang());
        modul.setSemester(input.getSemester());
        modul.setBeschreibung(input.getBeschreibung());

        Modul gespeichertesModul = modulRepository.save(modul);
        log.info("Modul mit ID {} erfolgreich aktualisiert", modulId);
        return gespeichertesModul;
    }

    /**
     * Löscht ein Modul (Nur Prüfungsamt)
     */
    @Transactional
    public boolean modulLoeschen(Integer modulId, UUID tenantId) {
        log.debug("Lösche Modul mit ID: {}", modulId);

        if (!modulRepository.existsByModulIdAndTenantId(modulId, tenantId)) {
            throw new NotFoundException("Modul", modulId);
        }

        modulRepository.deleteByModulIdAndTenantId(modulId, tenantId);
        log.info("Modul mit ID {} erfolgreich gelöscht", modulId);
        return true;
    }
}