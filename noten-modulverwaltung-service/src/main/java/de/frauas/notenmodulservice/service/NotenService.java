package de.frauas.notenmodulservice.service;

import de.frauas.notenmodulservice.dto.NoteErstellenInput;
import de.frauas.notenmodulservice.dto.NoteSetzenInput;
import de.frauas.notenmodulservice.dto.NoteValidierenInput;
import de.frauas.notenmodulservice.exception.AccessDeniedException;
import de.frauas.notenmodulservice.exception.ConflictException;
import de.frauas.notenmodulservice.exception.InvalidStateException;
import de.frauas.notenmodulservice.exception.NotFoundException;
import de.frauas.notenmodulservice.exception.ValidationException;
import de.frauas.notenmodulservice.model.Note;
import de.frauas.notenmodulservice.model.NotenStatus;
import de.frauas.notenmodulservice.model.Modul;
import de.frauas.notenmodulservice.repository.NoteRepository;
import de.frauas.notenmodulservice.repository.ModulRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service-Klasse für die Verwaltung von Noten.
 * 
 * <p>Implementiert den kompletten Noten-Workflow:</p>
 * <ol>
 *   <li><b>noteAnlegen</b>: Prüfungsamt erstellt Note (Status: ADD)</li>
 *   <li><b>noteSetzen</b>: Lehrender trägt Notenwert ein (Status: TO_VALIDATE)</li>
 *   <li><b>notenValidieren</b>: Prüfungsamt gibt Note frei (Status: PUBLISHED)</li>
 * </ol>
 * 
 * <p>Alle Methoden berücksichtigen Tenant-Isolation und werfen typisierte
 * Exceptions für einheitliches Fehlerhandling.</p>
 * 
 * @see de.frauas.notenmodulservice.model.NotenStatus
 * @see de.frauas.notenmodulservice.exception.GraphQLExceptionHandler
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotenService {

    private final NoteRepository noteRepository;
    private final ModulRepository modulRepository;

    /**
     * Gibt alle Noten im System zurück (Nur für Prüfungsamt)
     */
    public List<Note> alleNoten() {
        log.debug("Lade alle Noten");
        return noteRepository.findAll();
    }

    /**
     * Gibt alle sichtbaren Noten eines Studierenden zurück
     * Studenten sehen nur veröffentlichte (PUBLISHED) Noten
     *
     * @param matrikelnummer Matrikelnummer des Studierenden
     * @param tenantIds Liste der Tenant-IDs (Pflicht)
     */
    public List<Note> notenVonStudent(String matrikelnummer, List<UUID> tenantIds) {
        if (tenantIds == null || tenantIds.isEmpty()) {
            throw new ValidationException("Mindestens eine Tenant-ID muss angegeben werden");
        }

        log.debug("Lade veröffentlichte Noten (PUBLISHED) für Student {} in Tenants {}", matrikelnummer, tenantIds);
        return noteRepository.findPublishedNotenByMatrikelnummerAndTenants(
                matrikelnummer,
                tenantIds
        );
    }

    /**
     * Gibt eine spezifische Note anhand der ID zurück
     */
    public Note noteById(UUID id) {
        log.debug("Lade Note mit ID: {}", id);
        return noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Note", id));
    }

    /**
     * Gibt alle Noten mit einem bestimmten Status zurück
     */
    public List<Note> notenNachStatus(NotenStatus status) {
        log.debug("Lade Noten mit Status: {}", status);
        return noteRepository.findByStatus(status);
    }

    /**
     * Gibt alle Noten eines Lehrenden zurück (anhand Matrikelnummer)
     * Lehrende sehen ausschließlich Noten im Status ADD
     */
    public List<Note> notenVonLehrender(String lehrendenMatrikelnummer) {
        log.debug("Lade Noten (nur ADD) von Lehrendem mit Matrikelnummer: {}", lehrendenMatrikelnummer);
        return noteRepository.findByLehrendenMatrikelnummerAndStatus(lehrendenMatrikelnummer, NotenStatus.ADD);
    }

    /**
     * Gibt alle Noten eines Lehrenden für ein bestimmtes Modul und Status zurück
     * Unterstützt flexible Filterung über tenantIds, modulId und matrikelnummer
     * 
     * @param lehrendenMatrikelnummer Matrikelnummer des Lehrenden (Pflicht)
     * @param tenantIds Liste der Tenant-IDs (Pflicht - mind. eine)
     * @param modulId Modul-ID (optional)
     * @param matrikelnummer Studenten-Matrikelnummer (optional)
     * @param status Status der Noten (Pflicht)
     */
    public List<Note> notenVonLehrenderNachModulUndStatus(
            String lehrendenMatrikelnummer,
            List<UUID> tenantIds,
            Integer modulId,
            String matrikelnummer) {

        if (tenantIds == null || tenantIds.isEmpty()) {
            throw new ValidationException("Mindestens eine Tenant-ID muss angegeben werden");
        }

        log.debug("Lade Noten (nur ADD) von Lehrendem {} für Tenants {}, Modul: {}, Student: {}", 
                  lehrendenMatrikelnummer, tenantIds, modulId, matrikelnummer);

        return noteRepository.findByLehrenderWithFilters(
                lehrendenMatrikelnummer,
                tenantIds,
                NotenStatus.ADD,
                modulId,
                (matrikelnummer != null && !matrikelnummer.isBlank()) ? matrikelnummer : null);
    }

    /**
     * Prüfungsamt: Suche nach Noten im Status TO_VALIDATE mit optionalen Filtern
     * tenantIds ist Pflicht
     */
        public List<Note> notenPruefungsamtZuValidieren(
            List<UUID> tenantIds,
            Integer modulId,
            String matrikelnummer,
            String lehrendenMatrikelnummer,
            String studiengang,
            String semester) {
        if (tenantIds == null || tenantIds.isEmpty()) {
            throw new ValidationException("Mindestens eine Tenant-ID muss angegeben werden");
        }

        log.debug("Lade TO_VALIDATE Noten für Tenants {}, Filter modulId={}, matrikelnummer={}, lehrendenMatrikelnummer={}",
                tenantIds, modulId, matrikelnummer, lehrendenMatrikelnummer);

        return noteRepository.findToValidateByTenantsWithFilters(
            tenantIds,
            NotenStatus.TO_VALIDATE,
            modulId,
            (matrikelnummer != null && !matrikelnummer.isBlank()) ? matrikelnummer : null,
            (lehrendenMatrikelnummer != null && !lehrendenMatrikelnummer.isBlank()) ? lehrendenMatrikelnummer : null,
            (studiengang != null && !studiengang.isBlank()) ? studiengang : null,
            (semester != null && !semester.isBlank()) ? semester : null
        );
    }

    /**
     * Erstellt eine neue Note mit Status ADD (Lehrende)
     * - optional übergebenes ID-Feld wird genutzt (sonst generiert JPA eine UUID)
     * - Tenant muss mit dem Modul übereinstimmen
     */
    public Note noteAnlegen(NoteErstellenInput input) {
        log.debug("Erstelle neue Note für Student {} und Modul {} (Tenant {})",
                input.getMatrikelnummer(), input.getModulId(), input.getTenantId());

        // Prüfen, ob angegebene ID bereits existiert
        if (input.getId() != null && noteRepository.existsById(input.getId())) {
            throw new ConflictException("Eine Note mit der angegebenen ID existiert bereits");
        }

        // Prüfen, ob bereits eine Note für Student + Modul existiert
        if (noteRepository.existsByMatrikelnummerAndModulIdAndTenantId(
            input.getMatrikelnummer(), input.getModulId(), input.getTenantId())) {
            throw new ConflictException("Für diesen Studierenden und dieses Modul existiert bereits eine Note");
        }

        // Modul laden und Tenant konsistent halten
        Modul modul = modulRepository.findByModulIdAndTenantId(input.getModulId(), input.getTenantId())
                .orElseThrow(() -> new NotFoundException("Modul", input.getModulId()));

        if (!modul.getTenantId().equals(input.getTenantId())) {
            throw new ValidationException("Tenant des Moduls stimmt nicht mit dem übergebenen Tenant überein");
        }

        Note note = Note.builder()
                .id(input.getId())
                .matrikelnummer(input.getMatrikelnummer())
                .tenantId(input.getTenantId())
                .modulId(input.getModulId())
                .modulName(modul.getModulName())
                .lehrendenMatrikelnummer(input.getLehrendenMatrikelnummer())
                .studiengang(input.getStudiengang())
                .semester(input.getSemester())
                .status(NotenStatus.ADD)
                .build();

        Note gespeicherteNote = noteRepository.save(note);
        log.info("Note erfolgreich angelegt mit ID: {}", gespeicherteNote.getId());
        return gespeicherteNote;
    }

    /**
     * Setzt die Note und ändert den Status auf TO_VALIDATE (Nur Lehrende)
     */
    public List<Note> noteSetzen(NoteSetzenInput input) {
        if (input.getNoteIds() == null || input.getNoteIds().isEmpty()) {
            throw new ValidationException("Mindestens eine Note-ID muss angegeben werden");
        }
        if (input.getTenantIds() == null || input.getTenantIds().isEmpty()) {
            throw new ValidationException("Mindestens eine Tenant-ID muss angegeben werden");
        }
        if (input.getNote() == null) {
            throw new ValidationException("Note darf nicht null sein");
        }
        if (input.getNote() < 1.0 || input.getNote() > 5.0) {
            throw new ValidationException("Note muss zwischen 1.0 und 5.0 liegen");
        }

        log.debug("Setze Noten für {} Note(n) auf Wert: {}", 
                  input.getNoteIds().size(), input.getNote());

        List<Note> noten = input.getNoteIds().stream()
                .map(id -> noteRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Note", id)))
                .collect(Collectors.toList());

        // Tenant-Gate: Lehrende dürfen nur Noten in erlaubten Tenants setzen
        noten.forEach(note -> {
            if (!input.getTenantIds().contains(note.getTenantId())) {
                throw new AccessDeniedException("Kein Zugriff auf Note (Tenant nicht erlaubt)");
            }
        });

        // Validierung: Nur Noten im Status ADD oder TO_VALIDATE können gesetzt werden
        noten.forEach(note -> {
            if (note.getStatus() == NotenStatus.PUBLISHED) {
                throw new InvalidStateException("Note mit ID " + note.getId() + 
                        " ist bereits veröffentlicht und kann nicht mehr geändert werden");
            }
        });

        // Noten setzen und Status ändern
        noten.forEach(note -> {
            note.setNote(input.getNote());
            note.setStatus(NotenStatus.TO_VALIDATE);
        });

        List<Note> gespeicherteNoten = noteRepository.saveAll(noten);
        log.info("{} Note(n) erfolgreich gesetzt und auf TO_VALIDATE geändert", gespeicherteNoten.size());
        return gespeicherteNoten;
    }

    /**
     * Validiert Noten und ändert den Status auf PUBLISHED (Nur Prüfungsamt)
     */
    public List<Note> notenValidieren(NoteValidierenInput input) {
        if (input.getNoteIds() == null || input.getNoteIds().isEmpty()) {
            throw new ValidationException("Mindestens eine Note-ID muss angegeben werden");
        }
        if (input.getTenantIds() == null || input.getTenantIds().isEmpty()) {
            throw new ValidationException("Mindestens eine Tenant-ID muss angegeben werden");
        }

        log.debug("Validiere {} Note(n)", input.getNoteIds().size());

        List<Note> noten = input.getNoteIds().stream()
                .map(id -> noteRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Note", id)))
                .collect(Collectors.toList());

        // Tenant-Gate: Prüfungsamt darf nur Noten in erlaubten Tenants validieren
        noten.forEach(note -> {
            if (!input.getTenantIds().contains(note.getTenantId())) {
                throw new AccessDeniedException("Kein Zugriff auf Note (Tenant nicht erlaubt)");
            }
        });

        // Validierung: Nur Noten im Status TO_VALIDATE können validiert werden
        noten.forEach(note -> {
            if (note.getStatus() != NotenStatus.TO_VALIDATE) {
                throw new InvalidStateException("Note mit ID " + note.getId() + 
                        " hat nicht den Status TO_VALIDATE und kann nicht validiert werden");
            }
            if (!note.isValidNote()) {
                throw new ValidationException("Note mit ID " + note.getId() + 
                        " hat keine gültige Note gesetzt");
            }
        });

        // Status auf PUBLISHED ändern
        noten.forEach(note -> note.setStatus(NotenStatus.PUBLISHED));

        List<Note> gespeicherteNoten = noteRepository.saveAll(noten);
        log.info("{} Note(n) erfolgreich validiert und veröffentlicht", gespeicherteNoten.size());
        return gespeicherteNoten;
    }

    /**
     * Löscht eine Note (Nur Prüfungsamt)
     */
    public boolean noteLoeschen(UUID id) {
        log.debug("Lösche Note mit ID: {}", id);

        if (!noteRepository.existsById(id)) {
            throw new NotFoundException("Note", id);
        }

        noteRepository.deleteById(id);
        log.info("Note mit ID {} erfolgreich gelöscht", id);
        return true;
    }
}