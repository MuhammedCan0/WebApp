package com.university.user_service.user;

import com.university.user_service.common.Role;
import com.university.user_service.exception.ForbiddenException;
import com.university.user_service.security.SecurityUtils;
import com.university.user_service.user.dto.CreateUserRequest;
import com.university.user_service.user.dto.UpdateCredentialsRequest;
import com.university.user_service.user.dto.UpdateUserRequest;
import com.university.user_service.user.dto.UserAuthResponse;
import com.university.user_service.user.dto.UserResponse;
import com.university.user_service.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * REST Controller für Benutzer-Verwaltung.
 * 
 * <p>Endpunkte:
 * <ul>
 *   <li>GET /api/v1/users - Alle Benutzer abrufen (ADMIN, PRUEFUNGSAMT)</li>
 *   <li>GET /api/v1/users/{id} - Benutzer nach ID abrufen</li>
 *   <li>POST /api/v1/users - Neuen Benutzer anlegen (ADMIN, PRUEFUNGSAMT)</li>
 *   <li>PUT /api/v1/users/{id} - Benutzer aktualisieren</li>
 *   <li>DELETE /api/v1/users/{id} - Benutzer löschen (ADMIN)</li>
 *   <li>PATCH /api/v1/users/{id}/status - Benutzer-Status ändern</li>
 * </ul>
 * 
 * <p>Berechtigungen:
 * <ul>
 *   <li>ADMIN: Voller Zugriff auf alle Benutzer</li>
 *   <li>PRUEFUNGSAMT: Zugriff nur auf eigene Tenant-Benutzer</li>
 *   <li>STUDENT/LEHRENDER: Nur eigenes Profil lesen</li>
 * </ul>
 * 
 * @see UserService
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        var currentRoles = SecurityUtils.getCurrentRoles();
        
        if (currentUserId == null) {
            throw new ForbiddenException("Authentication required");
        }

        // Erlaubt: ADMIN, PRUEFUNGSAMT (im eigenen Tenant) oder Owner (sein eigenes Profil)
        boolean isAdmin = currentRoles != null && currentRoles.contains(Role.ADMIN);
        boolean isPruefungsamt = currentRoles != null && currentRoles.contains(Role.PRUEFUNGSAMT);
        boolean isOwner = id.equals(currentUserId);

        if (!isAdmin && !isPruefungsamt && !isOwner) {
            throw new ForbiddenException("Access denied: Insufficient permissions");
        }

        UserResponse target = userService.getUser(id);

        if (isPruefungsamt && !isOwner) {
            // PRUEFUNGSAMT darf nur Nutzer mit gemeinsamem Tenant sehen
            Set<UUID> currentTenants = getCurrentUserTenants();
            if (!hasCommonTenant(currentTenants, target.getTenantIds())) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT can only access users from its own tenant(s)");
            }
        }

        return ResponseEntity.ok(target);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studiengang,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) com.university.user_service.common.Status status) {
        
        var currentRoles = SecurityUtils.getCurrentRoles();
        
        if (currentRoles == null) {
            throw new ForbiddenException("Access denied: Authentication required");
        }

        boolean isAdmin = currentRoles.contains(Role.ADMIN);
        boolean isPruefungsamt = currentRoles.contains(Role.PRUEFUNGSAMT);

        if (!isAdmin && !isPruefungsamt) {
            throw new ForbiddenException("Access denied: Only ADMIN and PRUEFUNGSAMT can list users");
        }

        List<UserResponse> users;
        
        if (isAdmin) {
            users = userService.getAllUsers();
        } else {
            // PRUEFUNGSAMT sieht Nutzer aus allen eigenen Tenants
            Set<UUID> currentTenants = getCurrentUserTenants();
            if (currentTenants == null || currentTenants.isEmpty()) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT without tenant assignment");
            }

            // Sammle User aus allen Tenants des PRUEFUNGSAMT
            users = new java.util.ArrayList<>();
            for (UUID tenantId : currentTenants) {
                users.addAll(userService.getUsersByTenant(tenantId));
            }
            // Duplikate entfernen (User die in mehreren Tenants sind)
            users = users.stream()
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
        }

        // Filter nach Studiengang
        if (studiengang != null && !studiengang.isEmpty()) {
            users = users.stream()
                    .filter(user -> user.getStudiengaenge() != null && 
                            user.getStudiengaenge().stream()
                                    .anyMatch(sg -> sg.equalsIgnoreCase(studiengang)))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Filter nach Rolle
        if (role != null) {
            users = users.stream()
                    .filter(user -> user.getRoles() != null && user.getRoles().contains(role))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Filter nach Status (ACTIVE, INACTIVE, PENDING, SUSPENDED)
        if (status != null) {
            users = users.stream()
                    .filter(user -> user.getStatus() == status)
                    .collect(java.util.stream.Collectors.toList());
        }

        // Paging anwenden
        int totalElements = users.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Validierung
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Max page size
        
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElements);
        
        List<UserResponse> pagedUsers = fromIndex < totalElements 
                ? users.subList(fromIndex, toIndex)
                : new java.util.ArrayList<>();

        // Response aufbauen
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("content", pagedUsers);
        response.put("currentPage", page);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("pageSize", size);
        response.put("hasNext", page < totalPages - 1);
        response.put("hasPrevious", page > 0);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/by-matrikelnummer/{matrikelnummer}")
    public ResponseEntity<UserAuthResponse> getUserAuthByMatrikelnummer(
            @PathVariable Long matrikelnummer) {
        // API-Key Validierung erfolgt durch ApiKeyValidationFilter
        return ResponseEntity.ok(userService.getUserAuthByMatrikelnummer(matrikelnummer));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@jakarta.validation.Valid @RequestBody CreateUserRequest request) {
        var currentRoles = SecurityUtils.getCurrentRoles();
        
        if (currentRoles == null) {
            throw new ForbiddenException("Access denied: Authentication required");
        }

        boolean isAdmin = currentRoles.contains(Role.ADMIN);
        boolean isPruefungsamt = currentRoles.contains(Role.PRUEFUNGSAMT);

        if (!isAdmin && !isPruefungsamt) {
            throw new ForbiddenException("Access denied: Only ADMIN and PRUEFUNGSAMT can create users");
        }

        // PRUEFUNGSAMT darf keine ADMIN- oder PRUEFUNGSAMT-User anlegen
        if (isPruefungsamt && request.getRoles() != null &&
                (request.getRoles().contains(Role.ADMIN) || request.getRoles().contains(Role.PRUEFUNGSAMT))) {
            throw new ForbiddenException("Access denied: PRUEFUNGSAMT cannot create ADMIN or PRUEFUNGSAMT users");
        }

        UUID creatorId = SecurityUtils.getCurrentUserId();

        if (isPruefungsamt) {
            
            Set<UUID> currentTenants = getCurrentUserTenants();
            if (currentTenants == null || currentTenants.isEmpty()) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT without tenant assignment");
            }
            request.setTenantIds(currentTenants);
        }

        User user = userService.createUser(request, creatorId);
        log.info("User created by {}: matrikelnummer {}", creatorId, user.getMatrikelnummer());
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.getUser(user.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @jakarta.validation.Valid @RequestBody UpdateUserRequest request) {
        var currentRoles = SecurityUtils.getCurrentRoles();

        if (currentRoles == null) {
            throw new ForbiddenException("Access denied: Authentication required");
        }

        boolean isAdmin = currentRoles.contains(Role.ADMIN);
        boolean isPruefungsamt = currentRoles.contains(Role.PRUEFUNGSAMT);

        if (!isAdmin && !isPruefungsamt) {
            throw new ForbiddenException("Access denied: Only ADMIN and PRUEFUNGSAMT can update users");
        }

        UUID updaterId = SecurityUtils.getCurrentUserId();
        User target = userService.findById(id).orElseThrow(() -> new com.university.user_service.exception.NotFoundException("User not found"));

        if (isPruefungsamt) {
            // PRUEFUNGSAMT darf nur Nutzer im eigenen Tenant bearbeiten
            Set<UUID> currentTenants = getCurrentUserTenants(updaterId);
            if (!hasCommonTenant(currentTenants, target.getTenantIds())) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT can only update users from its own tenant");
            }

            // PRUEFUNGSAMT darf keine ADMIN- oder PRUEFUNGSAMT-User bearbeiten
            if (target.getRoles() != null && (target.getRoles().contains(Role.ADMIN) || target.getRoles().contains(Role.PRUEFUNGSAMT))) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT cannot modify ADMIN or PRUEFUNGSAMT users");
            }

            // PRUEFUNGSAMT darf Tenants nicht ändern
            request.setTenantIds(null);
        }

        User user = userService.updateUser(id, request, updaterId);
        log.info("User updated by {}: {}", updaterId, id);
        return ResponseEntity.ok(userService.getUser(user.getId()));
    }

    @PatchMapping("/{id}/credentials")
    public ResponseEntity<UserResponse> updateCredentials(
            @PathVariable UUID id,
            @jakarta.validation.Valid @RequestBody UpdateCredentialsRequest request) {
        var currentRoles = SecurityUtils.getCurrentRoles();

        if (currentRoles == null) {
            throw new ForbiddenException("Access denied: Authentication required");
        }

        boolean isAdmin = currentRoles.contains(Role.ADMIN);
        boolean isPruefungsamt = currentRoles.contains(Role.PRUEFUNGSAMT);

        if (!isAdmin && !isPruefungsamt) {
            throw new ForbiddenException("Access denied: Only ADMIN and PRUEFUNGSAMT can update credentials");
        }

        UUID updaterId = SecurityUtils.getCurrentUserId();
        User target = userService.findById(id)
                .orElseThrow(() -> new com.university.user_service.exception.NotFoundException("User not found"));

        if (isPruefungsamt) {
            Set<UUID> currentTenants = getCurrentUserTenants(updaterId);
            if (!hasCommonTenant(currentTenants, target.getTenantIds())) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT can only update users from its own tenant");
            }

            if (target.getRoles() != null
                    && (target.getRoles().contains(Role.ADMIN) || target.getRoles().contains(Role.PRUEFUNGSAMT))) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT cannot modify ADMIN or PRUEFUNGSAMT users");
            }
        }

        boolean allowMatrikelChange = isAdmin || isPruefungsamt;
        userService.updateCredentials(id, request, updaterId, allowMatrikelChange);
        log.info("Credentials updated by {}: {}", updaterId, id);
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        var currentRoles = SecurityUtils.getCurrentRoles();

        if (currentRoles == null) {
            throw new ForbiddenException("Access denied: Authentication required");
        }

        boolean isAdmin = currentRoles.contains(Role.ADMIN);
        boolean isPruefungsamt = currentRoles.contains(Role.PRUEFUNGSAMT);

        if (!isAdmin && !isPruefungsamt) {
            throw new ForbiddenException("Access denied: Only ADMIN and PRUEFUNGSAMT can deactivate users");
        }

        UUID updaterId = SecurityUtils.getCurrentUserId();
        User target = userService.findById(id).orElseThrow(() -> new com.university.user_service.exception.NotFoundException("User not found"));

        if (isPruefungsamt) {
            Set<UUID> currentTenants = getCurrentUserTenants(updaterId);
            if (!hasCommonTenant(currentTenants, target.getTenantIds())) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT can only deactivate users from its own tenant");
            }
            if (target.getRoles() != null && (target.getRoles().contains(Role.ADMIN) || target.getRoles().contains(Role.PRUEFUNGSAMT))) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT cannot deactivate ADMIN or PRUEFUNGSAMT users");
            }
        }

        userService.deactivateUser(id, updaterId);
        log.info("User deactivated by {}: {}", updaterId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable UUID id) {
        var currentRoles = SecurityUtils.getCurrentRoles();

        if (currentRoles == null) {
            throw new ForbiddenException("Access denied: Authentication required");
        }

        boolean isAdmin = currentRoles.contains(Role.ADMIN);
        boolean isPruefungsamt = currentRoles.contains(Role.PRUEFUNGSAMT);

        if (!isAdmin && !isPruefungsamt) {
            throw new ForbiddenException("Access denied: Only ADMIN and PRUEFUNGSAMT can activate users");
        }

        UUID updaterId = SecurityUtils.getCurrentUserId();
        User target = userService.findById(id).orElseThrow(() -> new com.university.user_service.exception.NotFoundException("User not found"));

        if (isPruefungsamt) {
            Set<UUID> currentTenants = getCurrentUserTenants(updaterId);
            if (!hasCommonTenant(currentTenants, target.getTenantIds())) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT can only activate users from its own tenant");
            }
            if (target.getRoles() != null && (target.getRoles().contains(Role.ADMIN) || target.getRoles().contains(Role.PRUEFUNGSAMT))) {
                throw new ForbiddenException("Access denied: PRUEFUNGSAMT cannot activate ADMIN or PRUEFUNGSAMT users");
            }
        }

        userService.activateUser(id, updaterId);
        log.info("User activated by {}: {}", updaterId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        var currentRoles = SecurityUtils.getCurrentRoles();

        if (currentRoles == null || !currentRoles.contains(Role.ADMIN)) {
            throw new ForbiddenException("Access denied: Only ADMIN can permanently delete users");
        }

        userService.deleteUser(id);
        UUID deleterId = SecurityUtils.getCurrentUserId();
        log.info("User hard-deleted by {}: {}", deleterId, id);
        return ResponseEntity.noContent().build();
    }

    private boolean hasCommonTenant(Set<UUID> userTenants, Set<UUID> targetTenants) {
        if (userTenants == null || userTenants.isEmpty() || targetTenants == null || targetTenants.isEmpty()) {
            return false;
        }
        return userTenants.stream().anyMatch(targetTenants::contains);
    }

    
    private Set<UUID> getCurrentUserTenants() {
        // Prefer tenant IDs from SecurityUtils (set by Gateway/JWT) and fall back to DB
        Set<UUID> headerTenantIds = SecurityUtils.getCurrentTenantIds();
        if (headerTenantIds != null && !headerTenantIds.isEmpty()) {
            return headerTenantIds;
        }
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            return null;
        }
        return userService.findById(currentUserId)
                .map(User::getTenantIds)
                .orElse(null);
    }

    
    private Set<UUID> getCurrentUserTenants(UUID userId) {
        if (userId == null) {
            return null;
        }
        // If asking for the current authenticated user, prefer header claims
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(userId)) {
            Set<UUID> headerTenantIds = SecurityUtils.getCurrentTenantIds();
            if (headerTenantIds != null && !headerTenantIds.isEmpty()) {
                return headerTenantIds;
            }
        }
        // Fallback: read from DB
        return userService.findById(userId)
                .map(User::getTenantIds)
                .orElse(null);
    }
}
