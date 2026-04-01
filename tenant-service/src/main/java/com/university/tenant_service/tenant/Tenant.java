package com.university.tenant_service.tenant;

import com.university.tenant_service.common.AuditableEntity;
import com.university.tenant_service.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Tenant extends AuditableEntity {

    private String name;
    private String identifier;
    private String description;
    private Status status;
    private Set<String> allowedStudiengaenge;
}
