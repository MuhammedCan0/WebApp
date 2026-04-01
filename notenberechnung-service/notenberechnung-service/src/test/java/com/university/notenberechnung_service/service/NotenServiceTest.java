package com.university.notenberechnung_service.service;

import com.university.notenberechnung_service.dto.NotenEingabe;
import com.university.notenberechnung_service.model.ModulConfig;
import com.university.notenberechnung_service.repository.ModulGewichtungsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit Tests für den NotenService.
 * Testet die Berechnungslogik für gewichtete Gesamtnoten.
 */
@ExtendWith(MockitoExtension.class)
class NotenServiceTest {

    @Mock
    private ModulGewichtungsRepository repository;

    @Mock
    private GraphQLClientService graphQLClientService;

    private NotenService notenService;

    private static final String TEST_PO_ID = "Informatik_BA";
    private static final UUID TEST_TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        notenService = new NotenService(repository, graphQLClientService);
    }

    // ==================== Helper-Methoden ====================

    private NotenEingabe createNotenEingabe(List<NotenEingabe.EinzelNote> noten) {
        NotenEingabe eingabe = new NotenEingabe();
        eingabe.setNotenListe(noten);
        return eingabe;
    }

    private NotenEingabe.EinzelNote createEinzelNote(Integer modulId, double note) {
        NotenEingabe.EinzelNote einzelNote = new NotenEingabe.EinzelNote();
        einzelNote.setModulId(modulId);
        einzelNote.setNote(note);
        return einzelNote;
    }

    private ModulConfig createModulConfig(Integer modulId, String name, double gewichtung, boolean active) {
        return new ModulConfig(UUID.randomUUID().toString(), modulId, name, gewichtung, active, TEST_TENANT_ID.toString());
    }

    private Map<Integer, ModulConfig> createModulStore(ModulConfig... modules) {
        Map<Integer, ModulConfig> store = new HashMap<>();
        for (ModulConfig module : modules) {
            store.put(module.getModulId(), module);
        }
        return store;
    }

    // ==================== Tests für berechneGesamtnote ====================

    @Nested
    @DisplayName("Tests für erfolgreiche Notenberechnung")
    class ErfolgreicheBerechnung {

        @Test
        @DisplayName("Berechnet korrekte gewichtete Gesamtnote mit einem Modul")
        void berechneGesamtnote_EinModul_KorrekteBerechnung() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 5.0, true);
            Map<Integer, ModulConfig> store = createModulStore(mathe);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(store);

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 2.0)
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act
            double result = notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds);

            // Assert
            assertEquals(2.0, result, 0.01);
        }

        @Test
        @DisplayName("Berechnet korrekte gewichtete Gesamtnote mit mehreren Modulen")
        void berechneGesamtnote_MehrereModule_KorrekteGewichtung() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 5.0, true);      // Gewicht 5
            ModulConfig info = createModulConfig(2, "Informatik", 10.0, true);      // Gewicht 10
            Map<Integer, ModulConfig> store = createModulStore(mathe, info);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(store);

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 1.0),   // Mathe: 1.0
                    createEinzelNote(2, 2.0)    // Info: 2.0
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act
            double result = notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds);

            // Assert
            // Erwartete Berechnung: (1.0 * 5 + 2.0 * 10) / (5 + 10) = 25 / 15 = 1.666... ≈ 1.7
            assertEquals(1.7, result, 0.01);
        }

        @Test
        @DisplayName("Berechnet Durchschnitt bei mehreren Noten für ein Modul")
        void berechneGesamtnote_MehrereNotenProModul_BerechnetDurchschnitt() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 5.0, true);
            Map<Integer, ModulConfig> store = createModulStore(mathe);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(store);

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 1.0),   // Erste Mathe-Note
                    createEinzelNote(1, 3.0)    // Zweite Mathe-Note
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act
            double result = notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds);

            // Assert
            // Erwartete Berechnung: Durchschnitt (1.0 + 3.0) / 2 = 2.0
            assertEquals(2.0, result, 0.01);
        }

        @Test
        @DisplayName("Rundet Ergebnis auf eine Nachkommastelle")
        void berechneGesamtnote_Rundung_EineNachkommastelle() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 3.0, true);
            ModulConfig info = createModulConfig(2, "Informatik", 3.0, true);
            Map<Integer, ModulConfig> store = createModulStore(mathe, info);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(store);

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 1.3),
                    createEinzelNote(2, 2.7)
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act
            double result = notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds);

            // Assert
            // (1.3 * 3 + 2.7 * 3) / 6 = 12 / 6 = 2.0
            assertEquals(2.0, result, 0.01);
        }
    }

    // ==================== Tests für Fehlerbehandlung ====================

    @Nested
    @DisplayName("Tests für Fehlerbehandlung")
    class Fehlerbehandlung {

        @Test
        @DisplayName("Wirft Exception bei leerer Notenliste")
        void berechneGesamtnote_LeereNotenliste_WirftException() {
            // Arrange
            NotenEingabe eingabe = createNotenEingabe(Collections.emptyList());
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act & Assert
            assertThrows(
                    org.springframework.web.server.ResponseStatusException.class,
                    () -> notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds)
            );
        }

        @Test
        @DisplayName("Wirft Exception bei null Notenliste")
        void berechneGesamtnote_NullNotenliste_WirftException() {
            // Arrange
            NotenEingabe eingabe = new NotenEingabe();
            eingabe.setNotenListe(null);
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act & Assert
            assertThrows(
                    org.springframework.web.server.ResponseStatusException.class,
                    () -> notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds)
            );
        }

        @Test
        @DisplayName("Wirft Exception bei Note unter 1.0")
        void berechneGesamtnote_NoteZuNiedrig_WirftException() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 5.0, true);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(createModulStore(mathe));

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 0.5)  // Ungültige Note < 1.0
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act & Assert
            assertThrows(
                    org.springframework.web.server.ResponseStatusException.class,
                    () -> notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds)
            );
        }

        @Test
        @DisplayName("Wirft Exception bei Note über 5.0")
        void berechneGesamtnote_NoteZuHoch_WirftException() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 5.0, true);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(createModulStore(mathe));

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 5.5)  // Ungültige Note > 5.0
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act & Assert
            assertThrows(
                    org.springframework.web.server.ResponseStatusException.class,
                    () -> notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds)
            );
        }

        @Test
        @DisplayName("Wirft Exception bei unbekannter Modul-ID")
        void berechneGesamtnote_UnbekannteModulId_WirftException() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 5.0, true);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(createModulStore(mathe));

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(999, 2.0)  // Unbekannte Modul-ID
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act & Assert
            assertThrows(
                    org.springframework.web.server.ResponseStatusException.class,
                    () -> notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds)
            );
        }

        @Test
        @DisplayName("Wirft Exception bei inaktivem Modul")
        void berechneGesamtnote_InaktivesModul_WirftException() {
            // Arrange
            ModulConfig inaktiv = createModulConfig(1, "Altes Modul", 5.0, false);  // active = false
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(createModulStore(inaktiv));

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 2.0)
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act & Assert
            assertThrows(
                    org.springframework.web.server.ResponseStatusException.class,
                    () -> notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds)
            );
        }

        @Test
        @DisplayName("Wirft Exception bei fehlender ModulId in Note")
        void berechneGesamtnote_FehlendeModulId_WirftException() {
            // Arrange
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(new HashMap<>());

            NotenEingabe.EinzelNote noteOhneModulId = new NotenEingabe.EinzelNote();
            noteOhneModulId.setModulId(null);
            noteOhneModulId.setNote(2.0);

            NotenEingabe eingabe = createNotenEingabe(List.of(noteOhneModulId));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act & Assert
            assertThrows(
                    org.springframework.web.server.ResponseStatusException.class,
                    () -> notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds)
            );
        }
    }

    // ==================== Tests für Grenzwerte ====================

    @Nested
    @DisplayName("Tests für Grenzwerte")
    class Grenzwerte {

        @Test
        @DisplayName("Akzeptiert Note 1.0 (Minimum)")
        void berechneGesamtnote_MinimaleNote_Akzeptiert() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 5.0, true);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(createModulStore(mathe));

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 1.0)  // Minimum
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act
            double result = notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds);

            // Assert
            assertEquals(1.0, result, 0.01);
        }

        @Test
        @DisplayName("Akzeptiert Note 5.0 (Maximum)")
        void berechneGesamtnote_MaximaleNote_Akzeptiert() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 5.0, true);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(createModulStore(mathe));

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 5.0)  // Maximum
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act
            double result = notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds);

            // Assert
            assertEquals(5.0, result, 0.01);
        }

        @Test
        @DisplayName("Akzeptiert Noten mit Dezimalstellen")
        void berechneGesamtnote_DezimalNoten_Akzeptiert() {
            // Arrange
            ModulConfig mathe = createModulConfig(1, "Mathematik", 5.0, true);
            when(repository.getModulesForPo(TEST_PO_ID)).thenReturn(createModulStore(mathe));

            NotenEingabe eingabe = createNotenEingabe(List.of(
                    createEinzelNote(1, 2.3)
            ));
            Set<UUID> tenantIds = Set.of(TEST_TENANT_ID);

            // Act
            double result = notenService.berechneGesamtnote(TEST_PO_ID, eingabe, tenantIds);

            // Assert
            assertEquals(2.3, result, 0.01);
        }
    }
}
