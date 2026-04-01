package de.frauas.notenmodulservice.controller;

import de.frauas.notenmodulservice.dto.StudentDTO;
import de.frauas.notenmodulservice.exception.AccessDeniedException;
import de.frauas.notenmodulservice.model.Modul;
import de.frauas.notenmodulservice.model.Note;
import de.frauas.notenmodulservice.model.NotenStatus;
import de.frauas.notenmodulservice.security.AccessControlService;
import de.frauas.notenmodulservice.security.JwtUser;
import de.frauas.notenmodulservice.security.Role;
import de.frauas.notenmodulservice.service.ModulService;
import de.frauas.notenmodulservice.service.NotenService;
import de.frauas.notenmodulservice.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

/**
 * GraphQL Query Controller
 * Behandelt alle Abfragen (Queries) im System
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class QueryController {

    private final NotenService notenService;
    private final ModulService modulService;
    private final StudentService studentService;
    private final AccessControlService accessControlService;

    /**
     * Query: alleNoten
     * Gibt alle Noten im System zurück (Nur für Prüfungsamt)
     */
    @QueryMapping
    public List<Note> alleNoten(@ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.ADMIN);
        log.debug("GraphQL Query: alleNoten durch Benutzer {}", jwtUser != null ? jwtUser.getSubject() : "unbekannt");
        return notenService.alleNoten();
    }

    /**
     * Query: notenVonStudent
     * Gibt alle PUBLISHED Noten eines Studierenden zurück
     */
    @QueryMapping
    public List<Note> notenVonStudent(
            @Argument String matrikelnummer,
            @Argument List<String> tenantIds,
            @ContextValue(name = "jwtUser") JwtUser jwtUser) {

        accessControlService.requireRole(jwtUser, Role.STUDENT, Role.PRUEFUNGSAMT, Role.ADMIN);

        // Matrikelnummer aus JWT ziehen, falls kein Argument gesetzt ist
        if (matrikelnummer == null && jwtUser != null && jwtUser.getMatrikelnummer() != null) {
            matrikelnummer = jwtUser.getMatrikelnummer().toString();
        }

        // Studenten dürfen nur ihre eigenen Noten abfragen
        if (jwtUser != null && jwtUser.isStudent()) {
            String tokenMatrikel = jwtUser.getMatrikelnummer() != null
                    ? jwtUser.getMatrikelnummer().toString()
                    : null;
            if (tokenMatrikel == null || !tokenMatrikel.equals(matrikelnummer)) {
                throw new AccessDeniedException("Student darf nur eigene Daten einsehen");
            }
        }

        List<UUID> tenantUuids = accessControlService.resolveTenantIds(jwtUser, tenantIds);

        log.debug("GraphQL Query: notenVonStudent für Matrikelnummer: {}, Tenants (effektiv): {}", matrikelnummer, tenantUuids);
        return notenService.notenVonStudent(matrikelnummer, tenantUuids);
    }

    /**
     * Query: noteById
     * Gibt eine spezifische Note anhand der ID zurück
     */
    @QueryMapping
    public Note noteById(@Argument String id,
                         @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.LEHRENDER, Role.STUDENT, Role.ADMIN);
        log.debug("GraphQL Query: noteById für ID: {} durch Benutzer {}", id,
                jwtUser != null ? jwtUser.getSubject() : "unbekannt");
        return notenService.noteById(UUID.fromString(id));
    }

    /**
     * Query: notenNachStatus
     * Gibt alle Noten mit einem bestimmten Status zurück
     */
    @QueryMapping
    public List<Note> notenNachStatus(@Argument NotenStatus status,
                                      @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.ADMIN);
        log.debug("GraphQL Query: notenNachStatus für Status: {}", status);
        return notenService.notenNachStatus(status);
    }

    /**
     * Query: notenVonLehrender
     * Gibt alle Noten eines Lehrenden zurück (anhand Matrikelnummer)
     */
    @QueryMapping
    public List<Note> notenVonLehrender(@Argument String lehrendenMatrikelnummer,
                                        @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.LEHRENDER, Role.ADMIN);

        // Lehrenden-Matrikelnummer aus JWT ziehen, falls nicht gesetzt
        if (lehrendenMatrikelnummer == null && jwtUser != null && jwtUser.getMatrikelnummer() != null) {
            lehrendenMatrikelnummer = jwtUser.getMatrikelnummer().toString();
        }

        // Lehrende dürfen nur ihre eigenen Noten abfragen
        if (jwtUser != null && jwtUser.isLehrender()) {
            String tokenMatrikel = jwtUser.getMatrikelnummer() != null
                    ? jwtUser.getMatrikelnummer().toString()
                    : null;
            if (tokenMatrikel == null || !tokenMatrikel.equals(lehrendenMatrikelnummer)) {
                throw new AccessDeniedException("Lehrender darf nur eigene Daten einsehen");
            }
        }

        log.debug("GraphQL Query: notenVonLehrender für Lehrenden-Matrikelnummer: {}", lehrendenMatrikelnummer);
        return notenService.notenVonLehrender(lehrendenMatrikelnummer);
    }

    /**
     * Query: notenVonLehrenderNachModulUndStatus
     * Gibt alle Noten eines Lehrenden für ein bestimmtes Modul und Status zurück
     * Unterstützt flexible Filterung über tenantIds, modulId und matrikelnummer
     */
    @QueryMapping
    public List<Note> notenVonLehrenderNachModulUndStatus(
            @Argument String lehrendenMatrikelnummer,
            @Argument List<String> tenantIds,
            @Argument Integer modulId,
            @Argument String matrikelnummer,
            @ContextValue(name = "jwtUser") JwtUser jwtUser) {

        accessControlService.requireRole(jwtUser, Role.LEHRENDER, Role.ADMIN);

        // Lehrenden-Matrikelnummer aus JWT ziehen, falls nicht gesetzt
        if (lehrendenMatrikelnummer == null && jwtUser != null && jwtUser.getMatrikelnummer() != null) {
            lehrendenMatrikelnummer = jwtUser.getMatrikelnummer().toString();
        }

        // Lehrende dürfen nur ihre eigenen Noten filtern
        if (jwtUser != null && jwtUser.isLehrender()) {
            String tokenMatrikel = jwtUser.getMatrikelnummer() != null
                    ? jwtUser.getMatrikelnummer().toString()
                    : null;
            if (tokenMatrikel == null || !tokenMatrikel.equals(lehrendenMatrikelnummer)) {
                throw new AccessDeniedException("Lehrender darf nur eigene Daten einsehen");
            }
        }

        List<UUID> tenantUuids = accessControlService.resolveTenantIds(jwtUser, tenantIds);

        log.debug("GraphQL Query: notenVonLehrenderNachModulUndStatus (ADD only) für Lehrender: {}, Tenants (effektiv): {}, Modul: {}, Student: {}",
                lehrendenMatrikelnummer, tenantUuids, modulId, matrikelnummer);

        return notenService.notenVonLehrenderNachModulUndStatus(
                lehrendenMatrikelnummer, tenantUuids, modulId, matrikelnummer);
    }

    /**
     * Query: notenPruefungsamtZuValidieren
     * Gibt TO_VALIDATE-Noten für angegebene Tenants mit optionalen Filtern zurück
     */
    @QueryMapping
    public List<Note> notenPruefungsamtZuValidieren(
        @Argument List<String> tenantIds,
        @Argument Integer modulId,
        @Argument String matrikelnummer,
            @Argument String lehrendenMatrikelnummer,
            @Argument String studiengang,
            @Argument String semester,
            @ContextValue(name = "jwtUser") JwtUser jwtUser) {

        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.ADMIN);

        List<UUID> tenantUuids = accessControlService.resolveTenantIds(jwtUser, tenantIds);

    log.debug("GraphQL Query: notenPruefungsamtZuValidieren Tenants (effektiv): {}, modulId: {}, matrikelnummer: {}, lehrender: {}",
            tenantUuids, modulId, matrikelnummer, lehrendenMatrikelnummer);

    return notenService.notenPruefungsamtZuValidieren(
            tenantUuids, modulId, matrikelnummer, lehrendenMatrikelnummer, studiengang, semester);
    }

    /**
     * Query: alleModule
     * Gibt alle Module im System zurück
     */
    @QueryMapping
    public List<Modul> alleModule(@Argument List<String> tenantIds,
                                  @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.LEHRENDER, Role.STUDENT, Role.ADMIN);
        log.debug("GraphQL Query: alleModule durch Benutzer {}", jwtUser != null ? jwtUser.getSubject() : "unbekannt");
        List<UUID> tenantUuids = accessControlService.resolveTenantIds(jwtUser, tenantIds);
        return modulService.alleModule(tenantUuids);
    }

    /**
     * Query: modulById
     * Gibt ein Modul anhand der Modul-ID zurück
     */
    @QueryMapping
    public Modul modulById(@Argument Integer modulId,
                           @Argument List<String> tenantIds,
                           @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.LEHRENDER, Role.STUDENT, Role.ADMIN);
        log.debug("GraphQL Query: modulById für ID: {}", modulId);
        List<UUID> tenantUuids = accessControlService.resolveTenantIds(jwtUser, tenantIds);
        return modulService.modulById(modulId, tenantUuids);
    }

    /**
     * Query: moduleNachStudiengang
     * Gibt alle Module eines Studiengangs zurück
     */
    @QueryMapping
    public List<Modul> moduleNachStudiengang(@Argument String studiengang,
                                             @Argument List<String> tenantIds,
                                             @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.PRUEFUNGSAMT, Role.LEHRENDER, Role.STUDENT, Role.ADMIN);
        log.debug("GraphQL Query: moduleNachStudiengang für Studiengang: {}", studiengang);
        List<UUID> tenantUuids = accessControlService.resolveTenantIds(jwtUser, tenantIds);
        return modulService.moduleNachStudiengang(studiengang, tenantUuids);
    }

    /**
     * Query: studentInfo
     * Gibt Informationen über einen Studierenden inkl. Noten zurück
     */
    @QueryMapping
    public StudentDTO studentInfo(@Argument String matrikelnummer,
                                  @ContextValue(name = "jwtUser") JwtUser jwtUser) {
        accessControlService.requireRole(jwtUser, Role.STUDENT, Role.PRUEFUNGSAMT, Role.ADMIN);

        // Matrikelnummer aus JWT ziehen, falls kein Argument gesetzt ist
        if (matrikelnummer == null && jwtUser != null && jwtUser.getMatrikelnummer() != null) {
            matrikelnummer = jwtUser.getMatrikelnummer().toString();
        }

        if (jwtUser != null && jwtUser.isStudent()) {
            String tokenMatrikel = jwtUser.getMatrikelnummer() != null
                    ? jwtUser.getMatrikelnummer().toString()
                    : null;
            if (tokenMatrikel == null || !tokenMatrikel.equals(matrikelnummer)) {
                throw new AccessDeniedException("Student darf nur eigene Daten einsehen");
            }
        }

        log.debug("GraphQL Query: studentInfo für Matrikelnummer: {}", matrikelnummer);
        return studentService.studentInfo(matrikelnummer);
    }

}