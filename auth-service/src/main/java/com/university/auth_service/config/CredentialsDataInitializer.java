package com.university.auth_service.config;

import com.university.auth_service.auth.credentials.AuthCredentialsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CredentialsDataInitializer {

    @Bean
    public CommandLineRunner initializeCredentials(AuthCredentialsService credentialsService) {
        return args -> {
            seedCredentials(credentialsService, 1L, "admin@university.de", "AdminPassword123!");
            seedCredentials(credentialsService, 2L, "pruefungsamt@university.de", "PruefungPassword123!");
            seedCredentials(credentialsService, 3L, "student@university.de", "StudentPassword123!");
            seedCredentials(credentialsService, 4L, "lehrender@university.de", "LehrenderPassword123!");
            seedCredentials(credentialsService, 5L, "inactive@university.de", "InactivePassword123!");
            seedCredentials(credentialsService, 6L, "pruefungsamt-fb2@university.de", "PruefungFb2Password123!");
            seedCredentials(credentialsService, 7L, "informatiker@university.de", "InformatikPassword123!");
            seedCredentials(credentialsService, 8L, "multi.student@university.de", "MultiStudent123!");
        };
    }

    private void seedCredentials(AuthCredentialsService credentialsService,
                                 Long matrikelnummer,
                                 String email,
                                 String password) {
        if (credentialsService.findByEmail(email).isPresent()) {
            return;
        }
        try {
            credentialsService.registerCredentials(matrikelnummer, email, password);
            log.info("Seeded credentials for matrikelnummer: {}", matrikelnummer);
        } catch (RuntimeException ex) {
            log.warn("Skipping credentials for matrikelnummer {}: {}", matrikelnummer, ex.getMessage());
        }
    }
}
