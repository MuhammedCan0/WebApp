package de.frauas.notenmodulservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Input-DTO für das Validieren von Noten
 * Wird vom Prüfungsamt verwendet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteValidierenInput {

    @NotEmpty(message = "Liste der Note-IDs darf nicht leer sein")
    private List<UUID> noteIds;

    @NotEmpty(message = "Tenant-IDs dürfen nicht leer sein")
    private List<UUID> tenantIds;
}