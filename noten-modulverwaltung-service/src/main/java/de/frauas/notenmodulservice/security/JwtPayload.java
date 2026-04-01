package de.frauas.notenmodulservice.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Roh-Payload aus dem JWT-Token, gemappt 1:1 auf die Claims
 * entsprechend der Dokumentation im auth-service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtPayload {

    private List<String> role;
    private List<String> studiengaenge;
    // Claim-Name hat einen Underscore, daher gleiche Benennung
    private List<String> tenant_ids;
    private Integer matrikelnummer;
    private String email;
    private String sub;

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }

    public List<String> getStudiengaenge() {
        return studiengaenge;
    }

    public void setStudiengaenge(List<String> studiengaenge) {
        this.studiengaenge = studiengaenge;
    }

    public List<String> getTenant_ids() {
        return tenant_ids;
    }

    public void setTenant_ids(List<String> tenant_ids) {
        this.tenant_ids = tenant_ids;
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

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }
}