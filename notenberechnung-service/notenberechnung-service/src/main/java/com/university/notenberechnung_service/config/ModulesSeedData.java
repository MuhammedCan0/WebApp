package com.university.notenberechnung_service.config;

import java.util.List;
import java.util.Map;

/**
 * Seed data for modules per Prüfungsordnung (PO).
 * Maps study programs (Studiengänge) to their modules with tenant association.
 * Module IDs are Integer and aligned with noten-modulverwaltung-service DataLoader.
 */
public class ModulesSeedData {

    // Tenant IDs - aligned with auth-service DataInitializer
    public static final String TENANT_FB3_ID = "11111111-1111-1111-1111-111111111111";  // Fachbereich 3
    public static final String TENANT_FB2_ID = "22222222-2222-2222-2222-222222222222";  // Fachbereich 2
    public static final String TENANT_FB1_ID = "33333333-3333-3333-3333-333333333333";  // Fachbereich 1 (neu)

    // Module Configuration
    // Modul IDs sind Integer und aligned mit noten-modulverwaltung-service DataLoader
    public static final Map<String, List<ModuleConfig>> MODULES = Map.ofEntries(
        // Bachelor Luftverkehrsmanagement (BA_LVM) - Fachbereich 3
        // Modul IDs: 1, 2, 6 (aus DataLoader)
        Map.entry("Luftverkehrsmanagement_-_Aviation_Management_dual_BA", List.of(
            new ModuleConfig(1, "Einführung in die Betriebswirtschaftslehre und Schlüsselkompetenzen", 1.0, true, TENANT_FB3_ID),
            new ModuleConfig(2, "Wirtschaftsmathematik", 1.0, true, TENANT_FB3_ID),
            new ModuleConfig(3, "Business English", 1.0, true, TENANT_FB3_ID),
            new ModuleConfig(4, "Wirtschaftsmathematik", 1.0, true, TENANT_FB3_ID),
            new ModuleConfig(5, "Business English", 1.0, true, TENANT_FB3_ID)
        )),
        // Master Accounting and Finance (MA_AF) - Fachbereich 3
        // Modul IDs: 4, 5 (aus DataLoader)
        Map.entry("Accounting_and_Finance_MA", List.of(
            new ModuleConfig(6, "Nationale und internationale Steuerplanung", 2.0, true, TENANT_FB3_ID),
            new ModuleConfig(7, "Unternehmensbewertung und Cost Management", 2.0, true, TENANT_FB3_ID)
        )),
        // Informatik Bachelor (Informatik_BA) - Fachbereich 2
        // Modul IDs: 7, 8 (aus DataLoader)
        Map.entry("Informatik_BA", List.of(
            new ModuleConfig(8, "Programmierung 1", 3.0, true, TENANT_FB2_ID),
            new ModuleConfig(9, "Datenbanken", 3.0, true, TENANT_FB2_ID)
        ))
    );

    /**
     * Inner class for module configuration
     */
    public static class ModuleConfig {
        private final Integer modulId;
        private final String modulName;
        private final double gewichtung;
        private final boolean active;
        private final String tenantId;

        public ModuleConfig(Integer modulId, String modulName, double gewichtung, boolean active, String tenantId) {
            this.modulId = modulId;
            this.modulName = modulName;
            this.gewichtung = gewichtung;
            this.active = active;
            this.tenantId = tenantId;
        }

        public Integer getModulId() {
            return modulId;
        }

        public String getModulName() {
            return modulName;
        }

        public double getGewichtung() {
            return gewichtung;
        }

        public boolean isActive() {
            return active;
        }

        public String getTenantId() {
            return tenantId;
        }
    }
}
