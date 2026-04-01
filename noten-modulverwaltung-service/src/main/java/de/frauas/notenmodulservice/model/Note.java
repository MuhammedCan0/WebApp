package de.frauas.notenmodulservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity-Klasse für eine Note im System.
 * 
 * <p>Der Lebenszyklus einer Note durchläuft folgende Phasen:</p>
 * <ol>
 *   <li><b>ADD</b>: Prüfungsamt erstellt neue Note (ohne Notenwert)</li>
 *   <li><b>TO_VALIDATE</b>: Lehrender trägt Notenwert ein, wartet auf Validierung</li>
 *   <li><b>PUBLISHED</b>: Prüfungsamt validiert, Note ist für Studierende sichtbar</li>
 * </ol>
 */
@Entity
@Table(name = "noten")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note {

    /** Eindeutige ID der Note (UUID) */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Matrikelnummer des Studierenden */
    @NotBlank(message = "Matrikelnummer darf nicht leer sein")
    @Column(nullable = false)
    private String matrikelnummer;

    /** Zugehöriger Mandant (Hochschule/Fakultät) */
    @NotNull(message = "Tenant-ID darf nicht leer sein")
    @Column(nullable = false)
    private UUID tenantId;

    /** Referenz auf das Modul */
    @NotNull(message = "Modul-ID darf nicht leer sein")
    @Column(nullable = false)
    private Integer modulId;

    /** Modulname (denormalisiert für schnellere Abfragen) */
    @Column
    private String modulName;

    /** Matrikelnummer des verantwortlichen Lehrenden */
    @NotBlank(message = "Lehrenden-Matrikelnummer darf nicht leer sein")
    @Column(nullable = false)
    private String lehrendenMatrikelnummer;

    /** Notenwert (1.0 - 5.0, null wenn noch nicht gesetzt) */
    @Min(value = 1, message = "Note muss mindestens 1.0 sein")
    @Max(value = 5, message = "Note darf maximal 5.0 sein")
    @Column
    private Double note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotenStatus status;

    @Column
    private String studiengang;

    @Column
    private String semester;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime aktualisiertAm;

    /**
     * Prüft, ob die Note gültig ist (zwischen 1.0 und 5.0)
     */
    public boolean isValidNote() {
        return note != null && note >= 1.0 && note <= 5.0;
    }

    /**
     * Prüft, ob die Note bestanden ist (Note <= 4.0)
     */
    public boolean isBestanden() {
        return note != null && note <= 4.0;
    }
}