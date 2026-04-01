package de.frauas.notenmodulservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity-Klasse für ein Modul im Studiengang.
 * 
 * <p>Module sind mandantenspezifisch (Tenant) und werden durch die Kombination
 * von {@code modulId} und {@code tenantId} eindeutig identifiziert.</p>
 * 
 * <p>Beispiel: Modul "Mathematik I" kann in verschiedenen Hochschulen (Tenants)
 * mit unterschiedlichen ECTS-Werten existieren.</p>
 */
@Entity
@Table(
    name = "module",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "modulId"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modul {

    /** Technische UUID (Primärschlüssel) */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Fachliche Modul-ID (z.B. 1001) */
    @NotNull(message = "Modul-ID darf nicht leer sein")
    @Column(nullable = false)
    private Integer modulId;

    /** Zugehöriger Mandant (Hochschule/Fakultät) */
    @NotNull(message = "Tenant-ID darf nicht leer sein")
    @Column(nullable = false)
    private UUID tenantId;

    /** Modulname (z.B. "Mathematik I") */
    @NotBlank(message = "Modulname darf nicht leer sein")
    @Column(nullable = false)
    private String modulName;

    /** ECTS-Punkte des Moduls */
    @Min(value = 1, message = "ECTS müssen mindestens 1 sein")
    @Column(nullable = false)
    private Integer ects;

    /** Gewichtungsfaktor für Durchschnittsberechnung */
    @DecimalMin(value = "0.0", message = "Gewichtung muss mindestens 0.0 sein")
    @Column(nullable = false)
    private Double gewichtung;

    @NotBlank(message = "Studiengang darf nicht leer sein")
    @Column(nullable = false)
    private String studiengang;

    @Column
    private Integer semester;

    @Column(length = 1000)
    private String beschreibung;

    /**
     * Gibt die volle Modulbezeichnung zurück
     */
    public String getVollstaendigeBezeichnung() {
        return modulId + " - " + modulName;
    }
}