package com.university.auth_service.user.client;

import com.university.auth_service.common.Role;
import com.university.auth_service.common.Status;
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
public class UserAuthDto {

    private UUID id;
    private Set<Role> roles;
    private Status status;
    private Set<String> studiengaenge;
    private Set<UUID> tenantIds;
    private Long matrikelnummer;
}
