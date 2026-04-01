package de.frauas.notenmodulservice.security;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Abgeleitete Sicht auf den angemeldeten Benutzer aus dem JWT.
 */
public class JwtUser {

    private List<String> roles = Collections.emptyList();
    private List<UUID> tenantIds = Collections.emptyList();
    private Integer matrikelnummer;
    private String email;
    private String subject;
    private List<String> studiengaenge = Collections.emptyList();

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles != null ? roles : Collections.emptyList();
    }

    public List<UUID> getTenantIds() {
        return tenantIds;
    }

    public void setTenantIds(List<UUID> tenantIds) {
        this.tenantIds = tenantIds != null ? tenantIds : Collections.emptyList();
    }

    public Integer getMatrikelnummer() {
        return matrikelnummer;
    }

    public void setMatrikelnummer(Integer matrikelnummer) {
        this.matrikelnummer = matrikelnummer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getStudiengaenge() {
        return studiengaenge;
    }

    public void setStudiengaenge(List<String> studiengaenge) {
        this.studiengaenge = studiengaenge != null ? studiengaenge : Collections.emptyList();
    }

    /**
     * Rollenprüfung auf Basis eines String-Namens (für bestehende Aufrufer).
     */
    public boolean hasRole(String role) {
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }

    /**
     * Typ-sichere Rollenprüfung über das zentrale Role-Enum.
     */
    public boolean hasRole(Role role) {
        if (role == null) {
            return false;
        }
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role.name()));
    }

    public boolean isStudent() {
        return hasRole(Role.STUDENT);
    }

    public boolean isLehrender() {
        return hasRole(Role.LEHRENDER);
    }

    public boolean isPruefungsamt() {
        return hasRole(Role.PRUEFUNGSAMT);
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Globaler Admin: Rolle ADMIN und keine tenant_ids im Token.
     */
    public boolean isGlobalAdmin() {
        return isAdmin() && (tenantIds == null || tenantIds.isEmpty());
    }
}