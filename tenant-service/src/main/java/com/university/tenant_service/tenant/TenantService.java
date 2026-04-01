package com.university.tenant_service.tenant;

import com.university.tenant_service.common.Status;
import com.university.tenant_service.exception.BadRequestException;
import com.university.tenant_service.exception.NotFoundException;
import com.university.tenant_service.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Tenant createTenant(String name, String identifier, String description, java.util.Set<String> allowedStudiengaenge) {
        if (name == null || name.isEmpty()) {
            throw new BadRequestException("Tenant name is required");
        }
        if (identifier == null || identifier.isEmpty()) {
            throw new BadRequestException("Tenant identifier is required");
        }

        if (tenantRepository.findByIdentifier(identifier).isPresent()) {
            throw new BadRequestException("Identifier already exists");
        }

        Tenant tenant = Tenant.builder()
            .name(name)
            .identifier(identifier)
            .description(description)
            .status(Status.ACTIVE)
            .allowedStudiengaenge(allowedStudiengaenge)
            .build();

        UUID creatorId = SecurityUtils.getCurrentUserId();
        if (creatorId != null) {
            tenant.setAuditCreate(creatorId);
        }

        log.info("Creating tenant: {}", identifier);
        return tenantRepository.save(tenant);
    }

    public Tenant getTenant(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
    }

    public Tenant save(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public Tenant addStudiengang(UUID tenantId, String studiengang) {
        if (studiengang == null || studiengang.isEmpty()) {
            throw new BadRequestException("Studiengang is required");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        java.util.Set<String> allowed = tenant.getAllowedStudiengaenge();
        if (allowed == null) {
            allowed = new java.util.HashSet<>();
        } else {
            allowed = new java.util.HashSet<>(allowed);
        }
        allowed.add(studiengang);
        tenant.setAllowedStudiengaenge(allowed);

        UUID updaterId = SecurityUtils.getCurrentUserId();
        if (updaterId != null) {
            tenant.setAuditUpdate(updaterId);
        }

        log.info("Added studiengang {} to tenant {}", studiengang, tenantId);
        return tenantRepository.save(tenant);
    }

    public Tenant removeStudiengang(UUID tenantId, String studiengang) {
        if (studiengang == null || studiengang.isEmpty()) {
            throw new BadRequestException("Studiengang is required");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        java.util.Set<String> allowed = tenant.getAllowedStudiengaenge();
        if (allowed == null || !allowed.contains(studiengang)) {
            throw new BadRequestException("Studiengang not configured for tenant");
        }

        allowed = new java.util.HashSet<>(allowed);
        allowed.remove(studiengang);
        tenant.setAllowedStudiengaenge(allowed);

        UUID updaterId = SecurityUtils.getCurrentUserId();
        if (updaterId != null) {
            tenant.setAuditUpdate(updaterId);
        }

        log.info("Removed studiengang {} from tenant {}", studiengang, tenantId);
        return tenantRepository.save(tenant);
    }

    public Tenant activateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
        tenant.setStatus(Status.ACTIVE);
        UUID updaterId = SecurityUtils.getCurrentUserId();
        if (updaterId != null) {
            tenant.setAuditUpdate(updaterId);
        }
        log.info("Activated tenant: {}", id);
        return tenantRepository.save(tenant);
    }

    public Tenant deactivateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));
        tenant.setStatus(Status.INACTIVE);
        UUID updaterId = SecurityUtils.getCurrentUserId();
        if (updaterId != null) {
            tenant.setAuditUpdate(updaterId);
        }
        log.info("Deactivated tenant: {}", id);
        return tenantRepository.save(tenant);
    }

    public boolean isStudiengangAllowedForTenant(UUID tenantId, String studiengang) {
        if (tenantId == null) {
            throw new BadRequestException("Tenant ID is required for studiengang validation");
        }
        if (studiengang == null || studiengang.isEmpty()) {
            throw new BadRequestException("Studiengang is required");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        var allowed = tenant.getAllowedStudiengaenge();
        if (allowed == null || allowed.isEmpty()) {
            throw new BadRequestException("No allowed studiengaenge configured for tenant: " + tenant.getIdentifier());
        }

        return allowed.contains(studiengang);
    }

    public UUID resolveTenantIdForStudiengang(String studiengang) {
        if (studiengang == null || studiengang.isEmpty()) {
            throw new BadRequestException("Studiengang is required to resolve tenant");
        }

        Tenant found = null;
        for (Tenant tenant : tenantRepository.findAll()) {
            if (tenant.getStatus() == Status.DELETED) {
                continue;
            }
            var allowed = tenant.getAllowedStudiengaenge();
            if (allowed != null && allowed.contains(studiengang)) {
                if (found != null) {
                    throw new BadRequestException("Studiengang " + studiengang + " is associated with multiple tenants; tenantId is required explicitly");
                }
                found = tenant;
            }
        }

        if (found == null) {
            throw new BadRequestException("Studiengang " + studiengang + " is not associated with any tenant");
        }

        return found.getId();
    }

    public Optional<Tenant> findByIdentifier(String identifier) {
        return tenantRepository.findByIdentifier(identifier);
    }

    public String getTenantIdentifier(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .map(Tenant::getIdentifier)
                .orElse(null);
    }

    public List<Tenant> findAll() {
        return tenantRepository.findAll();
    }

    public void delete(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        tenant.setStatus(Status.DELETED);
        UUID deleterId = SecurityUtils.getCurrentUserId();
        if (deleterId != null) {
            tenant.setAuditUpdate(deleterId);
        }
        tenantRepository.save(tenant);
        log.info("Soft deleted tenant: {}", id);
    }
}
