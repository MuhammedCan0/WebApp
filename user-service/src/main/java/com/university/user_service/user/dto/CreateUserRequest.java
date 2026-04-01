package com.university.user_service.user.dto;

import com.university.user_service.common.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    private String studiengang;  // Deprecated: use studiengaenge instead (kept for backward compatibility)
    private Set<String> studiengaenge;  // Multi-Studiengang Support
    private Set<Role> roles;
    private Set<UUID> tenantIds;  // Multi-Tenant: User kann mehreren Tenants zugeordnet werden
    private String fachbereich;  // Identifier like "FB3-DEPT" - kann verwendet werden, um tenantId zu ermitteln
    private Set<String> fachbereiche;  // Mehrere Fachbereiche für Multi-Tenant
}
