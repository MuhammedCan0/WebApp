package com.university.tenant_service.config;

import com.university.tenant_service.common.Status;
import com.university.tenant_service.tenant.Tenant;
import com.university.tenant_service.tenant.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeTenantData(TenantRepository tenantRepository) {
        return args -> {
            log.info("Initializing tenant data...");

            UUID tenantId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID tenantId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

            Tenant tenant1 = Tenant.builder()
                    .name("Fachbereich 3")
                    .identifier("FB3-DEPT")
                    .description("Fachbereich 3 with Accounting_and_Finance_MA and Luftverkehrsmanagement_-_Aviation_Management_dual_BA")
                    .status(Status.ACTIVE)
                    .allowedStudiengaenge(Set.of(
                            "Accounting_and_Finance_MA",
                            "Luftverkehrsmanagement_-_Aviation_Management_dual_BA"
                    ))
                    .build();
            tenant1.setId(tenantId1);
            tenantRepository.save(tenant1);
            log.info("Created tenant: {}", tenant1.getIdentifier());

            Tenant tenant2 = Tenant.builder()
                    .name("Fachbereich 2")
                    .identifier("FB2-DEPT")
                    .description("Fachbereich 2 with Informatik_BA")
                    .status(Status.ACTIVE)
                    .allowedStudiengaenge(Set.of("Informatik_BA"))
                    .build();
            tenant2.setId(tenantId2);
            tenantRepository.save(tenant2);
            log.info("Created tenant: {}", tenant2.getIdentifier());

            log.info("Tenant data initialization completed!");
        };
    }
}
