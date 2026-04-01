package com.university.notenberechnung_service.config;

import com.university.notenberechnung_service.model.ModulConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.*;

/**
 * Initializes modules from ModulesSeedData at application startup.
 * This loads all module configurations with tenant associations
 * and populates the in-memory repository.
 * 
 * Module IDs and data are aligned with noten-modulverwaltung-service DataLoader.
 */
@Slf4j
@Configuration
public class ModulesInitializer {

    @Bean
    @Profile("!test")
    public CommandLineRunner loadModulesFromSeedData(AppModulesProperties appModulesProperties) {
        return args -> {
            log.info("Loading modules from ModulesSeedData configuration...");
            
            // Initialize module map from hardcoded seed data
            Map<String, List<ModulConfig>> seedDataModules = new HashMap<>();
            
            // Load Luftverkehrsmanagement modules (Fachbereich 3)
            String lvm_key = "Luftverkehrsmanagement_-_Aviation_Management_dual_BA";
            seedDataModules.put(lvm_key, new ArrayList<>());
            if (ModulesSeedData.MODULES.containsKey(lvm_key)) {
                for (ModulesSeedData.ModuleConfig seedModule : ModulesSeedData.MODULES.get(lvm_key)) {
                    ModulConfig config = new ModulConfig(
                        UUID.randomUUID().toString(),
                        seedModule.getModulId(),
                        seedModule.getModulName(),
                        seedModule.getGewichtung(),
                        seedModule.isActive(),
                        seedModule.getTenantId()
                    );
                    seedDataModules.get(lvm_key).add(config);
                }
                log.info("Loaded {} Luftverkehrsmanagement modules for tenant {}", 
                    seedDataModules.get(lvm_key).size(), 
                    ModulesSeedData.TENANT_FB3_ID);
            }
            
            // Load Accounting and Finance modules (Fachbereich 3)
            String af_key = "Accounting_and_Finance_MA";
            seedDataModules.put(af_key, new ArrayList<>());
            if (ModulesSeedData.MODULES.containsKey(af_key)) {
                for (ModulesSeedData.ModuleConfig seedModule : ModulesSeedData.MODULES.get(af_key)) {
                    ModulConfig config = new ModulConfig(
                        UUID.randomUUID().toString(),
                        seedModule.getModulId(),
                        seedModule.getModulName(),
                        seedModule.getGewichtung(),
                        seedModule.isActive(),
                        seedModule.getTenantId()
                    );
                    seedDataModules.get(af_key).add(config);
                }
                log.info("Loaded {} Accounting and Finance modules for tenant {}", 
                    seedDataModules.get(af_key).size(), 
                    ModulesSeedData.TENANT_FB3_ID);
            }
            
            // Load Informatik_BA modules (Fachbereich 2)
            String informatik_key = "Informatik_BA";
            seedDataModules.put(informatik_key, new ArrayList<>());
            if (ModulesSeedData.MODULES.containsKey(informatik_key)) {
                for (ModulesSeedData.ModuleConfig seedModule : ModulesSeedData.MODULES.get(informatik_key)) {
                    ModulConfig config = new ModulConfig(
                        UUID.randomUUID().toString(),
                        seedModule.getModulId(),
                        seedModule.getModulName(),
                        seedModule.getGewichtung(),
                        seedModule.isActive(),
                        seedModule.getTenantId()
                    );
                    seedDataModules.get(informatik_key).add(config);
                }
                log.info("Loaded {} Informatik_BA modules for tenant {}", 
                    seedDataModules.get(informatik_key).size(), 
                    ModulesSeedData.TENANT_FB2_ID);
            }
            
            // Set modules in AppModulesProperties (will be picked up by repository)
            appModulesProperties.setModules(seedDataModules);
            
            log.info("Module initialization completed. Total Studiengänge: {}", seedDataModules.size());
            log.info("Tenant mapping:");
            log.info("  - FB3 (Fachbereich 3): {} - Luftverkehrsmanagement, Accounting & Finance", ModulesSeedData.TENANT_FB3_ID);
            log.info("  - FB2 (Fachbereich 2): {} - Informatik", ModulesSeedData.TENANT_FB2_ID);
            log.info("  - FB1 (Fachbereich 1): {} - (reserved for future use)", ModulesSeedData.TENANT_FB1_ID);
        };
    }
}
