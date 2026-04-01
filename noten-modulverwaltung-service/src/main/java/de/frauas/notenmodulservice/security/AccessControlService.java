package de.frauas.notenmodulservice.security;

import de.frauas.notenmodulservice.exception.AccessDeniedException;
import de.frauas.notenmodulservice.exception.AuthenticationException;
import de.frauas.notenmodulservice.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Zentrale Zugriffskontrolle für Rollen- und Tenant-Checks.
 * 
 * <p>Diese Service-Klasse kapselt die Autorisierungslogik und stellt sicher,
 * dass Rollen- und Tenantprüfungen konsistent in allen Controllern erfolgen.</p>
 * 
 * <h3>Rollenbasierte Zugriffskontrolle:</h3>
 * <ul>
 *   <li>{@code STUDENT}: Nur Lesezugriff auf eigene Noten</li>
 *   <li>{@code LEHRENDER}: Noten setzen für eigene Module</li>
 *   <li>{@code PRUEFUNGSAMT}: Noten erstellen, validieren, Module verwalten</li>
 *   <li>{@code ADMIN}: Voller Zugriff, tenant-übergreifend</li>
 * </ul>
 *
 * <h3>Tenant-Isolation:</h3>
 * <p>Jeder Benutzer (außer ADMIN) darf nur auf Ressourcen zugreifen,
 * die zu seinen zugewiesenen Tenants gehören.</p>
 */
@Component
public class AccessControlService {

    public void requireRole(JwtUser jwtUser, Role... allowedRoles) {
        if (jwtUser == null) {
            throw new AuthenticationException("Nicht authentifiziert: JWT fehlt oder ist ungültig");
        }
        if (allowedRoles == null || allowedRoles.length == 0) {
            return; // nichts zu prüfen
        }
        for (Role role : allowedRoles) {
            if (jwtUser.hasRole(role)) {
                return;
            }
        }
        throw new AccessDeniedException("Zugriff verweigert: Rolle nicht erlaubt");
    }

    /**
     * Ermittelt die effektiven Tenant-IDs für eine Anfrage.
     *
     * - Globaler Admin: darf beliebige Tenant-IDs angeben (müssen aber gesetzt sein)
     * - Alle anderen Rollen: Tenant-IDs kommen ausschließlich aus dem JWT
     */
    public List<UUID> resolveTenantIds(JwtUser jwtUser, List<String> requestedTenantIds) {
        if (jwtUser == null) {
            throw new AuthenticationException("Nicht authentifiziert: JWT fehlt oder ist ungültig");
        }

        // Globaler Admin: darf die Tenant-IDs frei wählen (müssen aber angegeben werden)
        if (jwtUser.isGlobalAdmin()) {
            if (requestedTenantIds == null || requestedTenantIds.isEmpty()) {
                throw new ValidationException("Für ADMIN müssen Tenant-IDs angegeben werden");
            }
            return requestedTenantIds.stream()
                    .map(UUID::fromString)
                    .toList();
        }

        // Alle anderen Rollen: Tenant-IDs werden ausschließlich aus dem Token genommen
        List<UUID> tenantIdsFromToken = jwtUser.getTenantIds();
        if (tenantIdsFromToken == null || tenantIdsFromToken.isEmpty()) {
            throw new ValidationException("Im JWT sind keine Tenant-IDs hinterlegt");
        }
        return tenantIdsFromToken;
    }
}
