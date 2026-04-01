package com.university.user_service.user.dto;

import com.university.user_service.common.Role;
import com.university.user_service.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private Long matrikelnummer;
    private String email;
    private String firstName;
    private String lastName;
    private String studiengang;  // Deprecated: Compatibility - returns first studiengang or null
    private Set<String> studiengaenge;  // Multi-Studiengang Support
    private Set<Role> roles;
    private Status status;
    private Set<UUID> tenantIds;  // Multi-Tenant Support
    private Instant createdAt;
    private Instant updatedAt;
}
