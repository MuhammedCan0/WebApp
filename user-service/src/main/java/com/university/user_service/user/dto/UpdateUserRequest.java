package com.university.user_service.user.dto;

import com.university.user_service.common.Role;
import com.university.user_service.common.Status;
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
public class UpdateUserRequest {

    private String firstName;
    private String lastName;
    private String studiengang;  // Deprecated: use studiengaenge instead (kept for backward compatibility)
    private Set<String> studiengaenge;  // Multi-Studiengang Support
    private Set<Role> roles;
    private Status status;
    private Set<UUID> tenantIds;  // Multi-Tenant Support
}
