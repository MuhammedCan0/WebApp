package com.university.user_service.user.model;

import com.university.user_service.common.AuditableEntity;
import com.university.user_service.common.Role;
import com.university.user_service.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class User extends AuditableEntity {

    private Long matrikelnummer;
    private String username;
    private String firstName;
    private String lastName;
    private Set<String> studiengaenge; // Multi-Studiengang Support: User kann mehrere Studiengänge haben
    private Status status;
    private Set<Role> roles;
    private Set<UUID> tenantIds; // Multi-Tenant Support: User kann mehreren Tenants zugeordnet sein
}
