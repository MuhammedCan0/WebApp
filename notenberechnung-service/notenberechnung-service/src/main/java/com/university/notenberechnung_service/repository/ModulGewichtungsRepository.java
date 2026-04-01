package com.university.notenberechnung_service.repository;

import com.university.notenberechnung_service.config.AppModulesProperties;
import com.university.notenberechnung_service.model.ModulConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository für die Verwaltung von Modulgewichtungen pro Prüfungsordnung.
 * 
 * <p>Verantwortlich für:
 * <ul>
 *   <li>In-Memory Speicherung von Modulkonfigurationen</li>
 *   <li>CRUD-Operationen für Module pro Prüfungsordnung (PO)</li>
 *   <li>Initialisierung aus application.yaml beim Start</li>
 *   <li>Thread-sichere Zugriffe durch ConcurrentHashMap</li>
 * </ul>
 * 
 * <p>Datenstruktur:
 * <ul>
 *   <li>Primärschlüssel: poId (String) → z.B. "Informatik_BA"</li>
 *   <li>Sekundärschlüssel: modulId (Integer) → z.B. 30</li>
 *   <li>Wert: ModulConfig mit Name, Gewichtung, Status, TenantId</li>
 * </ul>
 * 
 * <p>Hinweis: Dies ist ein In-Memory Repository. Änderungen gehen beim
 * Neustart verloren. Für Persistenz müsste eine Datenbank angebunden werden.
 * 
 * @see ModulConfig
 * @see AppModulesProperties
 * @see com.university.notenberechnung_service.service.NotenService
 */
@Repository
public class ModulGewichtungsRepository {

    private final AppModulesProperties appModulesProperties;

    /** In-Memory Store: poId → (modulId → ModulConfig) */
    private final Map<String, Map<Integer, ModulConfig>> store = new ConcurrentHashMap<>();

    public ModulGewichtungsRepository(AppModulesProperties appModulesProperties) {
        this.appModulesProperties = appModulesProperties;
    }

    /**
     * Initialisiert den Store mit den Daten aus der application.yaml nach dem Start.
     */
    @PostConstruct
    public void initFromConfig() {
        Map<String, List<ModulConfig>> configuredModules = appModulesProperties.getModules();
        
        if (configuredModules == null || configuredModules.isEmpty()) {
            return;
        }

        for (Map.Entry<String, List<ModulConfig>> entry : configuredModules.entrySet()) {
            String poId = entry.getKey();
            List<ModulConfig> modules = entry.getValue();
            
            Map<Integer, ModulConfig> poStore = store.computeIfAbsent(poId, k -> new ConcurrentHashMap<>());
            
            for (ModulConfig module : modules) {
                // Generiere UUID falls keine ID vorhanden
                if (module.getId() == null || module.getId().isBlank()) {
                    module.setId(UUID.randomUUID().toString());
                }
                // Setze aktiv auf true falls nicht anders definiert (Standard)
                // Da aus YAML geladen, ist active standardmäßig true (durch Konstruktor)
                
                // ModulId muss Integer sein
                if (module.getModulId() == null) {
                    throw new IllegalArgumentException("ModulId darf nicht null sein");
                }
                
                poStore.put(module.getModulId(), module);
            }
        }
    }

    /**
     * Gibt den Store für eine bestimmte Prüfungsordnung zurück.
     * Erstellt einen neuen Store falls keiner existiert.
     */
    public Map<Integer, ModulConfig> getModulesForPo(String poId) {
        return store.computeIfAbsent(poId, k -> {
            Map<Integer, ModulConfig> poStore = new ConcurrentHashMap<>();

            // Lazy-Initialisierung aus AppModulesProperties, falls dort Module
            // (z.B. über ModulesInitializer / ModulesSeedData) konfiguriert wurden
            Map<String, List<ModulConfig>> configuredModules = appModulesProperties.getModules();
            if (configuredModules != null && !configuredModules.isEmpty()) {
                List<ModulConfig> modulesForPo = configuredModules.get(poId);
                if (modulesForPo != null) {
                    for (ModulConfig module : modulesForPo) {
                        if (module.getId() == null || module.getId().isBlank()) {
                            module.setId(UUID.randomUUID().toString());
                        }
                        if (module.getModulId() == null) {
                            throw new IllegalArgumentException("ModulId darf nicht null sein");
                        }
                        poStore.put(module.getModulId(), module);
                    }
                }
            }

            return poStore;
        });
    }

    /**
     * Speichert ein Modul für eine bestimmte Prüfungsordnung.
     */
    public void saveModule(String poId, ModulConfig module) {
        Map<Integer, ModulConfig> poStore = getModulesForPo(poId);
        if (module.getModulId() == null) {
            throw new IllegalArgumentException("ModulId darf nicht null sein");
        }
        poStore.put(module.getModulId(), module);
    }

    /**
     * Gibt alle Module für eine bestimmte Prüfungsordnung zurück.
     */
    public List<ModulConfig> allModules(String poId) {
        Map<Integer, ModulConfig> poStore = getModulesForPo(poId);
        return new ArrayList<>(poStore.values());
    }

    /**
     * Gibt nur aktive Module für eine bestimmte Prüfungsordnung zurück.
     */
    public List<ModulConfig> activeModules(String poId) {
        Map<Integer, ModulConfig> poStore = getModulesForPo(poId);
        return poStore.values().stream()
                .filter(ModulConfig::isActive)
                .toList();
    }

    /**
     * Findet ein Modul anhand der Modul-ID.
     */
    public Optional<ModulConfig> findByModulId(String poId, Integer modulId) {
        Map<Integer, ModulConfig> poStore = getModulesForPo(poId);
        return Optional.ofNullable(poStore.get(modulId));
    }

    /**
     * Löscht ein Modul anhand der Modul-ID.
     */
    public void deleteModule(String poId, Integer modulId) {
        Map<Integer, ModulConfig> poStore = getModulesForPo(poId);
        poStore.remove(modulId);
    }

    /**
     * Setzt den aktiv/inaktiv Status eines Moduls.
     */
    public Optional<ModulConfig> setModuleActive(String poId, Integer modulId, boolean active) {
        return findByModulId(poId, modulId).map(module -> {
            module.setActive(active);
            return module;
        });
    }

    /**
     * Gibt alle verfügbaren Prüfungsordnungen zurück.
     */
    public Set<String> getAllPoIds() {
        return store.keySet();
    }
}