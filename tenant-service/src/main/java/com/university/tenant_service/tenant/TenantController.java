package com.university.tenant_service.tenant;

import com.university.tenant_service.common.Role;
import com.university.tenant_service.exception.ForbiddenException;
import com.university.tenant_service.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable UUID id) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter für interne Calls
        // ADMIN-Prüfung nur wenn JWT vorhanden (externe Calls)
        requireAdminOrInternal("Access denied: Only ADMIN can view tenants");
        return ResponseEntity.ok(tenantService.getTenant(id));
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        requireAdmin("Access denied: Only ADMIN can list tenants");
        return ResponseEntity.ok(tenantService.findAll());
    }

    @GetMapping("/by-identifier/{identifier}")
    public ResponseEntity<Tenant> getTenantByIdentifier(@PathVariable String identifier) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter für interne Calls
        requireAdminOrInternal("Access denied: Only ADMIN can view tenants");
        return tenantService.findByIdentifier(identifier)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/identifier")
    public ResponseEntity<String> getTenantIdentifier(@PathVariable UUID id) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter für interne Calls
        requireAdminOrInternal("Access denied: Only ADMIN can view tenants");
        String identifier = tenantService.getTenantIdentifier(id);
        if (identifier == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(identifier);
    }

    @GetMapping("/{id}/studiengaenge/{studiengang}/allowed")
    public ResponseEntity<Boolean> isStudiengangAllowedForTenant(@PathVariable UUID id,
                                                                 @PathVariable String studiengang) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter für interne Calls
        requireAdminOrInternal("Access denied: Only ADMIN can view tenants");
        return ResponseEntity.ok(tenantService.isStudiengangAllowedForTenant(id, studiengang));
    }

    @GetMapping("/resolve")
    public ResponseEntity<UUID> resolveTenantIdForStudiengang(@RequestParam String studiengang) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter für interne Calls
        requireAdminOrInternal("Access denied: Only ADMIN can view tenants");
        return ResponseEntity.ok(tenantService.resolveTenantIdForStudiengang(studiengang));
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenantRequest) {
        requireAdmin("Access denied: Only ADMIN can create tenants");

        UUID creatorId = SecurityUtils.getCurrentUserId();
        Tenant tenant = tenantService.createTenant(
                tenantRequest.getName(),
                tenantRequest.getIdentifier(),
                tenantRequest.getDescription(),
                tenantRequest.getAllowedStudiengaenge()
        );
        log.info("Tenant created by {}: {}", creatorId, tenant.getIdentifier());
        return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable UUID id, @RequestBody Tenant tenantRequest) {
        requireAdmin("Access denied: Only ADMIN can update tenants");

        UUID updaterId = SecurityUtils.getCurrentUserId();
        Tenant existing = tenantService.getTenant(id);

        if (tenantRequest.getName() != null) {
            existing.setName(tenantRequest.getName());
        }
        if (tenantRequest.getDescription() != null) {
            existing.setDescription(tenantRequest.getDescription());
        }
        if (tenantRequest.getStatus() != null) {
            existing.setStatus(tenantRequest.getStatus());
        }
        if (tenantRequest.getAllowedStudiengaenge() != null) {
            existing.setAllowedStudiengaenge(tenantRequest.getAllowedStudiengaenge());
        }

        existing.setAuditUpdate(updaterId);
        Tenant updated = tenantService.save(existing);
        log.info("Tenant updated by {}: {}", updaterId, id);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/studiengaenge")
    public ResponseEntity<Tenant> addStudiengang(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        requireAdmin("Access denied: Only ADMIN can modify tenant studiengaenge");

        String studiengang = request.get("studiengang");
        Tenant updated = tenantService.addStudiengang(id, studiengang);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/studiengaenge")
    public ResponseEntity<Tenant> removeStudiengang(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        requireAdmin("Access denied: Only ADMIN can modify tenant studiengaenge");

        String studiengang = request.get("studiengang");
        Tenant updated = tenantService.removeStudiengang(id, studiengang);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Tenant> activateTenant(@PathVariable UUID id) {
        requireAdmin("Access denied: Only ADMIN can activate tenants");

        Tenant updated = tenantService.activateTenant(id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Tenant> deactivateTenant(@PathVariable UUID id) {
        requireAdmin("Access denied: Only ADMIN can deactivate tenants");

        Tenant updated = tenantService.deactivateTenant(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        requireAdmin("Access denied: Only ADMIN can delete tenants");

        UUID deleterId = SecurityUtils.getCurrentUserId();
        tenantService.delete(id);
        log.info("Tenant deleted by {}: {}", deleterId, id);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(String message) {
        var currentRoles = SecurityUtils.getCurrentRoles();
        if (currentRoles == null || !currentRoles.contains(Role.ADMIN)) {
            throw new ForbiddenException(message);
        }
    }

    private void requireAdminOrInternal(String message) {
        // Wenn keine Rollen vorhanden (Service-to-Service Call), vertrauen wir dem Filter
        var currentRoles = SecurityUtils.getCurrentRoles();
        if (currentRoles == null) {
            // Service-to-Service Call - ApiKeyValidationFilter hat bereits validiert
            return;
        }
        // User-Call - prüfe ADMIN Rolle
        boolean isAdmin = currentRoles.contains(Role.ADMIN);
        if (!isAdmin) {
            throw new ForbiddenException(message);
        }
    }
}
