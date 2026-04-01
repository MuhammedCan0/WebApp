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
public class UserAuthResponse {

    private UUID id;
    private Set<Role> roles;
    private Status status;
    private Set<String> studiengaenge;
    private Set<UUID> tenantIds;
    private Long matrikelnummer;
}
