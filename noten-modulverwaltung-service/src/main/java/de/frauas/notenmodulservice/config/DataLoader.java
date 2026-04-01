package de.frauas.notenmodulservice.config;

import de.frauas.notenmodulservice.model.Modul;
import de.frauas.notenmodulservice.model.Note;
import de.frauas.notenmodulservice.model.NotenStatus;
import de.frauas.notenmodulservice.repository.ModulRepository;
import de.frauas.notenmodulservice.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Lädt Testdaten beim Start der Anwendung
 * Nur für Entwicklungszwecke
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(ModulRepository modulRepository, 
                                   NoteRepository noteRepository) {
        return args -> {
            log.info("Lade Testdaten...");

            // Fixed UUIDs for tenant IDs - matching auth-service
            UUID tenantIdFb3 = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID tenantIdFb2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

            // Module für Fachbereich 3 - Luftverkehrsmanagement erstellen
            Modul modul1 = Modul.builder()
                    .modulId(1)
                    .tenantId(tenantIdFb3)
                    .modulName("Einführung in die Betriebswirtschaftslehre und Schlüsselkompetenzen")
                    .ects(5)
                    .gewichtung(2.0 / 35.0)
                    .studiengang("Luftverkehrsmanagement_-_Aviation_Management_dual_BA")
                    .semester(1)
                    .beschreibung("Grundlagen der BWL und wichtige Schlüsselkompetenzen")
                    .build();

            Modul modul2 = Modul.builder()
                    .modulId(2)
                    .tenantId(tenantIdFb3)
                    .modulName("Wirtschaftsmathematik")
                    .ects(5)
                    .gewichtung(2.0 / 35.0)
                    .studiengang("Luftverkehrsmanagement_-_Aviation_Management_dual_BA")
                    .semester(1)
                    .beschreibung("Mathematische Grundlagen für Wirtschaftswissenschaften")
                    .build();

            Modul modul3 = Modul.builder()
                    .modulId(6)
                    .tenantId(tenantIdFb3)
                    .modulName("Business English")
                    .ects(5)
                    .gewichtung(2.0 / 35.0)
                    .studiengang("Luftverkehrsmanagement_-_Aviation_Management_dual_BA")
                    .semester(1)
                    .beschreibung("Englisch für den Geschäftsalltag")
                    .build();

            // Module für Fachbereich 3 - Accounting and Finance erstellen
            Modul modul4 = Modul.builder()
                    .modulId(4)
                    .tenantId(tenantIdFb3)
                    .modulName("Nationale und internationale Steuerplanung")
                    .ects(5)
                    .gewichtung(2.0 / 35.0)
                    .studiengang("Accounting_and_Finance_MA")
                    .semester(1)
                    .beschreibung("Grundlagen der nationalen und internationalen Steuerplanung")
                    .build();

            Modul modul5 = Modul.builder()
                    .modulId(5)
                    .tenantId(tenantIdFb3)
                    .modulName("Unternehmensbewertung und Cost Management")
                    .ects(5)
                    .gewichtung(2.0 / 35.0)
                    .studiengang("Accounting_and_Finance_MA")
                    .semester(1)
                    .beschreibung("Methoden der Unternehmensbewertung und Kostenmanagement")
                    .build();

            // Module für Fachbereich 2 - Informatik erstellen
            Modul modul6 = Modul.builder()
                    .modulId(7)
                    .tenantId(tenantIdFb2)
                    .modulName("Programmierung 1")
                    .ects(5)
                    .gewichtung(2.0 / 35.0)
                    .studiengang("Informatik_BA")
                    .semester(1)
                    .beschreibung("Einführung in die Programmierung")
                    .build();

            Modul modul7 = Modul.builder()
                    .modulId(8)
                    .tenantId(tenantIdFb2)
                    .modulName("Datenbanken")
                    .ects(5)
                    .gewichtung(2.0 / 35.0)
                    .studiengang("Informatik_BA")
                    .semester(2)
                    .beschreibung("Relationale Datenbanken und SQL")
                    .build();

            modulRepository.save(modul1);
            modulRepository.save(modul2);
            modulRepository.save(modul3);
            modulRepository.save(modul4);
            modulRepository.save(modul5);
            modulRepository.save(modul6);
            modulRepository.save(modul7);

            log.info("7 Module erfolgreich erstellt");

            // Beispiel-Noten erstellen
            // Student Max Mustermann (Matrikelnummer: 3) - Doppelstudium
            // Luftverkehrsmanagement
            Note note1 = Note.builder()
                    .matrikelnummer("3")
                    .tenantId(tenantIdFb3)
                    .modulId(1)
                    .modulName(modul1.getModulName())
                    .lehrendenMatrikelnummer("4")
                    .note(1.7)
                    .status(NotenStatus.PUBLISHED)
                    .studiengang("Luftverkehrsmanagement_-_Aviation_Management_dual_BA")
                    .semester("1")
                    .build();

            Note note2 = Note.builder()
                    .matrikelnummer("3")
                    .tenantId(tenantIdFb3)
                    .modulId(2)
                    .modulName(modul2.getModulName())
                    .lehrendenMatrikelnummer("4")
                    .note(2.3)
                    .status(NotenStatus.PUBLISHED)
                    .studiengang("Luftverkehrsmanagement_-_Aviation_Management_dual_BA")
                    .semester("1")
                    .build();

            Note note3 = Note.builder()
                    .matrikelnummer("3")
                    .tenantId(tenantIdFb3)
                    .modulId(6)
                    .modulName(modul3.getModulName())
                    .lehrendenMatrikelnummer("4")
                    .note(3.0)
                    .status(NotenStatus.TO_VALIDATE)
                    .studiengang("Luftverkehrsmanagement_-_Aviation_Management_dual_BA")
                    .semester("1")
                    .build();

            // Max Mustermann - Accounting and Finance
            Note note4 = Note.builder()
                    .matrikelnummer("3")
                    .tenantId(tenantIdFb3)
                    .modulId(4)
                    .modulName(modul4.getModulName())
                    .lehrendenMatrikelnummer("4")
                    .note(1.3)
                    .status(NotenStatus.PUBLISHED)
                    .studiengang("Accounting_and_Finance_MA")
                    .semester("1")
                    .build();

            Note note5 = Note.builder()
                    .matrikelnummer("3")
                    .tenantId(tenantIdFb3)
                    .modulId(5)
                    .modulName(modul5.getModulName())
                    .lehrendenMatrikelnummer("4")
                    .status(NotenStatus.ADD)
                    .studiengang("Accounting_and_Finance_MA")
                    .semester("1")
                    .build();

            // Student Informatiker (Matrikelnummer: 7) - Fachbereich 2
            Note note6 = Note.builder()
                    .matrikelnummer("7")
                    .tenantId(tenantIdFb2)
                    .modulId(7)
                    .modulName(modul6.getModulName())
                    .lehrendenMatrikelnummer("4")
                    .note(2.0)
                    .status(NotenStatus.PUBLISHED)
                    .studiengang("Informatik_BA")
                    .semester("1")
                    .build();

            Note note7 = Note.builder()
                    .matrikelnummer("7")
                    .tenantId(tenantIdFb2)
                    .modulId(8)
                    .modulName(modul7.getModulName())
                    .lehrendenMatrikelnummer("4")
                    .note(1.7)
                    .status(NotenStatus.PUBLISHED)
                    .studiengang("Informatik_BA")
                    .semester("2")
                    .build();

            noteRepository.save(note1);
            noteRepository.save(note2);
            noteRepository.save(note3);
            noteRepository.save(note4);
            noteRepository.save(note5);
            noteRepository.save(note6);
            noteRepository.save(note7);

            log.info("7 Noten erfolgreich erstellt");
            log.info("Testdaten erfolgreich geladen!");
        };
    }
}