package com.university.user_service.tenant.client;

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
public class TenantDto {

    private UUID id;
    private String name;
    private String identifier;
    private String description;
    private Status status;
    private Set<String> allowedStudiengaenge;
}
