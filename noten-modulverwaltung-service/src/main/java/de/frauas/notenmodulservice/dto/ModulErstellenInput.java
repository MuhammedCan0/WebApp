package de.frauas.notenmodulservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Input-DTO für das Erstellen eines neuen Moduls
 * Wird vom Prüfungsamt verwendet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModulErstellenInput {

    @NotNull(message = "Modul-ID darf nicht leer sein")
    @Min(value = 1, message = "Modul-ID muss mindestens 1 sein")
    private Integer modulId;

    @NotNull(message = "Tenant-ID darf nicht leer sein")
    private UUID tenantId;

    @NotBlank(message = "Modulname darf nicht leer sein")
    private String modulName;

    @NotNull(message = "ECTS dürfen nicht null sein")
    @Min(value = 1, message = "ECTS müssen mindestens 1 sein")
    private Integer ects;

    @NotNull(message = "Gewichtung darf nicht null sein")
    @DecimalMin(value = "0.0", message = "Gewichtung muss mindestens 0.0 sein")
    private Double gewichtung;

    @NotBlank(message = "Studiengang darf nicht leer sein")
    private String studiengang;

    private Integer semester;

    private String beschreibung;
}