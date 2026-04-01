package com.university.notenberechnung_service.model;

import java.time.LocalDateTime;

public class ModulConfig {

    // Struktur: id, Modul-ID, Modulname, Gewichtung, active, tenantId, createdAt
    private String id;               // Interne ID (z.B. UUID)
    private Integer modulId;         // Offizielle Nummer, z.B. 30
    private String modulName;        // Name, z.B. "Bachelor-Arbeit"
    private double gewichtung;       // Gewichtung (ECTS oder Faktor)
    private boolean active = true;   // Aktiv/Inaktiv-Status (Standard: aktiv)
    private String tenantId;         // Tenant-ID (Fachbereich)
    private LocalDateTime createdAt = LocalDateTime.now(); // Erstelldatum

    // Leerer Konstruktor (für Frameworks wie JPA, Jackson etc.)
    public ModulConfig() {
    }

    // Konstruktor zum Befüllen (ohne Tenant)
    public ModulConfig(String id, Integer modulId, String modulName, double gewichtung) {
        this(id, modulId, modulName, gewichtung, true, null);
    }

    // Vollständiger Konstruktor mit Active-Status und Tenant-ID
    public ModulConfig(String id,
                       Integer modulId,
                       String modulName,
                       double gewichtung,
                       boolean active,
                       String tenantId) {
        this.id = id;
        this.modulId = modulId;
        this.modulName = modulName;
        this.gewichtung = gewichtung;
        this.active = active;
        this.tenantId = tenantId;
    }

    // Getter und Setter

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Integer getModulId() { return modulId; }

    public void setModulId(Integer modulId) { this.modulId = modulId; }

    public String getModulName() { return modulName; }

    public void setModulName(String modulName) { this.modulName = modulName; }

    public double getGewichtung() { return gewichtung; }

    public void setGewichtung(double gewichtung) { this.gewichtung = gewichtung; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public String getTenantId() { return tenantId; }

    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "ModulConfig{" +
                "modulId=" + modulId +
                ", modulName='" + modulName + '\'' +
                ", gewichtung=" + gewichtung +
                ", active=" + active +
                ", tenantId='" + tenantId + '\'' +
                '}';
    }
}