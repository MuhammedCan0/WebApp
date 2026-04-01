package com.university.user_service.config;

import com.university.user_service.common.Role;
import com.university.user_service.common.Status;
import com.university.user_service.user.UserRepository;
import com.university.user_service.user.model.User;
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
        public CommandLineRunner initializeData(UserRepository userRepository) {
        return args -> {
            log.info("Initializing sample data...");

                        // Fixed UUIDs for initial demo data so they stay stable across restarts
                        UUID tenantId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
                        UUID tenantId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

                        UUID adminId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-000000000001");
                        UUID pruefungsamtId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-000000000002");
                        UUID studentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-000000000003");
                        UUID lehrenderId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-000000000004");
                        UUID inactiveId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-000000000005");
                        UUID pruefungsamtFb2Id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-000000000006");
                        UUID informatikerFb2Id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-000000000007");
                        UUID multiStudentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-000000000008");

            // Create Users

            // ADMIN User (kein Studiengang, kein Fachbereich zugeordnet)
            User admin = User.builder()
                    .matrikelnummer(1L)
                    .firstName("Admin")
                    .lastName("User")
                    .roles(Set.of(Role.ADMIN))
                    .status(Status.ACTIVE)
                    .build();
            admin.setId(adminId);
            userRepository.save(admin);
            log.info("Created admin user with Matrikelnummer: {}", admin.getMatrikelnummer());

            // PRUEFUNGSAMT User (FB3: BA + MA)
            User pruefungsamt = User.builder()
                    .matrikelnummer(2L)
                    .firstName("Prüfungs")
                    .lastName("Amt")
                    .studiengaenge(Set.of(
                            "Accounting_and_Finance_MA",
                            "Luftverkehrsmanagement_-_Aviation_Management_dual_BA"
                    ))
                    .roles(Set.of(Role.PRUEFUNGSAMT))
                    .status(Status.ACTIVE)
                    .tenantIds(Set.of(tenantId1))
                    .build();
            pruefungsamt.setId(pruefungsamtId);
            userRepository.save(pruefungsamt);
            log.info("Created pruefungsamt user with Matrikelnummer: {}", pruefungsamt.getMatrikelnummer());

            // STUDENT User (FB3: BA)
            User student = User.builder()
                    .matrikelnummer(3L)
                    .firstName("Max")
                    .lastName("Mustermann")
                    .studiengaenge(Set.of("Luftverkehrsmanagement_-_Aviation_Management_dual_BA"))
                    .roles(Set.of(Role.STUDENT))
                    .status(Status.ACTIVE)
                    .tenantIds(Set.of(tenantId1))
                    .build();
            student.setId(studentId);
            userRepository.save(student);
            log.info("Created student user with Matrikelnummer: {}", student.getMatrikelnummer());

            // LEHRENDER User (Multi-Tenant: lehrt in beiden Fachbereichen)
            User lehrender = User.builder()
                    .matrikelnummer(4L)
                    .firstName("Prof")
                    .lastName("Dozent")
                    .studiengaenge(null)  // LEHRENDER hat keine Studiengänge zugeordnet
                    .roles(Set.of(Role.LEHRENDER))
                    .status(Status.ACTIVE)
                    .tenantIds(Set.of(tenantId1, tenantId2))  // Lehrt in FB2 UND FB3
                    .build();
            lehrender.setId(lehrenderId);
            userRepository.save(lehrender);
            log.info("Created lehrender user with Matrikelnummer: {} (Multi-Tenant: FB2 + FB3)", lehrender.getMatrikelnummer());

            // INACTIVE User
            User inactive = User.builder()
                    .matrikelnummer(5L)
                    .firstName("Inactive")
                    .lastName("User")
                    .studiengaenge(Set.of("Accounting_and_Finance_MA"))
                    .roles(Set.of(Role.STUDENT))
                    .status(Status.INACTIVE)
                    .tenantIds(Set.of(tenantId1))
                    .build();
            inactive.setId(inactiveId);
            userRepository.save(inactive);
            log.info("Created inactive user with Matrikelnummer: {}", inactive.getMatrikelnummer());

            // PRUEFUNGSAMT User für Fachbereich 2 (Demo: nur FB3-Studiengänge)
            User pruefungsamtFb2 = User.builder()
                    .matrikelnummer(6L)
                    .firstName("Prüfungs")
                    .lastName("Amt FB2")
                    .studiengaenge(Set.of("Accounting_and_Finance_MA"))
                    .roles(Set.of(Role.PRUEFUNGSAMT))
                    .status(Status.ACTIVE)
                    .tenantIds(Set.of(tenantId2))
                    .build();
            pruefungsamtFb2.setId(pruefungsamtFb2Id);
            userRepository.save(pruefungsamtFb2);
            log.info("Created pruefungsamt FB2 user with Matrikelnummer: {}", pruefungsamtFb2.getMatrikelnummer());

            // STUDENT für Fachbereich 2 (Demo: nur FB3-Studiengänge)
            User informatikerFb2 = User.builder()
                    .matrikelnummer(7L)
                    .firstName("Informatik")
                    .lastName("Student FB2")
                    .studiengaenge(Set.of("Accounting_and_Finance_MA"))
                    .roles(Set.of(Role.STUDENT))
                    .status(Status.ACTIVE)
                    .tenantIds(Set.of(tenantId2))
                    .build();
            informatikerFb2.setId(informatikerFb2Id);
            userRepository.save(informatikerFb2);
            log.info("Created informatiker FB2 user with Matrikelnummer: {}", informatikerFb2.getMatrikelnummer());

            // Multi-Tenant STUDENT (BA + MA)
            User multiStudent = User.builder()
                    .matrikelnummer(8L)
                    .firstName("Multi")
                    .lastName("Student")
                    .studiengaenge(Set.of(
                            "Luftverkehrsmanagement_-_Aviation_Management_dual_BA",
                            "Accounting_and_Finance_MA"
                    ))
                    .roles(Set.of(Role.STUDENT))
                    .status(Status.ACTIVE)
                    .tenantIds(Set.of(tenantId1, tenantId2))  // Beide Tenants: FB2 + FB3
                    .build();
            multiStudent.setId(multiStudentId);
            userRepository.save(multiStudent);
            log.info("Created multi-tenant student with Matrikelnummer: {} (Studies: Luftverkehrsmanagement BA + Accounting_and_Finance MA)", multiStudent.getMatrikelnummer());

            log.info("Sample data initialization completed!");
        };
    }
}
