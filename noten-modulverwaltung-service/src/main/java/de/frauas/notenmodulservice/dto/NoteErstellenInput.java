package de.frauas.notenmodulservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Input-DTO für das Erstellen einer neuen Note
 * Wird von Lehrenden verwendet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteErstellenInput {

    private UUID id;

    @NotBlank(message = "Matrikelnummer darf nicht leer sein")
    private String matrikelnummer;

    @NotNull(message = "Tenant-ID darf nicht leer sein")
    private UUID tenantId;

    @NotNull(message = "Modul-ID darf nicht leer sein")
    private Integer modulId;

    @NotBlank(message = "Lehrenden-Matrikelnummer darf nicht leer sein")
    private String lehrendenMatrikelnummer;

    private String studiengang;

    private String semester;
}