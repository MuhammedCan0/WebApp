package com.university.user_service.user;

import com.university.user_service.common.Role;
import com.university.user_service.common.Status;
import com.university.user_service.exception.BadRequestException;
import com.university.user_service.exception.ConflictException;
import com.university.user_service.exception.ForbiddenException;

import com.university.user_service.exception.NotFoundException;

import com.university.user_service.auth.client.AuthServiceClient;
import com.university.user_service.user.dto.CreateUserRequest;
import com.university.user_service.user.dto.UpdateCredentialsRequest;
import com.university.user_service.user.dto.UpdateUserRequest;
import com.university.user_service.user.dto.UserAuthResponse;
import com.university.user_service.user.dto.UserResponse;
import com.university.user_service.user.model.User;
import com.university.user_service.tenant.client.TenantClient;
import com.university.user_service.tenant.client.TenantDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service für Benutzer-Verwaltung und Geschäftslogik.
 * 
 * <p>Funktionen:
 * <ul>
 *   <li>CRUD-Operationen für Benutzer</li>
 *   <li>Automatische Matrikelnummer-Generierung</li>
 *   <li>Rollen-spezifische Validierungen (STUDENT, LEHRENDER, PRUEFUNGSAMT, ADMIN)</li>
 *   <li>Multi-Tenant- und Multi-Studiengang-Unterstützung</li>
 *   <li>Status-Management (ACTIVE, INACTIVE)</li>
 * </ul>
 * 
 * <p>Validierungsregeln nach Rolle:
 * <ul>
 *   <li>STUDENT: Min. 1 Studiengang erforderlich, Studiengänge gegen Tenants validiert</li>
 *   <li>PRUEFUNGSAMT: Genau 1 Tenant, Studiengänge optional</li>
 *   <li>LEHRENDER: Mehrere Tenants erlaubt, keine Studiengänge erforderlich</li>
 *   <li>ADMIN: Keine Tenant/Studiengang-Anforderungen</li>
 * </ul>
 * 
 * @see UserController
 * @see UserRepository
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantClient tenantClient;
    private final AuthServiceClient authServiceClient;

    public UserService(UserRepository userRepository,
                       TenantClient tenantClient,
                       AuthServiceClient authServiceClient) {
        this.userRepository = userRepository;
        this.tenantClient = tenantClient;
        this.authServiceClient = authServiceClient;
    }

    public User createUser(CreateUserRequest request, UUID creatorId) {
        validateCreateRequest(request);

        Set<UUID> tenantIds = request.getTenantIds();
        
        // Konsolidiere Studiengänge: studiengaenge hat Vorrang, sonst studiengang als Fallback
        Set<String> studiengaenge = new java.util.HashSet<>();
        if (request.getStudiengaenge() != null && !request.getStudiengaenge().isEmpty()) {
            studiengaenge.addAll(request.getStudiengaenge());
        } else if (request.getStudiengang() != null && !request.getStudiengang().isEmpty()) {
            studiengaenge.add(request.getStudiengang());
        }

        // Tenant-Auflösung über Fachbereiche oder Studiengang
        if (tenantIds == null || tenantIds.isEmpty()) {
            tenantIds = new java.util.HashSet<>();
            
            // Option 1: Mehrere Fachbereiche gegeben
            if (request.getFachbereiche() != null && !request.getFachbereiche().isEmpty()) {
                for (String fachbereich : request.getFachbereiche()) {
                        UUID tenantId = tenantClient.findByIdentifier(fachbereich)
                            .map(TenantDto::getId)
                            .orElseThrow(() -> new NotFoundException("Fachbereich with identifier '" + fachbereich + "' not found"));
                    tenantIds.add(tenantId);
                }
            }
            // Option 2: Ein Fachbereich gegeben
            else if (request.getFachbereich() != null && !request.getFachbereich().isEmpty()) {
                UUID tenantId = tenantClient.findByIdentifier(request.getFachbereich())
                    .map(TenantDto::getId)
                        .orElseThrow(() -> new NotFoundException("Fachbereich with identifier '" + request.getFachbereich() + "' not found"));
                tenantIds.add(tenantId);
            }
            // Option 3: Studiengänge gegeben - nehme alle möglichen Tenants
            else if (!studiengaenge.isEmpty()) {
                for (String sg : studiengaenge) {
                    UUID tenantId = tenantClient.resolveTenantIdForStudiengang(sg);
                    tenantIds.add(tenantId);
                }
            }
            // Keine Möglichkeit gefunden - nur für nicht-ADMIN User erforderlich
            else if (!request.getRoles().contains(Role.ADMIN)) {
                throw new BadRequestException("Either tenantIds, fachbereich(e) or studiengang must be provided for user creation");
            }
        }

        // Rollen-spezifische Verarbeitung
        if (request.getRoles().contains(Role.PRUEFUNGSAMT)) {
            // PRUEFUNGSAMT: genau EIN Tenant, studiengänge optional
            if (tenantIds.size() > 1) {
                throw new BadRequestException("PRUEFUNGSAMT can only be assigned to ONE Fachbereich");
            }
            // Falls Studiengänge angegeben: validieren gegen den einen Tenant
            for (String sg : studiengaenge) {
                boolean valid = tenantIds.stream().anyMatch(tid -> 
                    tenantClient.isStudiengangAllowedForTenant(tid, sg));
                if (!valid) {
                    throw new BadRequestException("Studiengang " + sg + " is not allowed for the given tenant");
                }
            }
        } else if (request.getRoles().contains(Role.STUDENT)) {
            // STUDENT: mindestens ein Studiengang erforderlich
            if (studiengaenge.isEmpty()) {
                throw new BadRequestException("At least one studiengang is required for STUDENT role");
            }
            // Validiere alle Studiengänge gegen die Tenants
            for (String sg : studiengaenge) {
                boolean valid = tenantIds.stream().anyMatch(tid -> 
                    tenantClient.isStudiengangAllowedForTenant(tid, sg));
                if (!valid) {
                    throw new BadRequestException("Studiengang " + sg + " is not allowed for any of the given tenants");
                }
            }
        } else if (request.getRoles().contains(Role.LEHRENDER)) {
            // LEHRENDER: studiengänge optional, kann mehrere Tenants haben
            // Falls Studiengänge angegeben: validieren gegen mindestens einen Tenant
            for (String sg : studiengaenge) {
                boolean valid = tenantIds.stream().anyMatch(tid -> 
                    tenantClient.isStudiengangAllowedForTenant(tid, sg));
                if (!valid) {
                    throw new BadRequestException("Studiengang " + sg + " is not allowed for any of the given tenants");
                }
            }
        }

        // Generate matrikelnummer
        Long nextMatrikelnummer = userRepository.getNextMatrikelnummer();

        User user = User.builder()
            .matrikelnummer(nextMatrikelnummer)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .studiengaenge(studiengaenge.isEmpty() ? null : studiengaenge)
            .roles(request.getRoles())
            .status(Status.ACTIVE)
            .tenantIds(tenantIds)
            .build();

        user.setAuditCreate(creatorId);
        log.info("Creating user with matrikelnummer: {}", nextMatrikelnummer);
        User saved = userRepository.save(user);

        // Register credentials in Auth-Service
        // If email already exists, Auth-Service will throw ConflictException
        // In that case, we rollback the user creation
        try {
            authServiceClient.registerCredentials(nextMatrikelnummer, request.getEmail(), request.getPassword());
            log.info("Successfully registered credentials for matrikelnummer: {}", nextMatrikelnummer);
        } catch (ConflictException ex) {
            // Email already exists - rollback user creation
            userRepository.deleteById(saved.getId());
            log.warn("Rolled back user creation due to email conflict: {}", request.getEmail());
            throw ex;
        } catch (RuntimeException ex) {
            // Other error - rollback user creation
            userRepository.deleteById(saved.getId());
            log.error("Rolled back user creation due to error: {}", ex.getMessage());
            throw ex;
        }

        return saved;
    }

    public UserResponse getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return mapToResponse(user);
    }

    public UserAuthResponse getUserAuthByMatrikelnummer(Long matrikelnummer) {
        User user = userRepository.findByMatrikelnummer(matrikelnummer)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return mapToAuthResponse(user);
    }

    public User updateCredentials(UUID id, UpdateCredentialsRequest request, UUID updaterId, boolean allowMatrikelChange) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if ((request.getEmail() == null || request.getEmail().isBlank())
                && (request.getPassword() == null || request.getPassword().isBlank())
                && request.getMatrikelnummer() == null) {
            throw new BadRequestException("No credential changes provided");
        }

        Long newMatrikelnummer = request.getMatrikelnummer();
        if (newMatrikelnummer != null && !newMatrikelnummer.equals(user.getMatrikelnummer())) {
            if (!allowMatrikelChange) {
                throw new ForbiddenException("Access denied: Matrikelnummer changes are not allowed");
            }
            if (userRepository.existsByMatrikelnummer(newMatrikelnummer)) {
                throw new ConflictException("Matrikelnummer is already in use");
            }
        }

        authServiceClient.updateCredentials(
                user.getMatrikelnummer(),
                request.getEmail(),
                request.getPassword(),
                newMatrikelnummer
        );

        if (newMatrikelnummer != null && !newMatrikelnummer.equals(user.getMatrikelnummer())) {
            user.setMatrikelnummer(newMatrikelnummer);
            user.setAuditUpdate(updaterId);
            return userRepository.save(user);
        }

        return user;
    }

    public User updateUser(UUID id, UpdateUserRequest request, UUID updaterId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Bestimmen, wie Tenants und Studiengänge nach dem Update aussehen würden
        Set<UUID> effectiveTenantIds = (request.getTenantIds() != null && !request.getTenantIds().isEmpty())
                ? new java.util.HashSet<>(request.getTenantIds())
                : new java.util.HashSet<>(user.getTenantIds() != null ? user.getTenantIds() : Set.of());
        
        // Konsolidiere Studiengänge
        Set<String> effectiveStudiengaenge = new java.util.HashSet<>();
        if (request.getStudiengaenge() != null && !request.getStudiengaenge().isEmpty()) {
            effectiveStudiengaenge.addAll(request.getStudiengaenge());
        } else if (request.getStudiengang() != null && !request.getStudiengang().isEmpty()) {
            effectiveStudiengaenge.add(request.getStudiengang());
        } else if (user.getStudiengaenge() != null) {
            effectiveStudiengaenge.addAll(user.getStudiengaenge());
        }

        if (effectiveTenantIds == null || effectiveTenantIds.isEmpty()) {
            throw new BadRequestException("At least one tenantId is required for user update");
        }
        
        // 🆕 AUTO-TENANT-RESOLUTION: Wenn neue Studiengänge hinzugefügt werden,
        // automatisch die entsprechenden Tenants hinzufügen
        if (request.getStudiengaenge() != null && !request.getStudiengaenge().isEmpty()) {
            for (String sg : request.getStudiengaenge()) {
                // Prüfe ob dieser Studiengang bereits in einem der existierenden Tenants erlaubt ist
                boolean existingTenantHasStudiengang = effectiveTenantIds.stream().anyMatch(tid -> 
                    tenantClient.isStudiengangAllowedForTenant(tid, sg));
                
                // Falls nicht, finde den Tenant der diesen Studiengang hat
                if (!existingTenantHasStudiengang) {
                    UUID tenantIdForStudiengang = tenantClient.resolveTenantIdForStudiengang(sg);
                    if (tenantIdForStudiengang != null) {
                        effectiveTenantIds.add(tenantIdForStudiengang);
                        log.info("Auto-added tenant {} for studiengang {}", tenantIdForStudiengang, sg);
                    }
                }
            }
        }
        
        // Validiere alle Studiengänge gegen die Tenants
        for (String sg : effectiveStudiengaenge) {
            if (sg != null && !sg.isEmpty()) {
                boolean valid = effectiveTenantIds.stream().anyMatch(tid -> 
                    tenantClient.isStudiengangAllowedForTenant(tid, sg));
                if (!valid) {
                    throw new BadRequestException("Studiengang " + sg + " is not allowed for any of the given tenants");
                }
            }
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        // Update Studiengänge: setze entweder studiengaenge oder konvertiere studiengang
        if (request.getStudiengaenge() != null) {
            user.setStudiengaenge(request.getStudiengaenge().isEmpty() ? null : request.getStudiengaenge());
        } else if (request.getStudiengang() != null) {
            user.setStudiengaenge(Set.of(request.getStudiengang()));
        }
        if (request.getRoles() != null) {
            validateRoles(request.getRoles());
            user.setRoles(request.getRoles());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        // Setze die effektiven TenantIds (inkl. auto-resolved Tenants)
        user.setTenantIds(effectiveTenantIds);

        user.setAuditUpdate(updaterId);
        log.info("Updating user: {} with {} tenants after auto-resolution", id, effectiveTenantIds.size());
        return userRepository.save(user);
    }

    public void deactivateUser(UUID id, UUID updaterId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setStatus(Status.INACTIVE);
        user.setAuditUpdate(updaterId);
        userRepository.save(user);
        log.info("Deactivated user: {}", id);
    }

    public void activateUser(UUID id, UUID updaterId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setStatus(Status.ACTIVE);
        user.setAuditUpdate(updaterId);
        userRepository.save(user);
        log.info("Activated user: {}", id);
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        Long matrikelnummer = user.getMatrikelnummer();
        
        // Erst User löschen
        userRepository.deleteById(id);
        log.info("Hard deleted user: {}", id);
        
        // Dann Credentials im Auth-Service löschen
        if (matrikelnummer != null) {
            try {
                authServiceClient.deleteCredentials(matrikelnummer);
                log.info("Deleted credentials for matrikelnummer: {}", matrikelnummer);
            } catch (Exception e) {
                log.error("Failed to delete credentials for matrikelnummer {}: {}", matrikelnummer, e.getMessage());
                // User ist bereits gelöscht, aber Credentials blieben zurück
                // Das ist nicht kritisch, aber sollte geloggt werden
            }
        }
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getStatus() != Status.DELETED)
                .map(this::mapToResponse)
                .sorted((u1, u2) -> {
                    // Sortierung nach Matrikelnummer
                    if (u1.getMatrikelnummer() == null && u2.getMatrikelnummer() == null) return 0;
                    if (u1.getMatrikelnummer() == null) return 1;
                    if (u2.getMatrikelnummer() == null) return -1;
                    return u1.getMatrikelnummer().compareTo(u2.getMatrikelnummer());
                })
                .collect(Collectors.toList());
    }

    public List<UserResponse> getUsersByTenant(UUID tenantId) {
        return userRepository.findAll().stream()
                .filter(u -> u.getStatus() != Status.DELETED)
                .filter(u -> u.getTenantIds() != null && u.getTenantIds().contains(tenantId))
                .map(this::mapToResponse)
                .sorted((u1, u2) -> {
                    // Sortierung nach Matrikelnummer
                    if (u1.getMatrikelnummer() == null && u2.getMatrikelnummer() == null) return 0;
                    if (u1.getMatrikelnummer() == null) return 1;
                    if (u2.getMatrikelnummer() == null) return -1;
                    return u1.getMatrikelnummer().compareTo(u2.getMatrikelnummer());
                })
                .collect(Collectors.toList());
    }

    private void validateCreateRequest(CreateUserRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new BadRequestException("At least one role must be assigned");
        }

        validateRoles(request.getRoles());

        // Rolle-spezifische Validierung
        if (request.getRoles().contains(Role.PRUEFUNGSAMT)) {
            // PRUEFUNGSAMT benötigt entweder tenantIds, fachbereich oder fachbereiche
            if ((request.getTenantIds() == null || request.getTenantIds().isEmpty()) &&
                (request.getFachbereich() == null || request.getFachbereich().isEmpty()) &&
                (request.getFachbereiche() == null || request.getFachbereiche().isEmpty())) {
                throw new BadRequestException("For PRUEFUNGSAMT role, either tenantIds, fachbereich, or fachbereiche must be provided");
            }
            // PRUEFUNGSAMT darf nur EINEN Fachbereich haben
            int tenantCount = 0;
            if (request.getTenantIds() != null) tenantCount += request.getTenantIds().size();
            if (request.getFachbereich() != null && !request.getFachbereich().isEmpty()) tenantCount += 1;
            if (request.getFachbereiche() != null) tenantCount += request.getFachbereiche().size();
            if (tenantCount > 1) {
                throw new BadRequestException("PRUEFUNGSAMT can only be assigned to ONE Fachbereich");
            }
        } else if (request.getRoles().contains(Role.STUDENT)) {
            // STUDENT benötigt mindestens einen Studiengang (plus tenantIds ODER fachbereich)
            if ((request.getStudiengang() == null || request.getStudiengang().isEmpty()) &&
                (request.getStudiengaenge() == null || request.getStudiengaenge().isEmpty())) {
                throw new BadRequestException("For STUDENT role, at least one studiengang is required");
            }
            if ((request.getTenantIds() == null || request.getTenantIds().isEmpty()) &&
                (request.getFachbereich() == null || request.getFachbereich().isEmpty()) &&
                (request.getFachbereiche() == null || request.getFachbereiche().isEmpty())) {
                throw new BadRequestException("For STUDENT role, either tenantIds, fachbereich, or fachbereiche must be provided");
            }
        } else if (request.getRoles().contains(Role.LEHRENDER)) {
            // LEHRENDER benötigt entweder studiengang, tenantIds, fachbereich(e) (studiengang ist optional)
            if ((request.getTenantIds() == null || request.getTenantIds().isEmpty()) &&
                (request.getFachbereich() == null || request.getFachbereich().isEmpty()) &&
                (request.getFachbereiche() == null || request.getFachbereiche().isEmpty()) &&
                (request.getStudiengang() == null || request.getStudiengang().isEmpty())) {
                throw new BadRequestException("For LEHRENDER role, at least one of studiengang, tenantIds, fachbereich, or fachbereiche must be provided");
            }
        }
    }

    private void validateRoles(java.util.Set<Role> roles) {
        for (Role role : roles) {
            if (role == null) {
                throw new BadRequestException("Invalid role: null");
            }
        }
    }

    private UserResponse mapToResponse(User user) {
        // Backward compatibility: set studiengang to first entry for old clients
        String firstStudiengang = (user.getStudiengaenge() != null && !user.getStudiengaenge().isEmpty())
                ? user.getStudiengaenge().iterator().next()
                : null;
        
        String email = resolveEmail(user.getMatrikelnummer());

        return UserResponse.builder()
                .id(user.getId())
                .matrikelnummer(user.getMatrikelnummer())
            .email(email)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .studiengang(firstStudiengang)  // Deprecated field for backward compatibility
                .studiengaenge(user.getStudiengaenge())  // New multi-studiengang field
                .roles(user.getRoles())
                .status(user.getStatus())
                .tenantIds(user.getTenantIds())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UserAuthResponse mapToAuthResponse(User user) {
        return UserAuthResponse.builder()
                .id(user.getId())
                .roles(user.getRoles())
                .status(user.getStatus())
                .studiengaenge(user.getStudiengaenge())
                .tenantIds(user.getTenantIds())
                .matrikelnummer(user.getMatrikelnummer())
                .build();
    }

    private String resolveEmail(Long matrikelnummer) {
        return authServiceClient.getEmailByMatrikelnummer(matrikelnummer).orElse(null);
    }
}
