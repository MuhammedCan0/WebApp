package com.university.tenant_service.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AuditableEntity extends BaseEntity {

    private Instant createdAt;
    private Instant updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public void setAuditCreate(UUID userId) {
        this.createdAt = Instant.now();
        this.createdBy = userId;
        this.updatedAt = Instant.now();
        this.updatedBy = userId;
    }

    public void setAuditUpdate(UUID userId) {
        this.updatedAt = Instant.now();
        this.updatedBy = userId;
    }
}
