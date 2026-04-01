package de.frauas.notenmodulservice.client;

import java.util.Set;
import java.util.UUID;

public class UserProfileResponse {

    private UUID id;
    private Long matrikelnummer;
    private String firstName;
    private String lastName;
    private String studiengang;
    private Set<String> studiengaenge;
    private Set<UUID> tenantIds;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Long getMatrikelnummer() {
        return matrikelnummer;
    }

    public void setMatrikelnummer(Long matrikelnummer) {
        this.matrikelnummer = matrikelnummer;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStudiengang() {
        return studiengang;
    }

    public void setStudiengang(String studiengang) {
        this.studiengang = studiengang;
    }

    public Set<String> getStudiengaenge() {
        return studiengaenge;
    }

    public void setStudiengaenge(Set<String> studiengaenge) {
        this.studiengaenge = studiengaenge;
    }

    public Set<UUID> getTenantIds() {
        return tenantIds;
    }

    public void setTenantIds(Set<UUID> tenantIds) {
        this.tenantIds = tenantIds;
    }
}
