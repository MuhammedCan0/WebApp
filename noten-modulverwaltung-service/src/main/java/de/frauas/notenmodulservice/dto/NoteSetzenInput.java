package de.frauas.notenmodulservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Input-DTO für das Setzen von Noten
 * Wird von Lehrenden verwendet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteSetzenInput {

    @NotEmpty(message = "Liste der Note-IDs darf nicht leer sein")
    private List<UUID> noteIds;

    @NotEmpty(message = "Tenant-IDs dürfen nicht leer sein")
    private List<UUID> tenantIds;

    @NotNull(message = "Note darf nicht null sein")
    @Min(value = 1, message = "Note muss mindestens 1.0 sein")
    @Max(value = 5, message = "Note darf maximal 5.0 sein")
    private Double note;
}