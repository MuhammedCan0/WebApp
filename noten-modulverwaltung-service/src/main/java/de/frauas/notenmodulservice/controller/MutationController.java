package de.frauas.notenmodulservice.controller;

import de.frauas.notenmodulservice.dto.ModulErstellenInput;
import de.frauas.notenmodulservice.dto.NoteErstellenInput;
import de.frauas.notenmodulservice.dto.NoteSetzenInput;
import de.frauas.notenmodulservice.dto.NoteValidierenInput;
import de.frauas.notenmodulservice.exception.AccessDeniedException;
import de.frauas.notenmodulservice.exception.ValidationException;
import de.frauas.notenmodulservice.model.Modul;
import de.frauas.notenmodulservice.model.Note;
import de.frauas.notenmodulservice.security.AccessControlService;
import de.frauas.notenmodulservice.security.JwtUser;
import de.frauas.notenmodulservice.security.Role;
import de.frauas.notenmodulservice.service.ModulService;
import de.frauas.notenmodulservice.service.NotenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * GraphQL Mutation Controller
 * Behandelt alle Mutationen (Änderungen) im System
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MutationController {

    private final NotenService notenService;
    private final ModulService modulService;
    private final AccessControlService accessControlService;

    /**
     * Mutation: noteAnlegen
     * Erstellt eine neue Note mit Status ADD (Prüfungsamt oder Admin)
     */
    @MutationMapping
    public Note noteAnlegen(@Argument NoteErstellenInput input,
                            @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.ADMIN);

        // Prüfungsamt ohne Admin-Rolle darf nur in eigenen Tenants Noten anlegen
        if (jwtUser != null && jwtUser.isPruefungsamt() && !jwtUser.isGlobalAdmin()) {
            if (jwtUser.getTenantIds().stream().noneMatch(t -> t.equals(input.getTenantId()))) {
                throw new AccessDeniedException("Tenant der Note ist für dieses Prüfungsamt nicht erlaubt");
            }
        }

        log.debug("GraphQL Mutation: noteAnlegen für Student {} und Modul {}",
                input.getMatrikelnummer(), input.getModulId());
        return notenService.noteAnlegen(input);
    }

    /**
     * Mutation: noteSetzen
     * Setzt die Note und ändert den Status auf TO_VALIDATE (Nur Lehrende, Note und IDs Pflicht)
     */
    @MutationMapping
    public List<Note> noteSetzen(@Argument NoteSetzenInput input,
                                 @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.LEHRENDER, Role.ADMIN);

        // Für alle Nicht-Admin-Rollen werden die Tenant-IDs ausschließlich aus dem Token genommen
        if (jwtUser != null && !jwtUser.isGlobalAdmin()) {
            input.setTenantIds(jwtUser.getTenantIds());
        }

        log.debug("GraphQL Mutation: noteSetzen für {} Note(n)", input.getNoteIds().size());
        return notenService.noteSetzen(input);
    }

    /**
     * Mutation: notenValidieren
     * Validiert Noten und ändert den Status auf PUBLISHED (Nur Prüfungsamt)
     */
    @MutationMapping
    public List<Note> notenValidieren(@Argument NoteValidierenInput input,
                                      @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.ADMIN);

        // Für alle Nicht-Admin-Rollen werden die Tenant-IDs ausschließlich aus dem Token genommen
        if (jwtUser != null && !jwtUser.isGlobalAdmin()) {
            input.setTenantIds(jwtUser.getTenantIds());
        }

        log.debug("GraphQL Mutation: notenValidieren für {} Note(n)", input.getNoteIds().size());
        return notenService.notenValidieren(input);
    }

    /**
     * Mutation: noteLoeschen
     * Löscht eine Note (Nur Prüfungsamt)
     */
    @MutationMapping
    public Boolean noteLoeschen(@Argument String id,
                                @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.ADMIN);
        log.debug("GraphQL Mutation: noteLoeschen für ID: {}", id);
        return notenService.noteLoeschen(UUID.fromString(id));
    }

    /**
     * Mutation: modulErstellen
     * Erstellt ein neues Modul (Nur Prüfungsamt)
     */
    @MutationMapping
    public Modul modulErstellen(@Argument ModulErstellenInput input,
                                @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.ADMIN);

        // Prüfungsamt ohne Admin-Rolle darf nur in eigenen Tenants Module anlegen
        if (jwtUser != null && jwtUser.isPruefungsamt() && !jwtUser.isGlobalAdmin()) {
            if (jwtUser.getTenantIds().stream().noneMatch(t -> t.equals(input.getTenantId()))) {
                throw new AccessDeniedException("Tenant des Moduls ist für dieses Prüfungsamt nicht erlaubt");
            }
        }

        log.debug("GraphQL Mutation: modulErstellen für Modul-ID: {}", input.getModulId());
        return modulService.modulErstellen(input);
    }

    /**
     * Mutation: modulAktualisieren
     * Aktualisiert ein bestehendes Modul (Nur Prüfungsamt)
     */
    @MutationMapping
    public Modul modulAktualisieren(@Argument Integer modulId, 
                                    @Argument ModulErstellenInput input,
                                    @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.ADMIN);

        // Prüfungsamt ohne Admin-Rolle darf nur in eigenen Tenants Module aktualisieren
        if (jwtUser != null && jwtUser.isPruefungsamt() && !jwtUser.isGlobalAdmin()) {
            if (jwtUser.getTenantIds().stream().noneMatch(t -> t.equals(input.getTenantId()))) {
                throw new AccessDeniedException("Tenant des Moduls ist für dieses Prüfungsamt nicht erlaubt");
            }
        }

        log.debug("GraphQL Mutation: modulAktualisieren für Modul-ID: {}", modulId);
        return modulService.modulAktualisieren(modulId, input);
    }

    /**
     * Mutation: modulLoeschen
     * Löscht ein Modul (Nur Prüfungsamt)
     */
    @MutationMapping
    public Boolean modulLoeschen(@Argument Integer modulId,
                                 @Argument String tenantId,
                                 @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.ADMIN);

        if (tenantId == null || tenantId.isBlank()) {
            throw new ValidationException("Tenant-ID muss angegeben werden");
        }

        UUID tenantUuid = UUID.fromString(tenantId);

        // Prüfungsamt ohne Admin-Rolle darf nur in eigenen Tenants Module löschen
        if (jwtUser != null && jwtUser.isPruefungsamt() && !jwtUser.isGlobalAdmin()) {
            if (jwtUser.getTenantIds().stream().noneMatch(t -> t.equals(tenantUuid))) {
                throw new AccessDeniedException("Tenant des Moduls ist für dieses Prüfungsamt nicht erlaubt");
            }
        }

        log.debug("GraphQL Mutation: modulLoeschen für Modul-ID: {}", modulId);
        return modulService.modulLoeschen(modulId, tenantUuid);
    }

}