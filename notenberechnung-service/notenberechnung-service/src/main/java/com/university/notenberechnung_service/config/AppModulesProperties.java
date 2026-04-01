package com.university.notenberechnung_service.config;

import com.university.notenberechnung_service.model.ModulConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Binds application modules configuration.
 * Example YAML structure:
 * app:
 *   modules:
 *     BA_LVM:
 *       - modulId: "1"
 *         modulName: "Einführung ..."
 *         gewichtung: 1.0
 *     MA_AF:
 *       - modulId: "1"
 *         modulName: "Nationale ..."
 *         gewichtung: 2.0
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppModulesProperties {
    private Map<String, List<ModulConfig>> modules;

    public Map<String, List<ModulConfig>> getModules() {
        return modules;
    }

    public void setModules(Map<String, List<ModulConfig>> modules) {
        this.modules = modules;
    }
}
