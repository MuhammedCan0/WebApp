package de.frauas.notenmodulservice.service;

import de.frauas.notenmodulservice.client.UserProfileResponse;
import de.frauas.notenmodulservice.client.UserServiceClient;
import de.frauas.notenmodulservice.dto.StudentDTO;
import de.frauas.notenmodulservice.exception.NotFoundException;
import de.frauas.notenmodulservice.model.Note;
import de.frauas.notenmodulservice.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service-Klasse für die Verwaltung von Studenteninformationen
 * Hinweis: In einer echten Implementierung würde dieser Service
 * wahrscheinlich mit einem separaten User-Service kommunizieren
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentService {

    private final NoteRepository noteRepository;
    private final UserServiceClient userServiceClient;

    /**
     * Gibt Informationen über einen Studierenden inkl. seiner Noten zurück
     * Stammdaten kommen aus dem User-Service.
     */
    public StudentDTO studentInfo(String matrikelnummer) {
        log.debug("Lade Studenteninformationen für Matrikelnummer: {}", matrikelnummer);

        List<Note> noten = noteRepository.findByMatrikelnummer(matrikelnummer);

        if (noten.isEmpty()) {
            throw new NotFoundException("Keine Daten für Student mit Matrikelnummer " + 
                                     matrikelnummer + " gefunden");
        }

        UserProfileResponse profile = userServiceClient.getUserByMatrikelnummer(matrikelnummer);
        Note ersteNote = noten.get(0);

        String studiengang = profile.getStudiengang();
        if (studiengang == null && profile.getStudiengaenge() != null && !profile.getStudiengaenge().isEmpty()) {
            studiengang = profile.getStudiengaenge().iterator().next();
        }

        return StudentDTO.builder()
                .matrikelnummer(matrikelnummer)
            .firstName(profile.getFirstName())
            .lastName(profile.getLastName())
                .studiengang(studiengang)
                .semester(ersteNote.getSemester() != null ?
                         Integer.parseInt(ersteNote.getSemester()) : null)
                .noten(noten)
                .build();
    }
}